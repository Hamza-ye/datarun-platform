import 'dart:convert';

import 'package:datarun_mobile/data/oidc_config.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

class ActorSession {
  final String actorId;
  final String token;
  final String? refreshToken;
  final DateTime? tokenExpiresAt;
  final OidcClientConfig? oidcConfig;

  const ActorSession({
    required this.actorId,
    required this.token,
    this.refreshToken,
    this.tokenExpiresAt,
    this.oidcConfig,
  });

  bool isExpired(DateTime now) {
    final expiresAt = tokenExpiresAt;
    return expiresAt != null && !expiresAt.isAfter(now.toUtc());
  }
}

class DeviceIdentity {
  static const _deviceIdKey = 'device_id';
  static const _deviceSeqKey = 'device_seq';
  static const _legacyActorIdKey = 'actor_id';
  static const _legacyActorTokenKey = 'actor_token';
  static const _serverUrlKey = 'server_url';
  static const _activeActorIdKey = 'active_actor_id';
  static const _knownActorIdsKey = 'known_actor_ids';
  static const _actorPrefix = 'actor_session';

  final SharedPreferences _prefs;

  DeviceIdentity._(this._prefs);

  static Future<DeviceIdentity> init() async {
    final prefs = await SharedPreferences.getInstance();
    final identity = DeviceIdentity._(prefs);

    // Generate device_id on first launch
    if (prefs.getString(_deviceIdKey) == null) {
      await prefs.setString(_deviceIdKey, const Uuid().v4());
    }

    await identity._migrateLegacySingleActorSession();

    // Initialize device_seq if not set
    if (prefs.getInt(_deviceSeqKey) == null) {
      await prefs.setInt(_deviceSeqKey, 0);
    }

    return identity;
  }

  String get deviceId => _prefs.getString(_deviceIdKey)!;

  String? get activeActorId => _prefs.getString(_activeActorIdKey);

  String get actorId {
    final id = activeActorId;
    if (id == null || id.isEmpty) {
      throw StateError('No active actor session');
    }
    return id;
  }

  String? get actorToken {
    final id = activeActorId;
    if (id == null || id.isEmpty) return null;
    return actorTokenFor(id);
  }

  ActorSession? get activeSession {
    final id = activeActorId;
    if (id == null || id.isEmpty) return null;
    final token = actorTokenFor(id);
    if (token == null || token.isEmpty) return null;
    return ActorSession(
      actorId: id,
      token: token,
      refreshToken: actorRefreshTokenFor(id),
      tokenExpiresAt: actorTokenExpiresAtFor(id),
      oidcConfig: actorOidcConfigFor(id),
    );
  }

  List<String> get knownActorIds =>
      _prefs.getStringList(_knownActorIdsKey) ?? const <String>[];

  String? actorTokenFor(String actorId) =>
      _prefs.getString(_actorTokenKey(actorId));

  String? actorRefreshTokenFor(String actorId) =>
      _prefs.getString(_actorRefreshTokenKey(actorId));

  DateTime? actorTokenExpiresAtFor(String actorId) {
    final raw = _prefs.getString(_actorTokenExpiresAtKey(actorId));
    if (raw == null || raw.isEmpty) return null;
    return DateTime.tryParse(raw)?.toUtc();
  }

  OidcClientConfig? actorOidcConfigFor(String actorId) {
    final raw = _prefs.getString(_actorOidcConfigKey(actorId));
    if (raw == null || raw.isEmpty) return null;
    try {
      return OidcClientConfig.fromJson(jsonDecode(raw) as Map<String, dynamic>);
    } on Exception {
      return null;
    }
  }

  bool hasSessionFor(String actorId) => actorTokenFor(actorId) != null;

  String? get serverUrl => _prefs.getString(_serverUrlKey);

  Future<void> setServerUrl(String url) async {
    await _prefs.setString(_serverUrlKey, url);
  }

  /// Whether the device has been set up (has server URL + token).
  bool get isSetupComplete =>
      _prefs.getString(_serverUrlKey) != null && activeSession != null;

  /// Activates exactly one actor session. The actor id supplied here must have
  /// come from /api/auth/me; callers should not pass UI-selected identities.
  Future<void> activateActorSession({
    required String actorId,
    required String token,
    String? serverUrl,
    String? refreshToken,
    DateTime? tokenExpiresAt,
    OidcClientConfig? oidcConfig,
  }) async {
    if (actorId.isEmpty) {
      throw ArgumentError.value(actorId, 'actorId', 'must not be empty');
    }
    if (token.isEmpty) {
      throw ArgumentError.value(token, 'token', 'must not be empty');
    }

    if (serverUrl != null) {
      await setServerUrl(serverUrl);
    }

    // Crash-safe switch ordering: remove the active pointer before storing the
    // next actor's token, then publish the next active actor as the final step.
    await _prefs.remove(_activeActorIdKey);
    await _prefs.setString(_actorTokenKey(actorId), token);
    if (refreshToken != null && refreshToken.isNotEmpty) {
      await _prefs.setString(_actorRefreshTokenKey(actorId), refreshToken);
    } else {
      await _prefs.remove(_actorRefreshTokenKey(actorId));
    }
    if (tokenExpiresAt != null) {
      await _prefs.setString(
        _actorTokenExpiresAtKey(actorId),
        tokenExpiresAt.toUtc().toIso8601String(),
      );
    } else {
      await _prefs.remove(_actorTokenExpiresAtKey(actorId));
    }
    if (oidcConfig != null) {
      await _prefs.setString(
        _actorOidcConfigKey(actorId),
        jsonEncode(oidcConfig.toJson()),
      );
    } else {
      await _prefs.remove(_actorOidcConfigKey(actorId));
    }
    await _rememberActor(actorId);
    await _prefs.setString(_activeActorIdKey, actorId);
    await _prefs.remove(_legacyActorIdKey);
    await _prefs.remove(_legacyActorTokenKey);
  }

  /// Resumes a previously established actor-local session.
  Future<bool> resumeActorSession(String actorId) async {
    if (!hasSessionFor(actorId)) return false;
    await _prefs.remove(_activeActorIdKey);
    await _prefs.setString(_activeActorIdKey, actorId);
    await _rememberActor(actorId);
    return true;
  }

  Future<void> clearActiveActorSession() async {
    await _prefs.remove(_activeActorIdKey);
  }

  int get syncWatermark => _prefs.getInt(_syncWatermarkKey(actorId)) ?? 0;

  Future<void> setSyncWatermark(int watermark) async {
    await _prefs.setInt(_syncWatermarkKey(actorId), watermark);
  }

  int subjectHistoryCursor({
    required String subjectId,
    required String activityRef,
  }) =>
      _prefs.getInt(
        _subjectHistoryCursorKey(actorId, subjectId, activityRef),
      ) ??
      0;

  Future<void> setSubjectHistoryCursor({
    required String subjectId,
    required String activityRef,
    required int cursor,
  }) async {
    await _prefs.setInt(
      _subjectHistoryCursorKey(actorId, subjectId, activityRef),
      cursor,
    );
  }

  /// Returns the next device_seq and persists it.
  /// Monotonically increasing, starting at 1. This remains device-global
  /// because the server enforces UNIQUE(device_id, device_seq).
  Future<int> nextSeq() async {
    final current = _prefs.getInt(_deviceSeqKey) ?? 0;
    final next = current + 1;
    await _prefs.setInt(_deviceSeqKey, next);
    return next;
  }

  Future<void> _migrateLegacySingleActorSession() async {
    if (_prefs.getString(_activeActorIdKey) != null) return;

    final legacyActorId = _prefs.getString(_legacyActorIdKey);
    if (legacyActorId == null || legacyActorId.isEmpty) return;

    final legacyToken = _prefs.getString(_legacyActorTokenKey);
    if (legacyToken != null && legacyToken.isNotEmpty) {
      await _prefs.setString(_actorTokenKey(legacyActorId), legacyToken);
    }
    await _rememberActor(legacyActorId);
    await _prefs.setString(_activeActorIdKey, legacyActorId);
    await _prefs.remove(_legacyActorIdKey);
    await _prefs.remove(_legacyActorTokenKey);
  }

  Future<void> _rememberActor(String actorId) async {
    final existing = knownActorIds.toSet();
    existing.add(actorId);
    await _prefs.setStringList(_knownActorIdsKey, existing.toList()..sort());
  }

  static String _actorTokenKey(String actorId) =>
      '$_actorPrefix.$actorId.token';

  static String _actorRefreshTokenKey(String actorId) =>
      '$_actorPrefix.$actorId.refresh_token';

  static String _actorTokenExpiresAtKey(String actorId) =>
      '$_actorPrefix.$actorId.token_expires_at';

  static String _actorOidcConfigKey(String actorId) =>
      '$_actorPrefix.$actorId.oidc_config';

  static String _syncWatermarkKey(String actorId) =>
      '$_actorPrefix.$actorId.sync_watermark';

  static String _subjectHistoryCursorKey(
    String actorId,
    String subjectId,
    String activityRef,
  ) => '$_actorPrefix.$actorId.subject_history.$activityRef.$subjectId.cursor';
}
