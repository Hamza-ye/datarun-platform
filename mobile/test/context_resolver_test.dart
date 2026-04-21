import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/domain/event.dart';

/// Phase 3d.3: verify ContextResolver resolves `context.*` properties per
/// ADR-5 S8 / IDR-018 rule 4 (pre-resolved at form-open, null-tolerant).
void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  late EventStore store;
  late ProjectionEngine projection;
  late ContextResolver resolver;
  late String dbPath;
  var seq = 1;

  setUp(() async {
    seq = 1;
    dbPath =
        '${Directory.systemTemp.path}/datarun_ctx_${DateTime.now().microsecondsSinceEpoch}.db';
    store = EventStore(dbPath: dbPath);
    projection = ProjectionEngine(store);
    resolver = ContextResolver(store, projection);
  });

  tearDown(() async {
    await store.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  });

  Event capture(String subjectId, String timestamp) {
    return Event(
      id: 'evt-${seq++}',
      type: 'capture',
      shapeRef: 'basic_capture/v1',
      subjectRef: {'type': 'subject', 'id': subjectId},
      actorRef: {'type': 'actor', 'id': 'actor-1'},
      deviceId: 'device-1',
      deviceSeq: seq,
      syncWatermark: seq,
      timestamp: timestamp,
      payload: {'name': 'Test'},
    );
  }

  Event assignmentCreated({
    required String role,
    String? geoScope,
  }) {
    return Event(
      id: 'asgn-${seq++}',
      type: 'assignment_changed',
      shapeRef: 'assignment_created/v1',
      subjectRef: {'type': 'assignment', 'id': 'asgn-1'},
      actorRef: {'type': 'actor', 'id': 'admin-1'},
      deviceId: 'admin-device',
      deviceSeq: seq,
      syncWatermark: seq,
      timestamp: '2026-04-01T00:00:00Z',
      payload: {
        'target_actor': {'type': 'actor', 'id': 'actor-1'},
        'role': role,
        'scope': {
          if (geoScope != null) 'geographic': geoScope,
        },
        'valid_from': '2026-04-01T00:00:00Z',
      },
    );
  }

  group('ContextResolver', () {
    test('new subject → event_count=0 and days_since_last_event=null',
        () async {
      final ctx = await resolver.resolve(subjectId: null);

      expect(ctx['context.event_count'], 0);
      expect(ctx['context.days_since_last_event'], isNull);
    });

    test('existing subject → event_count and days_since_last_event computed',
        () async {
      await store.insertFromServer(capture('subj-1', '2026-04-10T10:00:00Z'));
      await store.insertFromServer(capture('subj-1', '2026-04-15T10:00:00Z'));

      final now = DateTime.parse('2026-04-20T10:00:00Z');
      final ctx = await resolver.resolve(subjectId: 'subj-1', now: now);

      expect(ctx['context.event_count'], 2);
      expect(ctx['context.days_since_last_event'], 5);
    });

    test('no active assignment → actor role/scope null', () async {
      final ctx = await resolver.resolve(subjectId: null);

      expect(ctx['context.actor.role'], isNull);
      expect(ctx['context.actor.scope_name'], isNull);
    });

    test('active assignment → actor role and scope_name resolved', () async {
      final asgn =
          assignmentCreated(role: 'field_worker', geoScope: '/SL/Western');
      await store.insertFromServer(asgn);
      await store.processAssignmentEvent(asgn);

      final ctx = await resolver.resolve(subjectId: null);

      expect(ctx['context.actor.role'], 'field_worker');
      expect(ctx['context.actor.scope_name'], '/SL/Western');
    });

    test('Phase 4 placeholders pre-resolved as null', () async {
      final ctx = await resolver.resolve(subjectId: null);

      expect(ctx.containsKey('context.subject_state'), true);
      expect(ctx['context.subject_state'], isNull);
      expect(ctx.containsKey('context.subject_pattern'), true);
      expect(ctx['context.subject_pattern'], isNull);
      expect(ctx.containsKey('context.activity_stage'), true);
      expect(ctx['context.activity_stage'], isNull);
    });

    test('subject with zero events → days_since_last_event null', () async {
      // Subject id exists in our projection space but has no events
      final ctx = await resolver.resolve(subjectId: 'subj-empty');

      expect(ctx['context.event_count'], 0);
      expect(ctx['context.days_since_last_event'], isNull);
    });
  });
}
