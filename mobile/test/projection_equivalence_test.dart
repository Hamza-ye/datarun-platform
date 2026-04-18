import 'dart:convert';
import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/domain/event.dart';

/// Projection equivalence test (E7): Dart PE must produce identical
/// canonical output to Java PE given the same ordered event set.
///
/// Loads shared fixture from contracts/fixtures/projection-equivalence.json.
void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late ProjectionEngine pe;
  late String dbPath;

  setUp(() async {
    dbPath =
        '${Directory.systemTemp.path}/datarun_equiv_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath);
    pe = ProjectionEngine(store);
  });

  tearDown(() async {
    await store.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  test('E7: projection equivalence — Dart PE matches shared fixture', () async {
    // Load the shared fixture
    final fixtureFile = File('${Directory.current.path}/../contracts/fixtures/projection-equivalence.json');
    expect(fixtureFile.existsSync(), isTrue,
        reason: 'Fixture file must exist at contracts/fixtures/projection-equivalence.json');
    final fixture = jsonDecode(fixtureFile.readAsStringSync()) as Map<String, dynamic>;

    // Insert events
    final events = fixture['events'] as List<dynamic>;
    for (final eventJson in events) {
      final e = eventJson as Map<String, dynamic>;
      final event = Event(
        id: e['id'] as String,
        type: e['type'] as String,
        shapeRef: e['shape_ref'] as String,
        activityRef: e['activity_ref'] as String?,
        subjectRef: Map<String, String>.from(e['subject_ref'] as Map),
        actorRef: Map<String, String>.from(e['actor_ref'] as Map),
        deviceId: e['device_id'] as String,
        deviceSeq: e['device_seq'] as int,
        syncWatermark: e['sync_watermark'] as int?,
        timestamp: e['timestamp'] as String,
        payload: Map<String, dynamic>.from(e['payload'] as Map),
      );
      await store.insert(event);
    }

    // Insert aliases
    final aliases = fixture['aliases'] as List<dynamic>;
    for (final alias in aliases) {
      final a = alias as Map<String, dynamic>;
      await store.upsertAlias(
        a['retired_id'] as String,
        a['surviving_id'] as String,
        DateTime.now().toUtc().toIso8601String(),
      );
    }

    // Run Dart PE
    final subjects = await pe.getSubjectList();

    // Compare to expected output
    final expectedSubjects =
        (fixture['expected_output']['subjects'] as List<dynamic>)
            .cast<Map<String, dynamic>>();

    expect(subjects.length, equals(expectedSubjects.length),
        reason: 'Subject count mismatch');

    for (var i = 0; i < expectedSubjects.length; i++) {
      final expected = expectedSubjects[i];
      final actual = subjects[i];

      expect(actual.subjectId, equals(expected['subject_id']),
          reason: 'subject[$i].subject_id');
      expect(actual.captureCount, equals(expected['event_count']),
          reason: 'subject[$i].event_count');
      expect(actual.flagCount, equals(expected['flag_count']),
          reason: 'subject[$i].flag_count');

      // Compare timestamps as instants
      final expectedTs =
          DateTime.parse(expected['latest_timestamp'] as String);
      final actualTs = DateTime.parse(actual.latestTimestamp);
      expect(actualTs.toUtc(), equals(expectedTs.toUtc()),
          reason: 'subject[$i].latest_timestamp');
    }
  });
}
