import 'dart:convert';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

/// Local event store. Mirrors the wire envelope 1:1 so the client never has to
/// reconstruct an event at push time — rows are pushed verbatim.
///
/// Two tables:
///  * `events` — all events the device knows about. `sync_state` distinguishes
///    locally-captured-not-yet-synced (`pending`), locally-captured-and-acked
///    (`synced`), and server-emitted events pulled down (`remote`).
///  * `villages` — the latest config snapshot (refreshed on every fetch).
class LocalDb {
  static const _schemaVersion = 1;
  static const _dbFile = 'datarun_mobile.db';

  final Database _db;
  LocalDb._(this._db);

  static Future<LocalDb> open() async {
    final dir = await getApplicationDocumentsDirectory();
    final path = p.join(dir.path, _dbFile);
    final db = await openDatabase(
      path,
      version: _schemaVersion,
      onCreate: (db, _) async {
        await db.execute('''
          CREATE TABLE events (
            id TEXT PRIMARY KEY,
            type TEXT NOT NULL,
            shape_ref TEXT NOT NULL,
            subject_id TEXT NOT NULL,
            device_seq INTEGER NOT NULL,
            sync_watermark INTEGER,
            timestamp TEXT NOT NULL,
            envelope_json TEXT NOT NULL,
            sync_state TEXT NOT NULL CHECK (sync_state IN ('pending','synced','remote'))
          )
        ''');
        await db.execute('''
          CREATE TABLE villages (
            id TEXT PRIMARY KEY,
            district_name TEXT NOT NULL,
            name TEXT NOT NULL
          )
        ''');
        await db.execute(
          'CREATE INDEX idx_events_sync_state ON events(sync_state)',
        );
      },
    );
    return LocalDb._(db);
  }

  // ------------------------------------------------------------- events
  Future<void> insertLocalCapture(Map<String, Object?> envelope) async {
    await _db.insert('events', _eventRow(envelope, 'pending'),
        conflictAlgorithm: ConflictAlgorithm.ignore);
  }

  Future<void> markSynced(String eventId, int watermark) async {
    await _db.update(
      'events',
      {'sync_state': 'synced', 'sync_watermark': watermark},
      where: 'id = ?',
      whereArgs: [eventId],
    );
  }

  /// Upsert a server-side event pulled down. Never overwrites a local pending
  /// row (the local client is source of truth until its own push is acked).
  Future<void> upsertRemote(Map<String, Object?> envelope) async {
    final id = envelope['id'] as String;
    final existing = await _db
        .query('events', columns: ['sync_state'], where: 'id = ?', whereArgs: [id]);
    if (existing.isNotEmpty && existing.first['sync_state'] == 'pending') {
      // Our own unsynced write — don't clobber it.
      return;
    }
    await _db.insert('events', _eventRow(envelope, 'remote'),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<Map<String, Object?>>> pendingEnvelopes() async {
    final rows = await _db.query(
      'events',
      where: "sync_state = 'pending'",
      orderBy: 'device_seq ASC',
    );
    return rows
        .map((r) => jsonDecode(r['envelope_json'] as String) as Map<String, Object?>)
        .toList();
  }

  /// Unified event feed for the Home screen.
  Future<List<EventRow>> allEvents() async {
    final rows = await _db.query(
      'events',
      orderBy: "sync_watermark IS NULL DESC, sync_watermark DESC, timestamp DESC",
    );
    return rows.map(EventRow.fromRow).toList();
  }

  Map<String, Object?> _eventRow(Map<String, Object?> env, String state) {
    final subjectRef = env['subject_ref'] as Map;
    return {
      'id': env['id'],
      'type': env['type'],
      'shape_ref': env['shape_ref'],
      'subject_id': subjectRef['id'],
      'device_seq': env['device_seq'],
      'sync_watermark': env['sync_watermark'],
      'timestamp': env['timestamp'],
      'envelope_json': jsonEncode(env),
      'sync_state': state,
    };
  }

  // ------------------------------------------------------------- villages
  Future<void> replaceVillages(List<Map<String, Object?>> villages) async {
    final batch = _db.batch();
    batch.delete('villages');
    for (final v in villages) {
      batch.insert('villages', {
        'id': v['id'],
        'district_name': v['district_name'],
        'name': v['name'],
      });
    }
    await batch.commit(noResult: true);
  }

  Future<List<Village>> villages() async {
    final rows = await _db.query('villages', orderBy: 'name ASC');
    return rows
        .map((r) => Village(
              id: r['id'] as String,
              districtName: r['district_name'] as String,
              name: r['name'] as String,
            ))
        .toList();
  }
}

class Village {
  final String id;
  final String districtName;
  final String name;
  const Village({required this.id, required this.districtName, required this.name});
}

class EventRow {
  final String id;
  final String type;
  final String shapeRef;
  final String subjectId;
  final int deviceSeq;
  final int? syncWatermark;
  final String timestamp;
  final String syncState;
  final Map<String, Object?> envelope;

  EventRow({
    required this.id,
    required this.type,
    required this.shapeRef,
    required this.subjectId,
    required this.deviceSeq,
    required this.syncWatermark,
    required this.timestamp,
    required this.syncState,
    required this.envelope,
  });

  static EventRow fromRow(Map<String, Object?> r) => EventRow(
        id: r['id'] as String,
        type: r['type'] as String,
        shapeRef: r['shape_ref'] as String,
        subjectId: r['subject_id'] as String,
        deviceSeq: r['device_seq'] as int,
        syncWatermark: r['sync_watermark'] as int?,
        timestamp: r['timestamp'] as String,
        syncState: r['sync_state'] as String,
        envelope: jsonDecode(r['envelope_json'] as String) as Map<String, Object?>,
      );
}
