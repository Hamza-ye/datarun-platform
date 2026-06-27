import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/field_asset_lookup.dart';
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
  /// [activityRef] — activity context when the form was opened from an activity; null otherwise.
  /// [payload] — shape-conforming field values
  Future<Event> assemble({
    required String? subjectId,
    required String shapeRef,
    required Map<String, dynamic> payload,
    String? activityRef,
  }) async {
    final actorId = _identity.actorId;
    if (_eventStore.actorId != null && _eventStore.actorId != actorId) {
      throw StateError('EventStore actor partition is not active');
    }

    final sid = subjectId ?? _uuid.v4();
    final eventId = _uuid.v4();
    final seq = await _identity.nextSeq();
    final timestamp = DateTime.now().toUtc().toIso8601String();
    final eventPayload = _withCandidateRecordReference(
      payload,
      eventId,
      timestamp,
    );

    final event = Event(
      id: eventId,
      type: 'capture', // Phase 0: only type
      shapeRef: shapeRef,
      activityRef: activityRef,
      subjectRef: {'type': 'subject', 'id': sid},
      actorRef: {'type': 'actor', 'id': actorId},
      deviceId: _identity.deviceId,
      deviceSeq: seq,
      syncWatermark: null, // Server-assigned
      timestamp: timestamp,
      payload: eventPayload,
    );

    await _eventStore.insert(event);
    return event;
  }

  Map<String, dynamic> _withCandidateRecordReference(
    Map<String, dynamic> payload,
    String eventId,
    String timestamp,
  ) {
    final evidence = payload[assetCandidateEvidenceKey];
    if (evidence is! Map) {
      return payload;
    }

    final copy = Map<String, dynamic>.from(payload);
    final evidenceCopy = Map<String, dynamic>.from(evidence);
    evidenceCopy.putIfAbsent('capture_timestamp', () => timestamp);
    evidenceCopy.putIfAbsent(
      'original_submitted_record_ref',
      () => {'type': 'event', 'id': eventId},
    );
    copy[assetCandidateEvidenceKey] = evidenceCopy;
    return copy;
  }
}
