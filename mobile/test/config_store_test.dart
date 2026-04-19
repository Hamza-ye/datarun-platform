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
}
