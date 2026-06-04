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

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late DeviceIdentity identity;
  late ConfigStore configStore;
  late String dbPath;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    identity = await DeviceIdentity.init();
    await identity.setActorToken('production-token');
    await identity.setActorId('11111111-1111-1111-1111-111111111111');
    dbPath =
        '${Directory.systemTemp.path}/datarun_sync_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath);
    configStore = ConfigStore(store);
    await configStore.init();
  });

  tearDown(() async {
    await store.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
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
          jsonEncode({
            'actor_id': '11111111-1111-1111-1111-111111111111',
            'auth_source': 'jwt-principal',
          }),
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
          'id': '11111111-1111-1111-1111-111111111111',
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
}
