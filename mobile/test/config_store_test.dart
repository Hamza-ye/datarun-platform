import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/config_store.dart';

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore eventStore;
  late ConfigStore configStore;
  late String dbPath;

  final sampleConfig = <String, dynamic>{
    'version': 1,
    'shapes': {
      'household_visit/v1': {
        'name': 'household_visit',
        'version': 1,
        'status': 'active',
        'sensitivity': 'standard',
        'fields': [
          {
            'name': 'head_of_household',
            'type': 'text',
            'required': true,
            'description': 'Head of Household',
            'display_order': 1,
            'group': null,
            'deprecated': false,
            'options': null,
          },
          {
            'name': 'members_count',
            'type': 'integer',
            'required': true,
            'description': 'Number of Members',
            'display_order': 2,
            'group': null,
            'deprecated': false,
            'options': null,
          },
          {
            'name': 'visit_type',
            'type': 'select',
            'required': true,
            'description': 'Visit Type',
            'display_order': 3,
            'group': null,
            'deprecated': false,
            'options': ['initial', 'follow_up', 'referral'],
          },
          {
            'name': 'old_field',
            'type': 'text',
            'required': false,
            'description': 'Deprecated Field',
            'display_order': 99,
            'group': null,
            'deprecated': true,
            'options': null,
          },
        ],
      },
      'child_check/v1': {
        'name': 'child_check',
        'version': 1,
        'status': 'active',
        'sensitivity': 'sensitive',
        'fields': [
          {
            'name': 'child_name',
            'type': 'text',
            'required': true,
            'description': 'Child Name',
            'display_order': 1,
          },
        ],
      },
    },
    'activities': {
      'monitoring': {
        'name': 'monitoring',
        'shapes': ['household_visit/v1'],
        'roles': {'field_worker': ['capture']},
        'status': 'active',
      },
      'child_health': {
        'name': 'child_health',
        'shapes': ['child_check/v1', 'household_visit/v1'],
        'roles': {'nurse': ['capture']},
        'status': 'active',
      },
      'archived_program': {
        'name': 'archived_program',
        'shapes': ['household_visit/v1'],
        'roles': {},
        'status': 'deprecated',
      },
    },
    'expressions': {},
    'flag_severity_overrides': {},
    'sensitivity_classifications': {'shapes': {}, 'activities': {}},
    'published_at': '2026-04-20T10:00:00Z',
  };

  setUp(() async {
    dbPath =
        '${Directory.systemTemp.path}/datarun_config_test_${DateTime.now().microsecondsSinceEpoch}.db';
    eventStore = EventStore(dbPath: dbPath);
    // Force DB init
    await eventStore.database;
    configStore = ConfigStore(eventStore);
  });

  tearDown(() async {
    await eventStore.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  test('configVersion returns 0 before any config applied', () {
    expect(configStore.configVersion, 0);
  });

  test('applyConfig parses shapes and activities correctly', () async {
    await configStore.applyConfig(sampleConfig);

    expect(configStore.configVersion, 1);
    expect(configStore.allShapes.length, 2);

    final hv = configStore.getShape('household_visit/v1');
    expect(hv, isNotNull);
    expect(hv!.name, 'household_visit');
    expect(hv.version, 1);
    expect(hv.status, 'active');
    expect(hv.sensitivity, 'standard');
    expect(hv.fields.length, 4);
    // Active fields should exclude deprecated
    expect(hv.activeFields.length, 3);
    // Active fields sorted by display_order
    expect(hv.activeFields.first.name, 'head_of_household');

    final visitTypeField = hv.fields.firstWhere((f) => f.name == 'visit_type');
    expect(visitTypeField.type, 'select');
    expect(visitTypeField.options, ['initial', 'follow_up', 'referral']);
    expect(visitTypeField.label, 'Visit Type');
  });

  test('getShape with valid ref returns ShapeDefinition', () async {
    await configStore.applyConfig(sampleConfig);

    final shape = configStore.getShape('child_check/v1');
    expect(shape, isNotNull);
    expect(shape!.name, 'child_check');
    expect(shape.shapeRef, 'child_check/v1');
    expect(shape.fields.length, 1);
    expect(shape.fields.first.name, 'child_name');
  });

  test('getShape with unknown ref returns null', () async {
    await configStore.applyConfig(sampleConfig);
    expect(configStore.getShape('nonexistent/v1'), isNull);
  });

  test('getShapesForActivity returns correct shapes', () async {
    await configStore.applyConfig(sampleConfig);

    final monitoringShapes = configStore.getShapesForActivity('monitoring');
    expect(monitoringShapes.length, 1);
    expect(monitoringShapes.first.shapeRef, 'household_visit/v1');

    final childHealthShapes = configStore.getShapesForActivity('child_health');
    expect(childHealthShapes.length, 2);
    final refs = childHealthShapes.map((s) => s.shapeRef).toSet();
    expect(refs, contains('child_check/v1'));
    expect(refs, contains('household_visit/v1'));
  });

  test('getActiveActivities excludes deprecated', () async {
    await configStore.applyConfig(sampleConfig);

    final active = configStore.getActiveActivities();
    expect(active, contains('monitoring'));
    expect(active, contains('child_health'));
    expect(active, isNot(contains('archived_program')));
  });

  test('init loads persisted config from SQLite', () async {
    // First: apply and persist
    await configStore.applyConfig(sampleConfig);
    expect(configStore.configVersion, 1);

    // Create a new ConfigStore pointing at same DB — simulates app restart
    final configStore2 = ConfigStore(eventStore);
    expect(configStore2.configVersion, 0); // not yet loaded
    await configStore2.init();
    expect(configStore2.configVersion, 1);
    expect(configStore2.getShape('household_visit/v1'), isNotNull);
    expect(configStore2.getActiveActivities(), contains('monitoring'));
  });

  group('expression storage and retrieval', () {
    final configWithExpressions = <String, dynamic>{
      ...sampleConfig,
      'expressions': {
        'monitoring.household_visit/v1': [
          {
            'field_name': 'followup_notes',
            'rule_type': 'show_condition',
            'expression': {'eq': ['payload.needs_followup', true]},
            'message': null,
          },
          {
            'field_name': 'members_count',
            'rule_type': 'warning',
            'expression': {'gt': ['payload.members_count', 20]},
            'message': 'Unusually large household — please verify',
          },
          {
            'field_name': 'visit_type',
            'rule_type': 'default',
            'expression': {'ref': 'context.default_visit_type'},
            'message': null,
          },
        ],
      },
    };

    test('expressions parsed and accessible via getExpressionsForField', () async {
      await configStore.applyConfig(configWithExpressions);

      final rules = configStore.getExpressionsForField(
          'monitoring', 'household_visit/v1', 'followup_notes');
      expect(rules.length, 1);
      expect(rules.first['rule_type'], 'show_condition');
    });

    test('getShowCondition returns correct expression', () async {
      await configStore.applyConfig(configWithExpressions);

      final expr = configStore.getShowCondition(
          'monitoring', 'household_visit/v1', 'followup_notes');
      expect(expr, isNotNull);
      expect(expr!['eq'], equals(['payload.needs_followup', true]));
    });

    test('getWarningExpression returns correct expression and message', () async {
      await configStore.applyConfig(configWithExpressions);

      final expr = configStore.getWarningExpression(
          'monitoring', 'household_visit/v1', 'members_count');
      expect(expr, isNotNull);
      expect(expr!['gt'], equals(['payload.members_count', 20]));

      final msg = configStore.getWarningMessage(
          'monitoring', 'household_visit/v1', 'members_count');
      expect(msg, 'Unusually large household — please verify');
    });

    test('getDefaultExpression returns correct expression', () async {
      await configStore.applyConfig(configWithExpressions);

      final expr = configStore.getDefaultExpression(
          'monitoring', 'household_visit/v1', 'visit_type');
      expect(expr, isNotNull);
      expect(expr!['ref'], 'context.default_visit_type');
    });

    test('getShowCondition returns null for field without expressions', () async {
      await configStore.applyConfig(configWithExpressions);

      final expr = configStore.getShowCondition(
          'monitoring', 'household_visit/v1', 'head_of_household');
      expect(expr, isNull);
    });
  });

  // --- Phase 3c: Two-slot config model (IDR-019) ---

  group('two-slot config model', () {
    final configV2 = <String, dynamic>{
      'version': 2,
      'shapes': {
        'household_visit/v1': {
          'name': 'household_visit',
          'version': 1,
          'status': 'active',
          'sensitivity': 'standard',
          'fields': [
            {
              'name': 'head_of_household',
              'type': 'text',
              'required': true,
              'description': 'Head of Household',
              'display_order': 1,
              'deprecated': false,
            },
          ],
        },
        'malaria_followup/v1': {
          'name': 'malaria_followup',
          'version': 1,
          'status': 'active',
          'sensitivity': 'elevated',
          'fields': [
            {
              'name': 'patient_name',
              'type': 'text',
              'required': true,
              'description': 'Patient Name',
              'display_order': 1,
              'deprecated': false,
            },
          ],
        },
      },
      'activities': {
        'malaria_program': {
          'name': 'malaria_program',
          'shapes': ['malaria_followup/v1'],
          'roles': {'field_worker': ['capture']},
          'status': 'active',
        },
      },
      'expressions': {},
      'flag_severity_overrides': {},
      'sensitivity_classifications': {'shapes': {}, 'activities': {}},
      'published_at': '2026-04-20T11:00:00Z',
    };

    test('first config promotes immediately when no current exists', () async {
      expect(configStore.configVersion, 0);
      await configStore.applyConfig(sampleConfig);
      // Should be current immediately (no pending)
      expect(configStore.configVersion, 1);
      expect(configStore.hasPending, false);
      expect(configStore.getShape('household_visit/v1'), isNotNull);
    });

    test('second config goes to pending, not current', () async {
      await configStore.applyConfig(sampleConfig);
      expect(configStore.configVersion, 1);

      await configStore.applyConfig(configV2);
      // Current should still be v1
      expect(configStore.configVersion, 1);
      expect(configStore.hasPending, true);
      // v1 shapes still active
      expect(configStore.getShape('household_visit/v1')!.fields.length, 4);
      // v2 shape not yet visible
      expect(configStore.getShape('malaria_followup/v1'), isNull);
    });

    test('promotePending moves pending to current', () async {
      await configStore.applyConfig(sampleConfig);
      await configStore.applyConfig(configV2);
      expect(configStore.configVersion, 1);
      expect(configStore.hasPending, true);

      await configStore.promotePending();

      expect(configStore.configVersion, 2);
      expect(configStore.hasPending, false);
      // v2 shape now visible
      expect(configStore.getShape('malaria_followup/v1'), isNotNull);
      // v2 only has 1 field for household_visit (simplified in v2)
      expect(configStore.getShape('household_visit/v1')!.fields.length, 1);
    });

    test('promotePending is no-op when no pending', () async {
      await configStore.applyConfig(sampleConfig);
      expect(configStore.configVersion, 1);
      expect(configStore.hasPending, false);

      await configStore.promotePending();

      expect(configStore.configVersion, 1);
    });

    test('at-most-2: new pending overwrites previous pending', () async {
      await configStore.applyConfig(sampleConfig); // v1 → current

      await configStore.applyConfig(configV2); // v2 → pending
      expect(configStore.hasPending, true);

      final configV3 = Map<String, dynamic>.from(configV2);
      configV3['version'] = 3;
      await configStore.applyConfig(configV3); // v3 replaces v2 as pending

      expect(configStore.configVersion, 1); // current unchanged
      await configStore.promotePending();
      expect(configStore.configVersion, 3); // v3 promoted, v2 was overwritten
    });

    test('pending survives app restart via init', () async {
      await configStore.applyConfig(sampleConfig); // v1 → current
      await configStore.applyConfig(configV2); // v2 → pending

      // Create a fresh ConfigStore pointing at same DB (simulate restart)
      final configStore2 = ConfigStore(eventStore);
      await configStore2.init();

      expect(configStore2.configVersion, 1); // current loaded
      expect(configStore2.hasPending, true); // pending loaded

      await configStore2.promotePending();
      expect(configStore2.configVersion, 2);
      expect(configStore2.getShape('malaria_followup/v1'), isNotNull);
    });

    test('pending persisted in SQLite and cleared after promotion', () async {
      await configStore.applyConfig(sampleConfig);
      await configStore.applyConfig(configV2);

      // Verify pending exists in DB
      final pending = await eventStore.getPendingConfigPackage();
      expect(pending, isNotNull);
      expect(pending!['version'], 2);

      await configStore.promotePending();

      // Pending cleared from DB
      final pendingAfter = await eventStore.getPendingConfigPackage();
      expect(pendingAfter, isNull);

      // Current updated in DB
      final current = await eventStore.getConfigPackage();
      expect(current, isNotNull);
      expect(current!['version'], 2);
    });
  });
}
