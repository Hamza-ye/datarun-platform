import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:flutter_test/flutter_test.dart';

/// Phase 3e (DD-2) contract: integrity and identity events are discriminated
/// by `shape_ref`, never by envelope `type`. The four named predicates below
/// encode that rule. A change to envelope type vocabulary must never break
/// discrimination, and an unrelated shape must never be mistaken for a flag.
void main() {
  Event make({required String type, required String shapeRef}) {
    return Event(
      id: '00000000-0000-0000-0000-000000000001',
      type: type,
      shapeRef: shapeRef,
      subjectRef: const {'type': 'subject', 'id': 's1'},
      actorRef: const {'type': 'actor', 'id': 'a1'},
      deviceId: 'd1',
      deviceSeq: 1,
      syncWatermark: 1,
      timestamp: '2026-04-21T10:00:00Z',
      payload: const {},
    );
  }

  group('event_classifiers (ADR-002 Addendum DD-2)', () {
    test('isIntegrityFlag matches conflict_detected/v* and nothing else', () {
      expect(isIntegrityFlag(make(type: 'alert', shapeRef: 'conflict_detected/v1')), isTrue);
      expect(isIntegrityFlag(make(type: 'alert', shapeRef: 'conflict_detected/v2')), isTrue);
      expect(isIntegrityFlag(make(type: 'review', shapeRef: 'conflict_resolved/v1')), isFalse);
      expect(isIntegrityFlag(make(type: 'capture', shapeRef: 'basic_capture/v1')), isFalse);
    });

    test('isIntegrityResolution matches conflict_resolved/v* and nothing else', () {
      expect(isIntegrityResolution(make(type: 'review', shapeRef: 'conflict_resolved/v1')), isTrue);
      expect(isIntegrityResolution(make(type: 'capture', shapeRef: 'conflict_resolved/v1')), isTrue,
          reason: 'Shape-based discrimination: auto-resolutions also carry this shape.');
      expect(isIntegrityResolution(make(type: 'alert', shapeRef: 'conflict_detected/v1')), isFalse);
      expect(isIntegrityResolution(make(type: 'capture', shapeRef: 'basic_capture/v1')), isFalse);
    });

    test('isIdentityLifecycle matches subjects_merged/v* and subject_split/v*', () {
      expect(isIdentityLifecycle(make(type: 'capture', shapeRef: 'subjects_merged/v1')), isTrue);
      expect(isIdentityLifecycle(make(type: 'capture', shapeRef: 'subject_split/v1')), isTrue);
      expect(isIdentityLifecycle(make(type: 'capture', shapeRef: 'basic_capture/v1')), isFalse);
    });

    test('isAssignmentEvent matches envelope type assignment_changed', () {
      expect(
          isAssignmentEvent(
              make(type: 'assignment_changed', shapeRef: 'assignment_created/v1')),
          isTrue);
      expect(
          isAssignmentEvent(
              make(type: 'assignment_changed', shapeRef: 'assignment_ended/v1')),
          isTrue);
      expect(
          isAssignmentEvent(make(type: 'capture', shapeRef: 'basic_capture/v1')),
          isFalse);
    });

    test('predicates are mutually exclusive for canonical events', () {
      final flag = make(type: 'alert', shapeRef: 'conflict_detected/v1');
      expect(
          [
            isIntegrityFlag(flag),
            isIntegrityResolution(flag),
            isIdentityLifecycle(flag),
            isAssignmentEvent(flag),
          ].where((b) => b).length,
          1);

      final merge = make(type: 'capture', shapeRef: 'subjects_merged/v1');
      expect(
          [
            isIntegrityFlag(merge),
            isIntegrityResolution(merge),
            isIdentityLifecycle(merge),
            isAssignmentEvent(merge),
          ].where((b) => b).length,
          1);
    });
  });
}
