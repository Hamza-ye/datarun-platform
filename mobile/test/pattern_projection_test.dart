import 'dart:convert';
import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/pattern_projection.dart';
import 'package:datarun_mobile/domain/event.dart';

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore eventStore;
  late ConfigStore configStore;
  late PatternProjectionEngine projection;
  late String dbPath;

  setUp(() async {
    dbPath =
        '${Directory.systemTemp.path}/datarun_pattern_${DateTime.now().microsecondsSinceEpoch}.db';
    eventStore = EventStore(dbPath: dbPath);
    configStore = ConfigStore(eventStore);
    projection = PatternProjectionEngine(eventStore, configStore);
  });

  tearDown(() async {
    await eventStore.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  test('Phase 4.5 pattern projection matches shared fixture', () async {
    final fixtureFile = File(
      '${Directory.current.path}/../contracts/fixtures/pattern-state-projection.json',
    );
    expect(
      fixtureFile.existsSync(),
      isTrue,
      reason:
          'Fixture file must exist at contracts/fixtures/pattern-state-projection.json',
    );
    final fixture =
        jsonDecode(fixtureFile.readAsStringSync()) as Map<String, dynamic>;

    await configStore.applyConfig(
      Map<String, dynamic>.from(fixture['config_package'] as Map),
    );

    for (final raw in fixture['events'] as List<dynamic>) {
      final e = Map<String, dynamic>.from(raw as Map);
      await eventStore.insertFromServer(
        Event(
          id: e['id'] as String,
          type: e['type'] as String,
          shapeRef: e['shape_ref'] as String,
          activityRef: e['activity_ref'] as String?,
          subjectRef: Map<String, String>.from(e['subject_ref'] as Map),
          actorRef: Map<String, String>.from(e['actor_ref'] as Map),
          deviceId: e['device_id'] as String,
          deviceSeq: e['device_seq'] as int,
          syncWatermark: e['sync_watermark'] as int,
          timestamp: e['timestamp'] as String,
          payload: Map<String, dynamic>.from(e['payload'] as Map),
        ),
      );
    }

    final states = await projection.projectCurrent(
      asOf: DateTime.parse(fixture['as_of'] as String),
    );
    final actual = states.map((state) => state.toJson()).toList();
    final expected =
        (fixture['expected_output']['pattern_states'] as List<dynamic>)
            .cast<Map<String, dynamic>>();

    expect(actual, expected);
  });
}
