import 'dart:convert';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/shape.dart';

/// Stores and caches the config package (IDR-019).
/// Persists to SQLite via EventStore; keeps parsed shapes/activities in memory.
class ConfigStore {
  final EventStore _eventStore;

  int _configVersion = 0;
  Map<String, ShapeDefinition> _shapes = {};
  Map<String, Map<String, dynamic>> _activities = {};

  ConfigStore(this._eventStore);

  /// Load persisted config from SQLite into memory cache.
  Future<void> init() async {
    final stored = await _eventStore.getConfigPackage();
    if (stored == null) return;
    final version = stored['version'] as int;
    final json = jsonDecode(stored['package_json'] as String) as Map<String, dynamic>;
    _applyToCache(version, json);
  }

  /// Apply a new config package. Persists to SQLite, then updates cache.
  Future<void> applyConfig(Map<String, dynamic> packageJson) async {
    final version = packageJson['version'] as int;
    final encoded = jsonEncode(packageJson);
    await _eventStore.saveConfigPackage(version, encoded);
    _applyToCache(version, packageJson);
  }

  void _applyToCache(int version, Map<String, dynamic> packageJson) {
    final shapesMap = packageJson['shapes'] as Map<String, dynamic>? ?? {};
    final activitiesMap = packageJson['activities'] as Map<String, dynamic>? ?? {};

    final parsedShapes = <String, ShapeDefinition>{};
    for (final entry in shapesMap.entries) {
      parsedShapes[entry.key] =
          ShapeDefinition.fromConfigJson(entry.key, entry.value as Map<String, dynamic>);
    }

    final parsedActivities = <String, Map<String, dynamic>>{};
    for (final entry in activitiesMap.entries) {
      parsedActivities[entry.key] = entry.value as Map<String, dynamic>;
    }

    // Atomic swap
    _configVersion = version;
    _shapes = parsedShapes;
    _activities = parsedActivities;
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
}
