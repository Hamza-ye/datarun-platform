import 'dart:convert';

/// Domain model for the 11-field event envelope.
class Event {
  final String id;
  final String type;
  final String shapeRef;
  final String? activityRef;
  final Map<String, String> subjectRef; // {type, id}
  final Map<String, String> actorRef; // {type, id}
  final String deviceId;
  final int deviceSeq;
  final int? syncWatermark;
  final String timestamp; // ISO 8601
  final Map<String, dynamic> payload;

  Event({
    required this.id,
    required this.type,
    required this.shapeRef,
    this.activityRef,
    required this.subjectRef,
    required this.actorRef,
    required this.deviceId,
    required this.deviceSeq,
    this.syncWatermark,
    required this.timestamp,
    required this.payload,
  });

  /// To SQLite row (JSON fields stored as TEXT).
  Map<String, dynamic> toMap() => {
        'id': id,
        'type': type,
        'shape_ref': shapeRef,
        'activity_ref': activityRef,
        'subject_ref': jsonEncode(subjectRef),
        'actor_ref': jsonEncode(actorRef),
        'device_id': deviceId,
        'device_seq': deviceSeq,
        'sync_watermark': syncWatermark,
        'timestamp': timestamp,
        'payload': jsonEncode(payload),
        'pushed': 0,
      };

  /// For sync push — the JSON envelope sent to the server.
  Map<String, dynamic> toEnvelope() => {
        'id': id,
        'type': type,
        'shape_ref': shapeRef,
        'activity_ref': activityRef,
        'subject_ref': subjectRef,
        'actor_ref': actorRef,
        'device_id': deviceId,
        'device_seq': deviceSeq,
        'sync_watermark': syncWatermark,
        'timestamp': timestamp,
        'payload': payload,
      };

  /// From SQLite row (JSON fields stored as TEXT).
  factory Event.fromMap(Map<String, dynamic> map) => Event(
        id: map['id'] as String,
        type: map['type'] as String,
        shapeRef: map['shape_ref'] as String,
        activityRef: map['activity_ref'] as String?,
        subjectRef: Map<String, String>.from(
            jsonDecode(map['subject_ref'] as String) as Map),
        actorRef: Map<String, String>.from(
            jsonDecode(map['actor_ref'] as String) as Map),
        deviceId: map['device_id'] as String,
        deviceSeq: map['device_seq'] as int,
        syncWatermark: map['sync_watermark'] as int?,
        timestamp: map['timestamp'] as String,
        payload: Map<String, dynamic>.from(
            jsonDecode(map['payload'] as String) as Map),
      );

  /// From server JSON response (maps already decoded, not JSON strings).
  factory Event.fromServerJson(Map<String, dynamic> json) => Event(
        id: json['id'] as String,
        type: json['type'] as String,
        shapeRef: json['shape_ref'] as String,
        activityRef: json['activity_ref'] as String?,
        subjectRef: Map<String, String>.from(json['subject_ref'] as Map),
        actorRef: Map<String, String>.from(json['actor_ref'] as Map),
        deviceId: json['device_id'] as String,
        deviceSeq: json['device_seq'] as int,
        syncWatermark: json['sync_watermark'] as int?,
        timestamp: json['timestamp'] as String,
        payload: Map<String, dynamic>.from(json['payload'] as Map),
      );
}
