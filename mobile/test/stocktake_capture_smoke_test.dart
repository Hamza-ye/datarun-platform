import 'dart:convert';
import 'dart:io';

import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  const stockWorkerActorId = '15000000-0000-4000-8000-000000000002';
  const pilotStockScopeSubjectId = '15200000-0000-4000-8000-000000000001';
  const activityRef = 'stock_operations';
  const shapeRef = 'stocktake_line/v1';

  late EventStore store;
  late DeviceIdentity identity;
  late ConfigStore configStore;
  late String dbPath;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    identity = await DeviceIdentity.init();
    await identity.activateActorSession(
      actorId: stockWorkerActorId,
      token: 'stock-worker-token',
    );
    dbPath =
        '${Directory.systemTemp.path}/datarun_stocktake_${DateTime.now().microsecondsSinceEpoch}.db';
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

  test(
    'field worker captures stocktake line for pre-established subject and syncs',
    () async {
      final config = stockOperationsConfigPackage();
      await configStore.applyConfig(config);

      final stocktakeShape = configStore.getShape(shapeRef);
      expect(stocktakeShape, isNotNull);
      expect(stocktakeShape!.activeFields.map((field) => field.name), [
        'stocktake_date',
        'stock_category',
        'quantity',
      ]);
      expect(
        configStore.getShapesForActivity(activityRef).single.shapeRef,
        shapeRef,
      );
      expect(
        configStore
            .getActivityRoleActions(activityRef)
            .rolePermits('field_worker', ActivityAction.capture),
        isTrue,
      );

      final event = await EventAssembler(identity, store).assemble(
        subjectId: pilotStockScopeSubjectId,
        shapeRef: shapeRef,
        activityRef: activityRef,
        payload: {
          'stocktake_date': '2026-06-23',
          'stock_category': 'mids_kit',
          'quantity': 42,
        },
      );

      expect(event.subjectRef, {
        'type': 'subject',
        'id': pilotStockScopeSubjectId,
      });
      expect(event.payload.containsKey('subject_ref'), isFalse);

      final pending = await store.getUnpushed();
      expect(pending, hasLength(1));
      expect(pending.single.id, event.id);
      expect(pending.single.shapeRef, shapeRef);
      expect(pending.single.activityRef, activityRef);
      expect(pending.single.subjectRef['id'], pilotStockScopeSubjectId);
      expect(await store.unpushedCount(), 1);

      final paths = <String>[];
      final client = MockClient((request) async {
        paths.add(request.url.path);
        expect(request.headers['Authorization'], 'Bearer stock-worker-token');

        if (request.url.path == '/api/auth/me') {
          return http.Response(
            jsonEncode({'actor_id': stockWorkerActorId}),
            200,
            headers: {'content-type': 'application/json'},
          );
        }

        if (request.url.path == '/api/sync/push') {
          final body = jsonDecode(request.body) as Map<String, dynamic>;
          expect(body['device_id'], identity.deviceId);
          expect(body['last_pull_watermark'], 0);

          final events = body['events'] as List<dynamic>;
          expect(events, hasLength(1));
          final pushed = events.single as Map<String, dynamic>;
          expect(pushed['id'], event.id);
          expect(pushed['type'], 'capture');
          expect(pushed['shape_ref'], shapeRef);
          expect(pushed['activity_ref'], activityRef);
          expect(pushed['subject_ref'], {
            'type': 'subject',
            'id': pilotStockScopeSubjectId,
          });
          expect(pushed['actor_ref'], {
            'type': 'actor',
            'id': stockWorkerActorId,
          });
          expect(pushed['payload'], {
            'stocktake_date': '2026-06-23',
            'stock_category': 'mids_kit',
            'quantity': 42,
          });
          expect(
            (pushed['payload'] as Map).containsKey('subject_ref'),
            isFalse,
          );

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
              'config_version': 1,
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
    },
  );
}

Map<String, dynamic> stockOperationsConfigPackage() {
  final manifest =
      jsonDecode(
            File(
              '../deploy/reference/pilot-packages/stock-operations/reviewed-config.json',
            ).readAsStringSync(),
          )
          as Map<String, dynamic>;
  final shape = (manifest['shapes'] as List<dynamic>)
      .cast<Map<String, dynamic>>()
      .singleWhere(
        (entry) => entry['name'] == 'stocktake_line' && entry['version'] == 1,
      );
  final schema = Map<String, dynamic>.from(shape['schema_json'] as Map);
  expect(schema['subject_binding'], isNull);

  final activity = (manifest['activities'] as List<dynamic>)
      .cast<Map<String, dynamic>>()
      .singleWhere((entry) => entry['name'] == 'stock_operations');
  final activityConfig = Map<String, dynamic>.from(
    activity['config_json'] as Map,
  );
  expect(activityConfig['roles'], {
    'field_worker': ['capture'],
  });

  return {
    'version': 1,
    'shapes': {
      'stocktake_line/v1': {
        'name': shape['name'],
        'version': shape['version'],
        'status': shape['status'],
        'sensitivity': shape['sensitivity'],
        'fields': schema['fields'],
        'uniqueness': schema['uniqueness'],
      },
    },
    'activities': {
      'stock_operations': {
        'name': activity['name'],
        'status': activity['status'],
        'sensitivity': activity['sensitivity'],
        'shapes': activityConfig['shapes'],
        'roles': activityConfig['roles'],
      },
    },
    'expressions': <String, dynamic>{},
    'flag_severity_overrides': <String, dynamic>{},
    'sensitivity_classifications': {
      'shapes': {'stocktake_line/v1': shape['sensitivity']},
      'activities': {'stock_operations': activity['sensitivity']},
    },
  };
}
