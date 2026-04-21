import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/domain/event.dart';

void main() {
  // Use FFI for desktop sqflite support in tests
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late ProjectionEngine pe;
  late String dbPath;
  var serverSeq = 100;

  setUp(() async {
    serverSeq = 100;
    dbPath = '${Directory.systemTemp.path}/datarun_test_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath);
    pe = ProjectionEngine(store);
  });

  tearDown(() async {
    await store.close();
    try { File(dbPath).deleteSync(); } catch (_) {}
  });

  Event makeCapture(String id, String subjectId,
      {String deviceId = 'dev-1',
      int seq = 1,
      String name = 'Alice',
      String timestamp = '2026-04-18T10:00:00Z'}) {
    return Event(
      id: id,
      type: 'capture',
      shapeRef: 'basic_capture/v1',
      subjectRef: {'type': 'subject', 'id': subjectId},
      actorRef: {'type': 'actor', 'id': 'actor-1'},
      deviceId: deviceId,
      deviceSeq: seq,
      syncWatermark: null,
      timestamp: timestamp,
      payload: {'name': name},
    );
  }

  Event makeFlag(String flagId, String sourceEventId, String subjectId,
      {String category = 'concurrent_state_change',
      String timestamp = '2026-04-18T11:00:00Z'}) {
    return Event(
      id: flagId,
      type: 'alert',
      shapeRef: 'conflict_detected/v1',
      subjectRef: {'type': 'subject', 'id': subjectId},
      // F-A3 system actor convention.
      actorRef: {'type': 'actor', 'id': 'system:conflict_detector/$category'},
      deviceId: 'server',
      deviceSeq: serverSeq++,
      syncWatermark: 50,
      timestamp: timestamp,
      payload: {
        'source_event_id': sourceEventId,
        'flag_category': category,
        'reason': 'test flag',
      },
    );
  }

  Event makeResolution(String id, String flagEventId, String sourceEventId,
      String subjectId, String resolution,
      {String timestamp = '2026-04-18T12:00:00Z'}) {
    return Event(
      id: id,
      type: 'review',
      shapeRef: 'conflict_resolved/v1',
      subjectRef: {'type': 'subject', 'id': subjectId},
      actorRef: {'type': 'actor', 'id': 'admin-1'},
      deviceId: 'server',
      deviceSeq: serverSeq++,
      syncWatermark: 60,
      timestamp: timestamp,
      payload: {
        'flag_event_id': flagEventId,
        'source_event_id': sourceEventId,
        'resolution': resolution,
        'reason': 'test resolution',
      },
    );
  }

  Event makeMerge(String id, String retiredId, String survivingId,
      {String timestamp = '2026-04-18T11:00:00Z'}) {
    return Event(
      id: id,
      type: 'capture',
      shapeRef: 'subjects_merged/v1',
      subjectRef: {'type': 'subject', 'id': survivingId},
      actorRef: {'type': 'actor', 'id': 'admin-1'},
      deviceId: 'server',
      deviceSeq: serverSeq++,
      syncWatermark: 70,
      timestamp: timestamp,
      payload: {
        'surviving_id': survivingId,
        'retired_id': retiredId,
      },
    );
  }

  group('ProjectionEngine', () {
    test('basic subject list from captures', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-2', name: 'Bob',
          seq: 2, timestamp: '2026-04-18T10:05:00Z'));

      final subjects = await pe.getSubjectList();

      expect(subjects, hasLength(2));
      // Sorted DESC by timestamp — Bob is more recent
      expect(subjects[0].name, 'Bob');
      expect(subjects[1].name, 'Alice');
      expect(subjects[0].captureCount, 1);
      expect(subjects[0].flagCount, 0);
    });

    test('flag exclusion: flagged event excluded from state', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-1',
          name: 'Alice Updated',
          deviceId: 'dev-2',
          seq: 2,
          timestamp: '2026-04-18T10:05:00Z'));
      // Flag e2
      await store.insertFromServer(makeFlag('f1', 'e2', 'subj-1'));

      final subjects = await pe.getSubjectList();

      expect(subjects, hasLength(1));
      expect(subjects[0].name, 'Alice'); // Not 'Alice Updated' — e2 is flagged
      expect(subjects[0].captureCount, 1); // Only e1 counted
      expect(subjects[0].flagCount, 1); // 1 unresolved flag
    });

    test('flag resolution (accepted): event re-included in state', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-1',
          name: 'Alice Updated',
          deviceId: 'dev-2',
          seq: 2,
          timestamp: '2026-04-18T10:05:00Z'));
      await store.insertFromServer(makeFlag('f1', 'e2', 'subj-1'));
      // Resolve: accepted → e2 back in state
      await store.insertFromServer(
          makeResolution('r1', 'f1', 'e2', 'subj-1', 'accepted'));

      final subjects = await pe.getSubjectList();

      expect(subjects, hasLength(1));
      expect(subjects[0].captureCount, 2); // Both e1 and e2 now in state
      expect(subjects[0].flagCount, 0); // Flag resolved
    });

    test('flag resolution (rejected): event stays excluded', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-1',
          name: 'Alice Updated',
          deviceId: 'dev-2',
          seq: 2,
          timestamp: '2026-04-18T10:05:00Z'));
      await store.insertFromServer(makeFlag('f1', 'e2', 'subj-1'));
      // Resolve: rejected → e2 stays excluded
      await store.insertFromServer(
          makeResolution('r1', 'f1', 'e2', 'subj-1', 'rejected'));

      final subjects = await pe.getSubjectList();

      expect(subjects, hasLength(1));
      expect(subjects[0].captureCount, 1); // Only e1 — e2 rejected
      expect(subjects[0].flagCount, 0); // Flag resolved (even if rejected)
    });

    test('alias resolution: merged subjects shown as one', () async {
      await store.insert(makeCapture('e1', 'subj-A', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-B',
          name: 'Alice Copy', seq: 2, timestamp: '2026-04-18T10:05:00Z'));
      // Merge B into A
      await store.insertFromServer(makeMerge('m1', 'subj-B', 'subj-A'));
      await store.upsertAlias('subj-B', 'subj-A', '2026-04-18T11:00:00Z');

      final subjects = await pe.getSubjectList();

      expect(subjects, hasLength(1)); // Merged into one
      expect(subjects[0].subjectId, 'subj-A');
      expect(subjects[0].captureCount, 2); // Both captures counted
    });

    test('alias transitive closure: A→B→C resolves to C', () async {
      await store.insert(makeCapture('e1', 'subj-A', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-B',
          name: 'Alice B', seq: 2, timestamp: '2026-04-18T10:05:00Z'));
      await store.insert(makeCapture('e3', 'subj-C',
          name: 'Alice C', seq: 3, timestamp: '2026-04-18T10:10:00Z'));

      // Merge A → B, then B → C (with transitive closure update)
      await store.upsertAlias('subj-A', 'subj-B', '2026-04-18T11:00:00Z');
      await store.upsertAlias('subj-B', 'subj-C', '2026-04-18T11:05:00Z');

      final aliases = await store.getAllAliases();
      expect(aliases['subj-A'], 'subj-C'); // Transitive closure
      expect(aliases['subj-B'], 'subj-C');

      final subjects = await pe.getSubjectList();
      expect(subjects, hasLength(1));
      expect(subjects[0].subjectId, 'subj-C');
      expect(subjects[0].captureCount, 3);
    });

    test('subject detail includes events from retired aliases', () async {
      await store.insert(makeCapture('e1', 'subj-A', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-B',
          name: 'Alice Copy', seq: 2, timestamp: '2026-04-18T10:05:00Z'));
      await store.upsertAlias('subj-B', 'subj-A', '2026-04-18T11:00:00Z');

      // Detail for surviving subject should include events from retired alias
      final events = await pe.getSubjectDetail('subj-A');
      expect(events, hasLength(2));
    });

    test('getFlaggedEventIds returns only unresolved flags', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      await store.insert(makeCapture('e2', 'subj-1',
          name: 'Updated', seq: 2, timestamp: '2026-04-18T10:05:00Z'));
      await store.insert(makeCapture('e3', 'subj-1',
          name: 'Updated2', seq: 3, timestamp: '2026-04-18T10:10:00Z'));

      // Flag e2 and e3
      await store.insertFromServer(makeFlag('f1', 'e2', 'subj-1'));
      await store.insertFromServer(makeFlag('f2', 'e3', 'subj-1',
          timestamp: '2026-04-18T11:05:00Z'));
      // Resolve e2 as accepted
      await store.insertFromServer(
          makeResolution('r1', 'f1', 'e2', 'subj-1', 'accepted'));

      final flagged = await pe.getFlaggedEventIds();
      expect(flagged, contains('e3')); // Still flagged
      expect(flagged, isNot(contains('e2'))); // Resolved (accepted)
    });

    test('system events excluded from subject grouping', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      await store.insertFromServer(makeFlag('f1', 'e1', 'subj-1'));

      final subjects = await pe.getSubjectList();
      // Only 1 subject — the conflict_detected event doesn't create a separate subject
      expect(subjects, hasLength(1));
    });

    test('assignment events excluded from subject grouping', () async {
      await store.insert(makeCapture('e1', 'subj-1', name: 'Alice'));
      // Simulate receiving an assignment_changed event via sync
      final assignmentEvent = Event(
        id: 'assign-1',
        type: 'assignment_changed',
        shapeRef: 'assignment_created/v1',
        subjectRef: {'type': 'assignment', 'id': 'assign-uuid-1'},
        actorRef: {'type': 'actor', 'id': 'admin-1'},
        deviceId: 'server',
        deviceSeq: 200,
        syncWatermark: 50,
        timestamp: '2026-04-19T10:00:00Z',
        payload: {
          'target_actor': {'type': 'actor', 'id': 'actor-1'},
          'role': 'field_worker',
          'scope': {'geographic': 'loc-1', 'subject_list': null, 'activity': null},
          'valid_from': '2026-04-19T00:00:00Z',
          'valid_to': null,
        },
      );
      await store.insertFromServer(assignmentEvent);

      final subjects = await pe.getSubjectList();
      // Only 1 subject — the assignment event doesn't create a subject entry
      expect(subjects, hasLength(1));
      expect(subjects[0].subjectId, 'subj-1');
    });

    test('processAssignmentEvent stores and tracks assignments', () async {
      final createdEvent = Event(
        id: 'assign-evt-1',
        type: 'assignment_changed',
        shapeRef: 'assignment_created/v1',
        subjectRef: {'type': 'assignment', 'id': 'assign-uuid-2'},
        actorRef: {'type': 'actor', 'id': 'admin-1'},
        deviceId: 'server',
        deviceSeq: 201,
        syncWatermark: 51,
        timestamp: '2026-04-19T10:00:00Z',
        payload: {
          'target_actor': {'type': 'actor', 'id': 'actor-1'},
          'role': 'field_worker',
          'scope': {
            'geographic': 'loc-1',
            'subject_list': null,
            'activity': ['vaccination', 'survey'],
          },
          'valid_from': '2026-04-19T00:00:00Z',
          'valid_to': null,
        },
      );
      await store.processAssignmentEvent(createdEvent);

      var assignments = await store.getActiveAssignments();
      expect(assignments, hasLength(1));
      expect(assignments[0]['assignment_id'], 'assign-uuid-2');
      expect(assignments[0]['role'], 'field_worker');
      expect(assignments[0]['activity_list'], 'vaccination,survey');
      expect(assignments[0]['ended'], 0);

      // End the assignment
      final endedEvent = Event(
        id: 'assign-evt-2',
        type: 'assignment_changed',
        shapeRef: 'assignment_ended/v1',
        subjectRef: {'type': 'assignment', 'id': 'assign-uuid-2'},
        actorRef: {'type': 'actor', 'id': 'admin-1'},
        deviceId: 'server',
        deviceSeq: 202,
        syncWatermark: 52,
        timestamp: '2026-04-19T11:00:00Z',
        payload: {'reason': 'reassignment'},
      );
      await store.processAssignmentEvent(endedEvent);

      assignments = await store.getActiveAssignments();
      expect(assignments, isEmpty);
    });
  });
}
