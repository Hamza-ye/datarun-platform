import 'dart:convert';
import 'dart:io';

import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/projection_engine.dart';

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late DeviceIdentity identity;
  late ConfigStore configStore;
  late String dbPath;
  const actorA = '11111111-1111-1111-1111-111111111111';
  const actorB = '22222222-2222-2222-2222-222222222222';

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    identity = await DeviceIdentity.init();
    await identity.activateActorSession(
      actorId: actorA,
      token: 'production-token',
    );
    dbPath =
        '${Directory.systemTemp.path}/datarun_sync_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath, actorId: identity.actorId);
    configStore = ConfigStore(store);
    await configStore.init();
  });

  tearDown(() async {
    await store.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  Map<String, dynamic> configPackage(int version) => {
    'version': version,
    'shapes': <String, dynamic>{},
    'activities': <String, dynamic>{},
    'expressions': <String, dynamic>{},
    'flag_severity_overrides': <String, dynamic>{},
    'sensitivity_classifications': <String, dynamic>{
      'shapes': <String, dynamic>{},
      'activities': <String, dynamic>{},
    },
  };

  Map<String, dynamic> serverEvent({
    required String id,
    required String subjectId,
    required String actorId,
    required int sequence,
    required int watermark,
  }) => {
    'id': id,
    'type': 'capture',
    'shape_ref': 'basic_capture/v1',
    'activity_ref': null,
    'subject_ref': {'type': 'subject', 'id': subjectId},
    'actor_ref': {'type': 'actor', 'id': actorId},
    'device_id': 'server',
    'device_seq': sequence,
    'sync_watermark': watermark,
    'timestamp': '2026-06-05T10:00:00Z',
    'payload': {'name': subjectId},
  };

  test('legacy single-actor token migrates into actor-local storage', () async {
    SharedPreferences.setMockInitialValues({
      'device_id': 'device-1',
      'device_seq': 2,
      'actor_id': actorA,
      'actor_token': 'legacy-token',
      'server_url': 'http://server.test',
    });

    final migrated = await DeviceIdentity.init();
    final prefs = await SharedPreferences.getInstance();

    expect(migrated.actorId, actorA);
    expect(migrated.actorToken, 'legacy-token');
    expect(migrated.knownActorIds, contains(actorA));
    expect(migrated.isSetupComplete, isTrue);
    expect(prefs.getString('actor_id'), isNull);
    expect(prefs.getString('actor_token'), isNull);
  });

  test('refreshes actor identity and sends bearer auth on push', () async {
    final assembler = EventAssembler(identity, store);
    await assembler.assemble(
      subjectId: 'subj-42',
      shapeRef: 'basic_capture/v1',
      payload: {'name': 'Test'},
    );

    final paths = <String>[];
    final client = MockClient((request) async {
      paths.add(request.url.path);
      expect(request.headers['Authorization'], 'Bearer production-token');

      if (request.url.path == '/api/auth/me') {
        return http.Response(
          jsonEncode({'actor_id': actorA, 'auth_source': 'jwt-principal'}),
          200,
          headers: {'content-type': 'application/json'},
        );
      }

      if (request.url.path == '/api/sync/push') {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        final events = body['events'] as List<dynamic>;
        expect(events, hasLength(1));
        expect((events.first as Map<String, dynamic>)['actor_ref'], {
          'type': 'actor',
          'id': actorA,
        });
        return http.Response(
          jsonEncode({'accepted': 1, 'duplicates': 0, 'flags_raised': 0}),
          200,
          headers: {'content-type': 'application/json'},
        );
      }

      if (request.url.path == '/api/sync/pull') {
        return http.Response(
          jsonEncode({
            'events': [],
            'latest_watermark': 0,
            'has_more': false,
            'config_version': 0,
          }),
          200,
          headers: {'content-type': 'application/json'},
        );
      }

      if (request.url.path == '/api/sync/config') {
        return http.Response('', 304);
      }

      fail('Unexpected request path: ${request.url.path}');
    });

    final result = await SyncService(
      store,
      identity,
      'http://server.test',
      configStore,
      client: client,
    ).sync();

    expect(result.error, isNull);
    expect(result.pushedCount, 1);
    expect(await store.unpushedCount(), 0);
    expect(paths, [
      '/api/auth/me',
      '/api/sync/push',
      '/api/sync/pull',
      '/api/sync/config',
    ]);
  });

  test(
    'stops before push when auth identity refresh is unauthorized',
    () async {
      final assembler = EventAssembler(identity, store);
      await assembler.assemble(
        subjectId: 'subj-42',
        shapeRef: 'basic_capture/v1',
        payload: {'name': 'Test'},
      );

      final paths = <String>[];
      final client = MockClient((request) async {
        paths.add(request.url.path);
        if (request.url.path == '/api/auth/me') {
          return http.Response(
            jsonEncode({'error': 'invalid_token'}),
            401,
            headers: {'content-type': 'application/json'},
          );
        }
        fail('Unexpected request after unauthorized auth refresh');
      });

      final result = await SyncService(
        store,
        identity,
        'http://server.test',
        configStore,
        client: client,
      ).sync();

      expect(result.error, contains('Unauthorized'));
      expect(await store.unpushedCount(), 1);
      expect(paths, ['/api/auth/me']);
    },
  );

  test(
    'switchActorSession drains A unpushed events with A credential when possible',
    () async {
      final assembler = EventAssembler(identity, store);
      final aEvent = await assembler.assemble(
        subjectId: 'subj-a',
        shapeRef: 'basic_capture/v1',
        payload: {'name': 'A local'},
      );
      await identity.setSyncWatermark(13);

      final paths = <String>[];
      final client = MockClient((request) async {
        paths.add('${request.url.path}:${request.headers['Authorization']}');

        if (request.url.path == '/api/auth/me' &&
            request.headers['Authorization'] == 'Bearer production-token') {
          return http.Response(
            jsonEncode({'actor_id': actorA}),
            200,
            headers: {'content-type': 'application/json'},
          );
        }

        if (request.url.path == '/api/sync/push') {
          expect(request.headers['Authorization'], 'Bearer production-token');
          final body = jsonDecode(request.body) as Map<String, dynamic>;
          expect(body['last_pull_watermark'], 13);
          final events = body['events'] as List<dynamic>;
          expect(events, hasLength(1));
          final event = events.single as Map<String, dynamic>;
          expect(event['id'], aEvent.id);
          expect(event['actor_ref'], {'type': 'actor', 'id': actorA});
          return http.Response(
            jsonEncode({'accepted': 1, 'duplicates': 0, 'flags_raised': 0}),
            200,
            headers: {'content-type': 'application/json'},
          );
        }

        if (request.url.path == '/api/auth/me' &&
            request.headers['Authorization'] == 'Bearer token-b') {
          return http.Response(
            jsonEncode({'actor_id': actorB}),
            200,
            headers: {'content-type': 'application/json'},
          );
        }

        fail('Unexpected request during actor switch: ${request.url.path}');
      });

      final result = await SyncService(
        store,
        identity,
        'http://server.test',
        configStore,
        client: client,
      ).switchActorSession('token-b');

      expect(result.success, isTrue);
      expect(result.actorId, actorB);
      expect(identity.actorToken, 'token-b');
      expect(await store.unpushedCount(), 0);
      expect(paths, [
        '/api/auth/me:Bearer production-token',
        '/api/sync/push:Bearer production-token',
        '/api/auth/me:Bearer token-b',
      ]);
    },
  );

  test(
    'switchActorSession seals prior partition and isolates B state',
    () async {
      await store.close();
      final partitionDir = Directory.systemTemp.createTempSync(
        'datarun_partitions_',
      );
      EventStore? storeA;
      EventStore? storeB;

      try {
        await identity.setSyncWatermark(41);
        await identity.setSubjectHistoryCursor(
          subjectId: 'subj-a',
          activityRef: 'activity-a',
          cursor: 99,
        );

        storeA = EventStore(actorId: actorA, dbDirectory: partitionDir.path);
        final configA = ConfigStore(storeA);
        await configA.init();
        await configA.applyConfig(configPackage(1));
        await configA.applyConfig(configPackage(2));
        expect(configA.configVersion, 1);
        expect(configA.hasPending, isTrue);

        final assemblerA = EventAssembler(identity, storeA);
        final aEvent = await assemblerA.assemble(
          subjectId: 'subj-a',
          shapeRef: 'basic_capture/v1',
          payload: {'name': 'A local'},
        );
        expect(await storeA.unpushedCount(), 1);

        final switchClient = MockClient((request) async {
          if (request.url.path == '/api/auth/me' &&
              request.headers['Authorization'] == 'Bearer production-token') {
            return http.Response(
              jsonEncode({'error': 'invalid_token'}),
              401,
              headers: {'content-type': 'application/json'},
            );
          }
          if (request.url.path == '/api/auth/me' &&
              request.headers['Authorization'] == 'Bearer token-b') {
            return http.Response(
              jsonEncode({'actor_id': actorB}),
              200,
              headers: {'content-type': 'application/json'},
            );
          }
          fail('Unexpected switch request path: ${request.url.path}');
        });

        final switchResult = await SyncService(
          storeA,
          identity,
          'http://server.test',
          configA,
          client: switchClient,
        ).switchActorSession('token-b');

        expect(switchResult.success, isTrue);
        expect(switchResult.actorId, actorB);
        expect(identity.actorId, actorB);
        expect(identity.actorToken, 'token-b');
        expect(identity.syncWatermark, 0);
        expect(
          identity.subjectHistoryCursor(
            subjectId: 'subj-a',
            activityRef: 'activity-a',
          ),
          0,
        );
        await expectLater(
          assemblerA.assemble(
            subjectId: 'subj-a-2',
            shapeRef: 'basic_capture/v1',
            payload: {'name': 'stale A write'},
          ),
          throwsA(isA<StateError>()),
        );

        storeB = EventStore(actorId: actorB, dbDirectory: partitionDir.path);
        final configB = ConfigStore(storeB);
        await configB.init();
        expect(configB.configVersion, 0);
        expect(configB.hasPending, isFalse);
        expect(await storeB.getAll(), isEmpty);
        expect(await storeB.unpushedCount(), 0);
        expect(await ProjectionEngine(storeB).getSubjectList(), isEmpty);

        final bEvent = await EventAssembler(identity, storeB).assemble(
          subjectId: 'subj-b',
          shapeRef: 'basic_capture/v1',
          payload: {'name': 'B local'},
        );

        final bAuthHeaders = <String>[];
        final bClient = MockClient((request) async {
          bAuthHeaders.add(request.headers['Authorization'] ?? '');
          if (request.url.path == '/api/auth/me') {
            return http.Response(
              jsonEncode({'actor_id': actorB}),
              200,
              headers: {'content-type': 'application/json'},
            );
          }

          if (request.url.path == '/api/sync/push') {
            final body = jsonDecode(request.body) as Map<String, dynamic>;
            final events = body['events'] as List<dynamic>;
            expect(body['last_pull_watermark'], 0);
            expect(events, hasLength(1));
            final event = events.single as Map<String, dynamic>;
            expect(event['id'], bEvent.id);
            expect(event['id'], isNot(aEvent.id));
            expect(event['actor_ref'], {'type': 'actor', 'id': actorB});
            return http.Response(
              jsonEncode({'accepted': 1, 'duplicates': 0, 'flags_raised': 0}),
              200,
              headers: {'content-type': 'application/json'},
            );
          }

          if (request.url.path == '/api/sync/pull') {
            return http.Response(
              jsonEncode({
                'events': [
                  serverEvent(
                    id: 'srv-b-1',
                    subjectId: 'subj-b-pulled',
                    actorId: actorB,
                    sequence: 700,
                    watermark: 7,
                  ),
                ],
                'latest_watermark': 7,
                'has_more': false,
                'config_version': 0,
              }),
              200,
              headers: {'content-type': 'application/json'},
            );
          }

          if (request.url.path == '/api/sync/config') {
            return http.Response('', 304);
          }

          fail('Unexpected B request path: ${request.url.path}');
        });

        final bResult = await SyncService(
          storeB,
          identity,
          'http://server.test',
          configB,
          client: bClient,
        ).sync();

        expect(bResult.error, isNull);
        expect(bResult.pushedCount, 1);
        expect(bResult.pulledCount, 1);
        expect(bAuthHeaders, everyElement('Bearer token-b'));
        expect(identity.syncWatermark, 7);
        expect(await storeB.unpushedCount(), 0);
        expect(await storeA.unpushedCount(), 1);
        expect((await storeA.getUnpushed()).single.id, aEvent.id);

        final resumed = await identity.resumeActorSession(actorA);
        expect(resumed, isTrue);
        expect(identity.actorToken, 'production-token');
        expect(identity.syncWatermark, 41);
        expect(configA.hasPending, isTrue);

        final aClient = MockClient((request) async {
          expect(request.headers['Authorization'], 'Bearer production-token');
          if (request.url.path == '/api/auth/me') {
            return http.Response(
              jsonEncode({'actor_id': actorA}),
              200,
              headers: {'content-type': 'application/json'},
            );
          }
          if (request.url.path == '/api/sync/push') {
            final body = jsonDecode(request.body) as Map<String, dynamic>;
            final events = body['events'] as List<dynamic>;
            expect(body['last_pull_watermark'], 41);
            expect(events, hasLength(1));
            final event = events.single as Map<String, dynamic>;
            expect(event['id'], aEvent.id);
            expect(event['actor_ref'], {'type': 'actor', 'id': actorA});
            return http.Response(
              jsonEncode({'accepted': 1, 'duplicates': 0, 'flags_raised': 0}),
              200,
              headers: {'content-type': 'application/json'},
            );
          }
          if (request.url.path == '/api/sync/pull') {
            return http.Response(
              jsonEncode({
                'events': [],
                'latest_watermark': 41,
                'has_more': false,
                'config_version': 1,
              }),
              200,
              headers: {'content-type': 'application/json'},
            );
          }
          if (request.url.path == '/api/sync/config') {
            return http.Response('', 304);
          }
          fail('Unexpected A request path: ${request.url.path}');
        });

        final aResult = await SyncService(
          storeA,
          identity,
          'http://server.test',
          configA,
          client: aClient,
        ).sync();

        expect(aResult.error, isNull);
        expect(aResult.pushedCount, 1);
        expect(await storeA.unpushedCount(), 0);
      } finally {
        await storeA?.close();
        await storeB?.close();
        try {
          partitionDir.deleteSync(recursive: true);
        } catch (_) {}
      }
    },
  );

  test('sync stops if active credential resolves to another actor', () async {
    final assembler = EventAssembler(identity, store);
    await assembler.assemble(
      subjectId: 'subj-42',
      shapeRef: 'basic_capture/v1',
      payload: {'name': 'Test'},
    );

    final paths = <String>[];
    final client = MockClient((request) async {
      paths.add(request.url.path);
      if (request.url.path == '/api/auth/me') {
        return http.Response(
          jsonEncode({'actor_id': actorB}),
          200,
          headers: {'content-type': 'application/json'},
        );
      }
      fail('Unexpected request after actor drift');
    });

    final result = await SyncService(
      store,
      identity,
      'http://server.test',
      configStore,
      client: client,
    ).sync();

    expect(result.error, contains('Actor identity changed'));
    expect(await store.unpushedCount(), 1);
    expect(paths, ['/api/auth/me']);
  });
}
