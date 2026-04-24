import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

/// Per-install device state + per-actor provisioning.
///
/// `device_id` is generated on first launch and persists for the life of the
/// install (ADR-002 §S5). `device_seq` is a monotonic per-device counter
/// assigned at capture time, starting at 1.
///
/// Server URL / actor_id / bearer token are set on the Setup screen after
/// copying values from `POST /dev/bootstrap`.
class Prefs {
  static const _kServerUrl = 'server_url';
  static const _kActorId = 'actor_id';
  static const _kToken = 'token';
  static const _kDeviceId = 'device_id';
  static const _kDeviceSeq = 'device_seq';
  static const _kLastPullWatermark = 'last_pull_watermark';

  final SharedPreferences _sp;
  Prefs._(this._sp);

  static Future<Prefs> load() async {
    final sp = await SharedPreferences.getInstance();
    if (!sp.containsKey(_kDeviceId)) {
      await sp.setString(_kDeviceId, const Uuid().v4());
      await sp.setInt(_kDeviceSeq, 0);
    }
    return Prefs._(sp);
  }

  String? get serverUrl => _sp.getString(_kServerUrl);
  String? get actorId => _sp.getString(_kActorId);
  String? get token => _sp.getString(_kToken);
  String get deviceId => _sp.getString(_kDeviceId)!;
  int get lastPullWatermark => _sp.getInt(_kLastPullWatermark) ?? 0;

  bool get isProvisioned =>
      (serverUrl?.isNotEmpty ?? false) &&
      (actorId?.isNotEmpty ?? false) &&
      (token?.isNotEmpty ?? false);

  Future<void> setProvisioning({
    required String serverUrl,
    required String actorId,
    required String token,
  }) async {
    await _sp.setString(_kServerUrl, serverUrl);
    await _sp.setString(_kActorId, actorId);
    await _sp.setString(_kToken, token);
  }

  Future<void> setLastPullWatermark(int wm) =>
      _sp.setInt(_kLastPullWatermark, wm);

  /// Atomically allocate the next device_seq. Starts at 1 on first call.
  Future<int> nextDeviceSeq() async {
    final next = (_sp.getInt(_kDeviceSeq) ?? 0) + 1;
    await _sp.setInt(_kDeviceSeq, next);
    return next;
  }

  Future<void> clear() async {
    await _sp.remove(_kServerUrl);
    await _sp.remove(_kActorId);
    await _sp.remove(_kToken);
    await _sp.remove(_kLastPullWatermark);
    // Intentionally preserve device_id and device_seq across re-provisioning —
    // they describe the physical device, not the actor.
  }
}
