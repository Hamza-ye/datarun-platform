import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

class DeviceIdentity {
  static const _deviceIdKey = 'device_id';
  static const _deviceSeqKey = 'device_seq';
  static const _actorIdKey = 'actor_id';
  static const _actorTokenKey = 'actor_token';

  final SharedPreferences _prefs;

  DeviceIdentity._(this._prefs);

  static Future<DeviceIdentity> init() async {
    final prefs = await SharedPreferences.getInstance();
    final identity = DeviceIdentity._(prefs);

    // Generate device_id on first launch
    if (prefs.getString(_deviceIdKey) == null) {
      await prefs.setString(_deviceIdKey, const Uuid().v4());
    }

    // Phase 0: hardcoded single-user actor
    if (prefs.getString(_actorIdKey) == null) {
      await prefs.setString(_actorIdKey, const Uuid().v4());
    }

    // Initialize device_seq if not set
    if (prefs.getInt(_deviceSeqKey) == null) {
      await prefs.setInt(_deviceSeqKey, 0);
    }

    return identity;
  }

  String get deviceId => _prefs.getString(_deviceIdKey)!;

  String get actorId => _prefs.getString(_actorIdKey)!;

  String? get actorToken => _prefs.getString(_actorTokenKey);

  Future<void> setActorToken(String token) async {
    await _prefs.setString(_actorTokenKey, token);
  }

  /// Returns the next device_seq and persists it.
  /// Monotonically increasing, starting at 1.
  Future<int> nextSeq() async {
    final current = _prefs.getInt(_deviceSeqKey) ?? 0;
    final next = current + 1;
    await _prefs.setInt(_deviceSeqKey, next);
    return next;
  }
}
