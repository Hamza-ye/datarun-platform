import 'package:flutter_test/flutter_test.dart';
import 'package:datarun_mobile/domain/event.dart';

void main() {
  group('Event', () {
    test('toMap and fromMap roundtrip', () {
      final event = Event(
        id: 'test-id',
        type: 'capture',
        shapeRef: 'basic_capture/v1',
        activityRef: null,
        subjectRef: {'type': 'subject', 'id': 'subj-1'},
        actorRef: {'type': 'actor', 'id': 'actor-1'},
        deviceId: 'device-1',
        deviceSeq: 1,
        syncWatermark: null,
        timestamp: '2026-04-17T10:00:00Z',
        payload: {'name': 'Test', 'value': 42},
      );

      final map = event.toMap();
      final restored = Event.fromMap(map);

      expect(restored.id, event.id);
      expect(restored.type, event.type);
      expect(restored.shapeRef, event.shapeRef);
      expect(restored.subjectRef, event.subjectRef);
      expect(restored.actorRef, event.actorRef);
      expect(restored.deviceId, event.deviceId);
      expect(restored.deviceSeq, event.deviceSeq);
      expect(restored.payload['name'], 'Test');
      expect(restored.payload['value'], 42);
    });

    test('toEnvelope produces server-compatible JSON', () {
      final event = Event(
        id: 'test-id',
        type: 'capture',
        shapeRef: 'basic_capture/v1',
        activityRef: null,
        subjectRef: {'type': 'subject', 'id': 'subj-1'},
        actorRef: {'type': 'actor', 'id': 'actor-1'},
        deviceId: 'device-1',
        deviceSeq: 1,
        syncWatermark: null,
        timestamp: '2026-04-17T10:00:00Z',
        payload: {'name': 'Test'},
      );

      final envelope = event.toEnvelope();

      expect(envelope['id'], 'test-id');
      expect(envelope['type'], 'capture');
      expect(envelope['shape_ref'], 'basic_capture/v1');
      expect(envelope['subject_ref'], {'type': 'subject', 'id': 'subj-1'});
      expect(envelope['sync_watermark'], isNull);
    });
  });
}
