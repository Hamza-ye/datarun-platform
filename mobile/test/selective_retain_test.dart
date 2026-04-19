import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/event.dart';

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late String dbPath;
  const ownDevice = 'own-device-1';
  const otherDevice = 'other-device-2';
  var seq = 1;

  setUp(() async {
    seq = 1;
    dbPath =
        '${Directory.systemTemp.path}/datarun_test_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath);
  });

  tearDown(() async {
    await store.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  Event makeCapture(String subjectId, String deviceId, {String? id}) {
    return Event(
      id: id ?? 'evt-${seq}',
      type: 'capture',
      shapeRef: 'basic_capture/v1',
      subjectRef: {'type': 'subject', 'id': subjectId},
      actorRef: {'type': 'actor', 'id': 'actor-1'},
      deviceId: deviceId,
      deviceSeq: seq++,
      syncWatermark: seq,
      timestamp: '2026-04-19T10:00:00Z',
      payload: {'name': 'Test'},
    );
  }

  Event makeAssignmentCreated(String assignmentId,
      {required List<String> subjectList}) {
    return Event(
      id: 'asgn-evt-$assignmentId',
      type: 'assignment_changed',
      shapeRef: 'assignment_created/v1',
      subjectRef: {'type': 'assignment', 'id': assignmentId},
      actorRef: {'type': 'actor', 'id': 'admin-1'},
      deviceId: 'admin-device',
      deviceSeq: seq++,
      syncWatermark: seq,
      timestamp: '2026-04-19T09:00:00Z',
      payload: {
        'target_actor': {'type': 'actor', 'id': 'actor-1'},
        'role': 'field_worker',
        'scope': {
          'subject_list': subjectList,
        },
        'valid_from': '2026-04-18T00:00:00Z',
      },
    );
  }

  group('purgeOutOfScopeEvents', () {
    test('no assignments → keeps all events', () async {
      await store.insert(makeCapture('subj-1', otherDevice, id: 'e1'));
      await store.insert(makeCapture('subj-2', otherDevice, id: 'e2'));

      final purged = await store.purgeOutOfScopeEvents(ownDevice);

      expect(purged, 0);
      final remaining = await store.getAll();
      expect(remaining.length, 2);
    });

    test('own-device events are never purged', () async {
      final assignment =
          makeAssignmentCreated('a1', subjectList: ['subj-1']);
      await store.insert(assignment);
      await store.processAssignmentEvent(assignment);

      // Own device event for out-of-scope subject
      await store.insert(makeCapture('subj-out', ownDevice, id: 'e1'));

      final purged = await store.purgeOutOfScopeEvents(ownDevice);

      expect(purged, 0);
    });

    test('out-of-scope other-device events are purged', () async {
      final assignment =
          makeAssignmentCreated('a1', subjectList: ['subj-1', 'subj-2']);
      await store.insert(assignment);
      await store.processAssignmentEvent(assignment);

      // In-scope events from other device
      await store.insert(makeCapture('subj-1', otherDevice, id: 'e1'));
      await store.insert(makeCapture('subj-2', otherDevice, id: 'e2'));
      // Out-of-scope event from other device
      await store.insert(makeCapture('subj-out', otherDevice, id: 'e3'));

      final purged = await store.purgeOutOfScopeEvents(ownDevice);

      expect(purged, 1);
      final remaining = await store.getAll();
      // Assignment event + 2 in-scope captures remain
      final remainingIds = remaining.map((e) => e.id).toSet();
      expect(remainingIds, contains('e1'));
      expect(remainingIds, contains('e2'));
      expect(remainingIds, isNot(contains('e3')));
    });

    test('system events are never purged', () async {
      final assignment =
          makeAssignmentCreated('a1', subjectList: ['subj-1']);
      await store.insert(assignment);
      await store.processAssignmentEvent(assignment);

      // System event (conflict_detected) from other device for out-of-scope subject
      final systemEvent = Event(
        id: 'sys-1',
        type: 'conflict_detected',
        shapeRef: 'conflict/v1',
        subjectRef: {'type': 'subject', 'id': 'subj-out'},
        actorRef: {'type': 'actor', 'id': 'system'},
        deviceId: otherDevice,
        deviceSeq: seq++,
        syncWatermark: seq,
        timestamp: '2026-04-19T10:00:00Z',
        payload: {'category': 'scope_violation'},
      );
      await store.insert(systemEvent);

      final purged = await store.purgeOutOfScopeEvents(ownDevice);

      expect(purged, 0);
    });
  });
}
