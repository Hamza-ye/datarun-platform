import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:datarun_mobile/domain/event.dart';

class EventStore {
  static const _dbName = 'datarun.db';
  static const _dbVersion = 5;
  static const _table = 'events';
  static const _configTable = 'config_current';
  static const _configPendingTable = 'config_pending';
  static const _aliasTable = 'subject_aliases';
  static const _assignmentTable = 'local_assignments';

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
    await db.execute('''
      CREATE TABLE $_assignmentTable (
        assignment_id TEXT PRIMARY KEY,
        role          TEXT NOT NULL,
        geo_scope     TEXT,
        subject_list  TEXT,
        activity_list TEXT,
        valid_from    TEXT NOT NULL,
        valid_to      TEXT,
        ended         INTEGER NOT NULL DEFAULT 0
      )
    ''');
    await db.execute('''
      CREATE TABLE $_configTable (
        id            INTEGER PRIMARY KEY CHECK(id = 1),
        version       INTEGER NOT NULL,
        package_json  TEXT NOT NULL
      )
    ''');
    await db.execute('''
      CREATE TABLE $_configPendingTable (
        id            INTEGER PRIMARY KEY CHECK(id = 1),
        version       INTEGER NOT NULL,
        package_json  TEXT NOT NULL
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
    if (oldVersion < 3) {
      await db.execute('''
        CREATE TABLE $_assignmentTable (
          assignment_id TEXT PRIMARY KEY,
          role          TEXT NOT NULL,
          geo_scope     TEXT,
          subject_list  TEXT,
          activity_list TEXT,
          valid_from    TEXT NOT NULL,
          valid_to      TEXT,
          ended         INTEGER NOT NULL DEFAULT 0
        )
      ''');
    }
    if (oldVersion < 4) {
      await db.execute('''
        CREATE TABLE $_configTable (
          id            INTEGER PRIMARY KEY CHECK(id = 1),
          version       INTEGER NOT NULL,
          package_json  TEXT NOT NULL
        )
      ''');
    }
    if (oldVersion < 5) {
      await db.execute('''
        CREATE TABLE $_configPendingTable (
          id            INTEGER PRIMARY KEY CHECK(id = 1),
          version       INTEGER NOT NULL,
          package_json  TEXT NOT NULL
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

  // --- Assignment table operations (Phase 2b) ---

  /// Process an assignment event to maintain local scope knowledge.
  /// assignment_created → insert into local_assignments
  /// assignment_ended → mark ended = 1
  Future<void> processAssignmentEvent(Event event) async {
    final db = await database;
    final shapeRef = event.shapeRef;
    final assignmentId = event.subjectRef['id']!;

    if (shapeRef == 'assignment_created/v1') {
      final payload = event.payload;
      final scope = payload['scope'] as Map<String, dynamic>?;
      await db.insert(_assignmentTable, {
        'assignment_id': assignmentId,
        'role': payload['role'] as String,
        'geo_scope': scope?['geographic'] as String?,
        'subject_list': scope?['subject_list'] != null
            ? (scope!['subject_list'] as List).join(',')
            : null,
        'activity_list': scope?['activity'] != null
            ? (scope!['activity'] as List).join(',')
            : null,
        'valid_from': payload['valid_from'] as String,
        'valid_to': payload['valid_to'] as String?,
        'ended': 0,
      }, conflictAlgorithm: ConflictAlgorithm.replace);
    } else if (shapeRef == 'assignment_ended/v1') {
      await db.update(
        _assignmentTable,
        {'ended': 1},
        where: 'assignment_id = ?',
        whereArgs: [assignmentId],
      );
    }
  }

  /// Get active assignments for the local actor.
  Future<List<Map<String, dynamic>>> getActiveAssignments() async {
    final db = await database;
    return db.query(
      _assignmentTable,
      where: 'ended = 0',
    );
  }

  /// Selective-retain: purge out-of-scope events from other actors.
  /// Own events (matching ownDeviceId) are always retained.
  /// Returns the count of purged events.
  Future<int> purgeOutOfScopeEvents(String ownDeviceId) async {
    final db = await database;
    final assignments = await getActiveAssignments();
    if (assignments.isEmpty) return 0; // No assignments → no scope info → keep all

    // Build list of in-scope subject IDs from assignments' subject_list + geo
    // For selective-retain, we only purge events from OTHER devices whose subjects
    // are provably out of scope. Geographic containment requires path matching.
    // Subject list filtering is simpler: if assignments specify subject_list,
    // events for subjects NOT in any list (and not from own device) are purge candidates.

    final allSubjects = <String>{};
    bool hasSubjectListScope = false;
    for (final a in assignments) {
      final sl = a['subject_list'] as String?;
      if (sl != null && sl.isNotEmpty) {
        hasSubjectListScope = true;
        allSubjects.addAll(sl.split(','));
      }
    }

    if (!hasSubjectListScope) return 0; // No subject_list scope → can't determine out-of-scope

    // Find events from other devices whose subject is not in any active assignment's subject_list
    // and not system events
    final candidates = await db.rawQuery('''
      SELECT id, subject_ref FROM $_table
      WHERE device_id != ?
        AND type NOT IN ('conflict_detected', 'conflict_resolved',
                         'subjects_merged', 'subject_split', 'assignment_changed')
    ''', [ownDeviceId]);

    final toPurge = <String>[];
    for (final row in candidates) {
      final subjectRef = row['subject_ref'] as String;
      // Extract subject ID from JSON string stored in subject_ref
      final match = RegExp(r'"id"\s*:\s*"([^"]+)"').firstMatch(subjectRef);
      if (match != null) {
        final subjectId = match.group(1)!;
        if (!allSubjects.contains(subjectId)) {
          toPurge.add(row['id'] as String);
        }
      }
    }

    if (toPurge.isEmpty) return 0;

    // Purge in batches
    final batch = db.batch();
    for (final id in toPurge) {
      batch.delete(_table, where: 'id = ?', whereArgs: [id]);
    }
    await batch.commit(noResult: true);

    return toPurge.length;
  }

  // --- Config package persistence (Phase 3a) ---

  /// Read the stored config package, or null if none.
  Future<Map<String, dynamic>?> getConfigPackage() async {
    final db = await database;
    final rows = await db.query(_configTable, where: 'id = 1');
    if (rows.isEmpty) return null;
    final row = rows.first;
    return {
      'version': row['version'] as int,
      'package_json': row['package_json'] as String,
    };
  }

  /// Store (or replace) the config package.
  Future<void> saveConfigPackage(int version, String packageJson) async {
    final db = await database;
    await db.rawInsert(
      'INSERT OR REPLACE INTO $_configTable (id, version, package_json) VALUES (1, ?, ?)',
      [version, packageJson],
    );
  }

  // --- Pending config package (Phase 3c: two-slot model, IDR-019) ---

  /// Read the pending config package, or null if none.
  Future<Map<String, dynamic>?> getPendingConfigPackage() async {
    final db = await database;
    final rows = await db.query(_configPendingTable, where: 'id = 1');
    if (rows.isEmpty) return null;
    final row = rows.first;
    return {
      'version': row['version'] as int,
      'package_json': row['package_json'] as String,
    };
  }

  /// Store (or replace) the pending config package.
  Future<void> savePendingConfigPackage(int version, String packageJson) async {
    final db = await database;
    await db.rawInsert(
      'INSERT OR REPLACE INTO $_configPendingTable (id, version, package_json) VALUES (1, ?, ?)',
      [version, packageJson],
    );
  }

  /// Delete the pending config package (after promotion to current).
  Future<void> deletePendingConfigPackage() async {
    final db = await database;
    await db.delete(_configPendingTable, where: 'id = 1');
  }
}
