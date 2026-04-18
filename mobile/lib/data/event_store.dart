import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:datarun_mobile/domain/event.dart';

class EventStore {
  static const _dbName = 'datarun.db';
  static const _dbVersion = 2;
  static const _table = 'events';
  static const _aliasTable = 'subject_aliases';

  final String? _dbPath; // null = default platform path
  Database? _db;

  EventStore({String? dbPath}) : _dbPath = dbPath;

  Future<Database> get database async {
    _db ??= await _initDb();
    return _db!;
  }

  Future<Database> _initDb() async {
    final path = _dbPath ?? join(await getDatabasesPath(), _dbName);
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
    await db.execute('''
      CREATE TABLE $_aliasTable (
        retired_id    TEXT PRIMARY KEY,
        surviving_id  TEXT NOT NULL,
        merged_at     TEXT NOT NULL
      )
    ''');
  }

  Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    // F11: Schema changes must use migration scripts.
    if (oldVersion < 2) {
      await db.execute('''
        CREATE TABLE $_aliasTable (
          retired_id    TEXT PRIMARY KEY,
          surviving_id  TEXT NOT NULL,
          merged_at     TEXT NOT NULL
        )
      ''');
    }
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

  // --- Alias table operations ---

  /// Upsert an alias with eager transitive closure.
  /// If existing aliases point to retiredId, they are updated to point to survivingId.
  Future<void> upsertAlias(String retiredId, String survivingId, String mergedAt) async {
    final db = await database;
    await db.transaction((txn) async {
      // Eager transitive closure: update any alias that pointed to the retired subject
      await txn.rawUpdate(
          'UPDATE $_aliasTable SET surviving_id = ? WHERE surviving_id = ?',
          [survivingId, retiredId]);
      // Insert or replace the new alias
      await txn.rawInsert(
          'INSERT OR REPLACE INTO $_aliasTable (retired_id, surviving_id, merged_at) '
          'VALUES (?, ?, ?)',
          [retiredId, survivingId, mergedAt]);
    });
  }

  /// Load all aliases as a map: retired_id → surviving_id.
  Future<Map<String, String>> getAllAliases() async {
    final db = await database;
    final rows = await db.query(_aliasTable);
    return {for (final r in rows) r['retired_id'] as String: r['surviving_id'] as String};
  }

  /// Close the database (for testing).
  Future<void> close() async {
    final db = _db;
    if (db != null && db.isOpen) {
      await db.close();
      _db = null;
    }
  }
}
