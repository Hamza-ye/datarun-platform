import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:uuid/uuid.dart';

/// Assembles a full 11-field event envelope from form state and writes to Event Store.
class EventAssembler {
  final DeviceIdentity _identity;
  final EventStore _eventStore;
  static const _uuid = Uuid();

  EventAssembler(this._identity, this._eventStore);

  /// Build and persist an event from form data.
  /// [subjectId] — existing subject UUID or null for new subject.
  /// [shapeRef] — e.g. "basic_capture/v1"
  /// [payload] — shape-conforming field values
  Future<Event> assemble({
    required String? subjectId,
    required String shapeRef,
    required Map<String, dynamic> payload,
  }) async {
    final sid = subjectId ?? _uuid.v4();
    final seq = await _identity.nextSeq();

    final event = Event(
      id: _uuid.v4(),
      type: 'capture', // Phase 0: only type
      shapeRef: shapeRef,
      activityRef: null, // Phase 0: no activities
      subjectRef: {'type': 'subject', 'id': sid},
      actorRef: {'type': 'actor', 'id': _identity.actorId},
      deviceId: _identity.deviceId,
      deviceSeq: seq,
      syncWatermark: null, // Server-assigned
      timestamp: DateTime.now().toUtc().toIso8601String(),
      payload: payload,
    );

    await _eventStore.insert(event);
    return event;
  }
}
