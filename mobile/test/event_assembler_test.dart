import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';

/// Phase 3d.1: verify activity_ref auto-population on assembled events.
/// Closes audit item D2 — Phase 3a quality gate previously passed by inspection.
void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late DeviceIdentity identity;
  late EventAssembler assembler;
  late String dbPath;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    identity = await DeviceIdentity.init();
    dbPath =
        '${Directory.systemTemp.path}/datarun_asm_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath);
    assembler = EventAssembler(identity, store);
  });

  tearDown(() async {
    await store.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  group('EventAssembler activity_ref', () {
    test('populates activity_ref when form opened from activity context',
        () async {
      final event = await assembler.assemble(
        subjectId: null,
        shapeRef: 'household_visit/v1',
        payload: {'name': 'Test'},
        activityRef: 'household_monitoring',
      );

      expect(event.activityRef, 'household_monitoring');
      // Sanity-check envelope is otherwise well-formed
      expect(event.shapeRef, 'household_visit/v1');
      expect(event.subjectRef['type'], 'subject');
    });

    test('activity_ref null when form opened outside any activity', () async {
      final event = await assembler.assemble(
        subjectId: null,
        shapeRef: 'basic_capture/v1',
        payload: {'name': 'Test'},
      );

      expect(event.activityRef, isNull);
    });

    test('persisted event round-trips activity_ref through SQLite', () async {
      final assembled = await assembler.assemble(
        subjectId: 'subj-42',
        shapeRef: 'household_visit/v1',
        payload: {'name': 'Test'},
        activityRef: 'household_monitoring',
      );

      final stored = (await store.getAll())
          .firstWhere((e) => e.id == assembled.id);

      expect(stored.activityRef, 'household_monitoring');
    });
  });
}
