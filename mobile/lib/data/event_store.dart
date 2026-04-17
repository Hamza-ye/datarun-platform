import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:datarun_mobile/domain/event.dart';

class EventStore {
  static const _dbName = 'datarun.db';
  static const _dbVersion = 1;
  static const _table = 'events';

  Database? _db;

  Future<Database> get database async {
    _db ??= await _initDb();
    return _db!;
  }

  Future<Database> _initDb() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, _dbName);
    return openDatabase(
      path,
      version: _dbVersion,
      onCreate: _onCreate,
      onUpgrade: _onUpgrade,
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE $_table (
        id              TEXT PRIMARY KEY,
        type            TEXT NOT NULL,
        shape_ref       TEXT NOT NULL,
        activity_ref    TEXT,
        subject_ref     TEXT NOT NULL,
        actor_ref       TEXT NOT NULL,
        device_id       TEXT NOT NULL,
        device_seq      INTEGER NOT NULL,
        sync_watermark  INTEGER,
        timestamp       TEXT NOT NULL,
        payload         TEXT NOT NULL,
        pushed          INTEGER NOT NULL DEFAULT 0,
        UNIQUE(device_id, device_seq)
      )
    ''');
  }

  Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    // F11: Schema changes must use migration scripts.
    // Future migrations go here as version increments.
  }

  /// Insert a locally-created event. pushed = 0.
  Future<void> insert(Event event) async {
    final db = await database;
    await db.insert(_table, event.toMap(),
        conflictAlgorithm: ConflictAlgorithm.ignore);
  }

  /// Insert an event received from the server (already has sync_watermark).
  /// Deduplicates by id.
  Future<void> insertFromServer(Event event) async {
    final db = await database;
    final map = event.toMap();
    map['pushed'] = 1; // Server events don't need pushing
    map['sync_watermark'] = event.syncWatermark;
    await db.insert(_table, map,
        conflictAlgorithm: ConflictAlgorithm.ignore);
  }

  /// All events not yet pushed to server.
  Future<List<Event>> getUnpushed() async {
    final db = await database;
    final rows = await db.query(_table,
        where: 'pushed = ?', whereArgs: [0], orderBy: 'device_seq ASC');
    return rows.map(Event.fromMap).toList();
  }

  /// Mark events as pushed after successful sync.
  Future<void> markPushed(List<String> ids) async {
    if (ids.isEmpty) return;
    final db = await database;
    final placeholders = ids.map((_) => '?').join(',');
    await db.rawUpdate(
        'UPDATE $_table SET pushed = 1 WHERE id IN ($placeholders)', ids);
  }

  /// All events for a given subject, ordered by device_seq.
  Future<List<Event>> getBySubject(String subjectId) async {
    final db = await database;
    // subject_ref is stored as JSON string like {"type":"subject","id":"..."}
    // We search using LIKE for the id within the JSON.
    final rows = await db.query(_table,
        where: 'subject_ref LIKE ?',
        whereArgs: ['%"id":"$subjectId"%'],
        orderBy: 'timestamp DESC');
    return rows.map(Event.fromMap).toList();
  }

  /// All events in the local store.
  Future<List<Event>> getAll() async {
    final db = await database;
    final rows = await db.query(_table, orderBy: 'timestamp DESC');
    return rows.map(Event.fromMap).toList();
  }

  /// Count of unpushed events.
  Future<int> unpushedCount() async {
    final db = await database;
    final result =
        await db.rawQuery('SELECT COUNT(*) as c FROM $_table WHERE pushed = 0');
    return Sqflite.firstIntValue(result) ?? 0;
  }
}
