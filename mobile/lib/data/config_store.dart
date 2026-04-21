import 'dart:convert';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/shape.dart';

/// Stores and caches the config package (IDR-019).
/// Two-slot model: current (in-memory, used by forms) + pending (awaiting promotion).
/// Pending is promoted to current at safe transition points (form open, refresh).
/// At-most-2 invariant: only current + pending exist simultaneously.
class ConfigStore {
  final EventStore _eventStore;

  int _configVersion = 0;
  Map<String, ShapeDefinition> _shapes = {};
  Map<String, Map<String, dynamic>> _activities = {};
  // Key: "{activity_ref}.{shape_ref}" → List of rule maps
  Map<String, List<Map<String, dynamic>>> _expressions = {};
  // Sensitivity classifications (IDR-019 §sensitivity_classifications)
  Map<String, String> _shapeSensitivity = {};
  Map<String, String> _activitySensitivity = {};

  // Pending slot (IDR-019 two-slot model)
  int _pendingVersion = 0;
  Map<String, dynamic>? _pendingJson;

  ConfigStore(this._eventStore);

  /// Load persisted config from SQLite into memory cache.
  /// Loads current config. Pending config stays in SQLite until promoted.
  Future<void> init() async {
    final stored = await _eventStore.getConfigPackage();
    if (stored != null) {
      final version = stored['version'] as int;
      final json = jsonDecode(stored['package_json'] as String) as Map<String, dynamic>;
      _applyToCache(version, json);
    }
    // Check for pending config in SQLite
    final pending = await _eventStore.getPendingConfigPackage();
    if (pending != null) {
      _pendingVersion = pending['version'] as int;
      _pendingJson = jsonDecode(pending['package_json'] as String) as Map<String, dynamic>;
    }
  }

  /// Apply a new config package from sync.
  /// If no current config exists, promotes immediately.
  /// Otherwise, stores as pending (two-slot model).
  Future<void> applyConfig(Map<String, dynamic> packageJson) async {
    final version = packageJson['version'] as int;
    final encoded = jsonEncode(packageJson);

    if (_configVersion == 0) {
      // No current config — promote immediately
      await _eventStore.saveConfigPackage(version, encoded);
      _applyToCache(version, packageJson);
    } else {
      // Store as pending — will be promoted at next safe transition point
      await _eventStore.savePendingConfigPackage(version, encoded);
      _pendingVersion = version;
      _pendingJson = packageJson;
    }
  }

  /// Promote pending config to current, if one exists.
  /// Called at safe transition points (form open, app refresh).
  Future<void> promotePending() async {
    if (_pendingJson == null) return;

    final encoded = jsonEncode(_pendingJson);
    await _eventStore.saveConfigPackage(_pendingVersion, encoded);
    await _eventStore.deletePendingConfigPackage();
    _applyToCache(_pendingVersion, _pendingJson!);
    _pendingVersion = 0;
    _pendingJson = null;
  }

  /// Whether a pending config is waiting for promotion.
  bool get hasPending => _pendingJson != null;

  void _applyToCache(int version, Map<String, dynamic> packageJson) {
    final shapesRaw = packageJson['shapes'];
    final activitiesRaw = packageJson['activities'];
    final expressionsRaw = packageJson['expressions'];

    final shapesMap = shapesRaw is Map ? Map<String, dynamic>.from(shapesRaw) : <String, dynamic>{};
    final activitiesMap = activitiesRaw is Map ? Map<String, dynamic>.from(activitiesRaw) : <String, dynamic>{};
    final expressionsMap = expressionsRaw is Map ? Map<String, dynamic>.from(expressionsRaw) : <String, dynamic>{};

    final parsedShapes = <String, ShapeDefinition>{};
    for (final entry in shapesMap.entries) {
      parsedShapes[entry.key] =
          ShapeDefinition.fromConfigJson(entry.key, Map<String, dynamic>.from(entry.value as Map));
    }

    final parsedActivities = <String, Map<String, dynamic>>{};
    for (final entry in activitiesMap.entries) {
      parsedActivities[entry.key] = Map<String, dynamic>.from(entry.value as Map);
    }

    final parsedExpressions = <String, List<Map<String, dynamic>>>{};
    for (final entry in expressionsMap.entries) {
      final rules = (entry.value as List)
          .map((r) => Map<String, dynamic>.from(r as Map))
          .toList();
      parsedExpressions[entry.key] = rules;
    }

    // Atomic swap
    _configVersion = version;
    _shapes = parsedShapes;
    _activities = parsedActivities;
    _expressions = parsedExpressions;

    // Sensitivity classifications (IDR-019). Defaults: shape='standard', activity='routine'.
    final sensRaw = packageJson['sensitivity_classifications'];
    final parsedShapeSens = <String, String>{};
    final parsedActivitySens = <String, String>{};
    if (sensRaw is Map) {
      final shapesSens = sensRaw['shapes'];
      if (shapesSens is Map) {
        for (final entry in shapesSens.entries) {
          parsedShapeSens[entry.key as String] = entry.value as String;
        }
      }
      final actsSens = sensRaw['activities'];
      if (actsSens is Map) {
        for (final entry in actsSens.entries) {
          parsedActivitySens[entry.key as String] = entry.value as String;
        }
      }
    }
    _shapeSensitivity = parsedShapeSens;
    _activitySensitivity = parsedActivitySens;
  }

  /// Current config version (0 if no config loaded).
  int get configVersion => _configVersion;

  /// Get a shape by ref (e.g. "household_visit/v1"). Returns null if not found.
  ShapeDefinition? getShape(String shapeRef) => _shapes[shapeRef];

  /// Get an activity config by name. Returns null if not found.
  Map<String, dynamic>? getActivity(String name) => _activities[name];

  /// Names of all active activities.
  List<String> getActiveActivities() => _activities.entries
      .where((e) => (e.value['status'] as String?) == 'active')
      .map((e) => e.key)
      .toList();

  /// Get shapes referenced by an activity.
  List<ShapeDefinition> getShapesForActivity(String activityName) {
    final activity = _activities[activityName];
    if (activity == null) return [];
    final shapeRefs = (activity['shapes'] as List?)?.cast<String>() ?? [];
    return shapeRefs
        .map((ref) => _shapes[ref])
        .whereType<ShapeDefinition>()
        .toList();
  }

  /// All loaded shapes.
  Map<String, ShapeDefinition> get allShapes => Map.unmodifiable(_shapes);

  /// Get all expression rules for a field within an activity+shape combination.
  List<Map<String, dynamic>> getExpressionsForField(
      String activityRef, String shapeRef, String fieldName) {
    final key = '$activityRef.$shapeRef';
    final rules = _expressions[key];
    if (rules == null) return [];
    return rules.where((r) => r['field_name'] == fieldName).toList();
  }

  /// Get the show_condition expression for a field. Returns the expression node or null.
  Map<String, dynamic>? getShowCondition(
      String activityRef, String shapeRef, String fieldName) {
    final rules = getExpressionsForField(activityRef, shapeRef, fieldName);
    for (final rule in rules) {
      if (rule['rule_type'] == 'show_condition') {
        return rule['expression'] as Map<String, dynamic>?;
      }
    }
    return null;
  }

  /// Get the default expression for a field. Returns the expression node or null.
  Map<String, dynamic>? getDefaultExpression(
      String activityRef, String shapeRef, String fieldName) {
    final rules = getExpressionsForField(activityRef, shapeRef, fieldName);
    for (final rule in rules) {
      if (rule['rule_type'] == 'default') {
        return rule['expression'] as Map<String, dynamic>?;
      }
    }
    return null;
  }

  /// Get the warning expression for a field. Returns the expression node or null.
  Map<String, dynamic>? getWarningExpression(
      String activityRef, String shapeRef, String fieldName) {
    final rules = getExpressionsForField(activityRef, shapeRef, fieldName);
    for (final rule in rules) {
      if (rule['rule_type'] == 'warning') {
        return rule['expression'] as Map<String, dynamic>?;
      }
    }
    return null;
  }

  /// Get the warning message for a field (from the rule, not the expression).
  String? getWarningMessage(
      String activityRef, String shapeRef, String fieldName) {
    final rules = getExpressionsForField(activityRef, shapeRef, fieldName);
    for (final rule in rules) {
      if (rule['rule_type'] == 'warning') {
        return rule['message'] as String?;
      }
    }
    return null;
  }

  /// Sensitivity classification for a shape (e.g. 'standard', 'elevated', 'restricted').
  /// Returns 'standard' if the shape has no explicit classification.
  String getShapeSensitivity(String shapeRef) =>
      _shapeSensitivity[shapeRef] ?? 'standard';

  /// Sensitivity classification for an activity (e.g. 'routine', 'elevated', 'restricted').
  /// Returns 'routine' if the activity has no explicit classification.
  String getActivitySensitivity(String activityName) =>
      _activitySensitivity[activityName] ?? 'routine';
}
