import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:datarun_mobile/data/auth_service.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/oidc_config.dart';
import 'package:datarun_mobile/domain/event.dart';

class SyncResult {
  final int pushedCount;
  final int pulledCount;
  final String? error;

  SyncResult({
    required this.pushedCount,
    required this.pulledCount,
    this.error,
  });
}

class ActorSwitchResult {
  final String? actorId;
  final String? error;

  const ActorSwitchResult._({this.actorId, this.error});

  const ActorSwitchResult.switched(String actorId) : this._(actorId: actorId);

  const ActorSwitchResult.failed(String error) : this._(error: error);

  bool get success => error == null;
}

class SyncService {
  final EventStore _eventStore;
  final DeviceIdentity _identity;
  final String _baseUrl;
  final ConfigStore _configStore;
  final http.Client _client;
  final MobileAuthService? _authService;
  final DateTime Function() _now;

  SyncService(
    this._eventStore,
    this._identity,
    this._baseUrl,
    this._configStore, {
    http.Client? client,
    MobileAuthService? authService,
    DateTime Function()? now,
  }) : _client = client ?? http.Client(),
       _authService = authService,
       _now = now ?? DateTime.now;

  Future<SyncResult> sync() async {
    int pushed = 0;
    int pulled = 0;
    var session = _identity.activeSession;
    if (session == null) {
      return SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: 'No active actor session',
      );
    }
    final refreshResult = await _refreshExpiredCredential(session);
    if (refreshResult.error != null) {
      return SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: refreshResult.error,
      );
    }
    session = refreshResult.session!;
    final actorResult = await _refreshActorIdentity(session);
    if (actorResult != null) {
      return actorResult;
    }

    // Push phase
    try {
      final activeError = _activeSessionError(session);
      if (activeError != null) {
        return SyncResult(pushedCount: 0, pulledCount: 0, error: activeError);
      }
      final unpushed = await _eventStore.getUnpushed();
      if (unpushed.isNotEmpty) {
        final lastPullWatermark = _identity.syncWatermark;
        final pushHeaders = <String, String>{
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ${session.token}',
        };
        final response = await _client.post(
          Uri.parse('$_baseUrl/api/sync/push'),
          headers: pushHeaders,
          body: jsonEncode({
            'events': unpushed.map((e) => e.toEnvelope()).toList(),
            'device_id': _identity.deviceId,
            'last_pull_watermark': lastPullWatermark,
          }),
        );
        if (response.statusCode == 200) {
          final body = jsonDecode(response.body) as Map<String, dynamic>;
          pushed = (body['accepted'] as int) + (body['duplicates'] as int);
          await _eventStore.markPushed(unpushed.map((e) => e.id).toList());
        } else {
          return SyncResult(
            pushedCount: 0,
            pulledCount: 0,
            error: 'Push failed: ${response.statusCode}',
          );
        }
      }
    } on Exception {
      return SyncResult(pushedCount: 0, pulledCount: 0, error: 'No connection');
    }

    // Pull phase
    try {
      final activeError = _activeSessionError(session);
      if (activeError != null) {
        return SyncResult(
          pushedCount: pushed,
          pulledCount: pulled,
          error: activeError,
        );
      }
      var watermark = _identity.syncWatermark;

      // Build pull headers: include actor token if available (Phase 2b auth)
      final pullHeaders = <String, String>{
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${session.token}',
      };

      while (true) {
        final activeError = _activeSessionError(session);
        if (activeError != null) {
          return SyncResult(
            pushedCount: pushed,
            pulledCount: pulled,
            error: activeError,
          );
        }
        final response = await _client.post(
          Uri.parse('$_baseUrl/api/sync/pull'),
          headers: pullHeaders,
          body: jsonEncode({
            'since_watermark': watermark,
            'limit': 100,
            'device_id': _identity.deviceId,
            'config_version': _configStore.configVersion,
          }),
        );
        if (response.statusCode == 401) {
          return SyncResult(
            pushedCount: pushed,
            pulledCount: pulled,
            error: 'Unauthorized — invalid or missing actor token',
          );
        }
        if (response.statusCode != 200) break;

        final body = jsonDecode(response.body) as Map<String, dynamic>;
        final events = (body['events'] as List)
            .map((e) => Event.fromServerJson(e as Map<String, dynamic>))
            .toList();

        if (events.isEmpty) break;

        for (final event in events) {
          await _eventStore.insertFromServer(event);
          // Process subjects_merged events to update local alias table.
          // ADR-007 S3: discriminate identity lifecycle by shape_ref.
          if (event.shapeRef.startsWith('subjects_merged/')) {
            final retiredId = event.payload['retired_id'] as String?;
            final survivingId = event.payload['surviving_id'] as String?;
            if (retiredId != null && survivingId != null) {
              await _eventStore.upsertAlias(
                retiredId,
                survivingId,
                event.timestamp,
              );
            }
          }
          // Process assignment events to maintain local scope knowledge (Phase 2b).
          if (event.type == 'assignment_changed') {
            await _eventStore.processAssignmentEvent(event);
          }
        }
        pulled += events.length;

        final latestWatermark = body['latest_watermark'] as int;
        watermark = latestWatermark;
        await _identity.setSyncWatermark(watermark);

        if (events.length < 100) break; // Last page
      }
    } on Exception {
      // Pull errors are non-fatal — pushed data is already safe
    }

    // Config download: fetch new config if pull response indicated a newer version
    try {
      final activeError = _activeSessionError(session);
      if (activeError != null) {
        return SyncResult(
          pushedCount: pushed,
          pulledCount: pulled,
          error: activeError,
        );
      }
      final configHeaders = <String, String>{
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${session.token}',
      };
      final currentVersion = _configStore.configVersion;
      if (currentVersion > 0) {
        configHeaders['If-None-Match'] = '$currentVersion';
      }
      final configResponse = await _client.get(
        Uri.parse('$_baseUrl/api/sync/config'),
        headers: configHeaders,
      );
      if (configResponse.statusCode == 200) {
        final configBody =
            jsonDecode(configResponse.body) as Map<String, dynamic>;
        await _configStore.applyConfig(configBody);
      }
      // 304 Not Modified — skip
    } on Exception {
      // Config download errors are non-fatal
    }

    // Selective-retain: purge out-of-scope events from other actors (Phase 2c)
    if (pulled > 0) {
      try {
        await _eventStore.purgeOutOfScopeEvents(_identity.deviceId);
      } on Exception {
        // Purge errors are non-fatal
      }
    }

    return SyncResult(pushedCount: pushed, pulledCount: pulled);
  }

  Future<ActorSwitchResult> switchActorSession(
    String token, {
    String? refreshToken,
    DateTime? tokenExpiresAt,
    OidcClientConfig? oidcConfig,
  }) async {
    final priorSession = _identity.activeSession;
    if (priorSession != null) {
      await _drainCurrentActorForSwitch(priorSession);
    }

    try {
      final actorId = await _resolveActorId(token);
      await _identity.activateActorSession(
        actorId: actorId,
        token: token,
        serverUrl: _baseUrl,
        refreshToken: refreshToken,
        tokenExpiresAt: tokenExpiresAt,
        oidcConfig: oidcConfig,
      );
      return ActorSwitchResult.switched(actorId);
    } on _ActorUnauthorizedException {
      return const ActorSwitchResult.failed(
        'Unauthorized — invalid or missing actor token',
      );
    } on _ActorIdentityException catch (e) {
      return ActorSwitchResult.failed(e.message);
    } on Exception {
      return const ActorSwitchResult.failed('No connection');
    }
  }

  Future<ActorSwitchResult> switchToProviderCredential(
    ProviderCredential credential, {
    OidcClientConfig? oidcConfig,
  }) {
    return switchActorSession(
      credential.accessToken,
      refreshToken: credential.refreshToken,
      tokenExpiresAt: credential.expiresAt,
      oidcConfig: oidcConfig,
    );
  }

  Future<void> _drainCurrentActorForSwitch(ActorSession session) async {
    try {
      final activeError = _activeSessionError(session);
      if (activeError != null) return;

      final actorId = await _resolveActorId(session.token);
      if (actorId != session.actorId) return;

      final unpushed = await _eventStore.getUnpushed();
      if (unpushed.isEmpty) return;

      final response = await _client.post(
        Uri.parse('$_baseUrl/api/sync/push'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ${session.token}',
        },
        body: jsonEncode({
          'events': unpushed.map((e) => e.toEnvelope()).toList(),
          'device_id': _identity.deviceId,
          'last_pull_watermark': _identity.syncWatermark,
        }),
      );
      if (response.statusCode == 200) {
        await _eventStore.markPushed(unpushed.map((e) => e.id).toList());
      }
    } on Exception {
      // Switch can still proceed: unpushed prior-actor data remains sealed in
      // its actor partition for same-actor resume or future recovery policy.
    }
  }

  Future<SyncResult?> _refreshActorIdentity(ActorSession session) async {
    try {
      final actorId = await _resolveActorId(session.token);
      if (actorId != session.actorId) {
        return SyncResult(
          pushedCount: 0,
          pulledCount: 0,
          error: 'Actor identity changed; switch required',
        );
      }
      return null;
    } on _ActorUnauthorizedException {
      return SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: 'Unauthorized — invalid or missing actor token',
      );
    } on _ActorIdentityException catch (e) {
      return SyncResult(pushedCount: 0, pulledCount: 0, error: e.message);
    } on Exception {
      return SyncResult(pushedCount: 0, pulledCount: 0, error: 'No connection');
    }
  }

  Future<_SessionRefreshResult> _refreshExpiredCredential(
    ActorSession session,
  ) async {
    if (!session.isExpired(_now().toUtc())) {
      return _SessionRefreshResult.success(session);
    }
    final authService = _authService;
    if (authService == null) {
      return const _SessionRefreshResult.failed('Needs sign-in to sync');
    }
    final refresh = await authService.refreshActiveSession(serverUrl: _baseUrl);
    if (!refresh.success) {
      return _SessionRefreshResult.failed(
        refresh.error ?? 'Needs sign-in to sync',
      );
    }
    final refreshedSession = _identity.activeSession;
    if (refreshedSession == null) {
      return const _SessionRefreshResult.failed('Needs sign-in to sync');
    }
    if (refreshedSession.actorId != session.actorId) {
      return const _SessionRefreshResult.failed(
        'Actor identity changed; switch required',
      );
    }
    return _SessionRefreshResult.success(refreshedSession);
  }

  Future<String> _resolveActorId(String token) async {
    final response = await _client.get(
      Uri.parse('$_baseUrl/api/auth/me'),
      headers: {'Authorization': 'Bearer $token'},
    );
    if (response.statusCode == 401) {
      throw const _ActorUnauthorizedException();
    }
    if (response.statusCode != 200) {
      throw _ActorIdentityException(
        'Actor identity check failed: ${response.statusCode}',
      );
    }
    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final actorId = body['actor_id'] as String?;
    if (actorId == null || actorId.isEmpty) {
      throw const _ActorIdentityException(
        'Actor identity check failed: missing actor_id',
      );
    }
    return actorId;
  }

  String? _activeSessionError(ActorSession session) {
    if (_identity.activeActorId != session.actorId ||
        _identity.actorToken != session.token) {
      return 'Active actor session changed during sync';
    }
    if (_eventStore.actorId != null && _eventStore.actorId != session.actorId) {
      return 'EventStore actor partition is not active';
    }
    return null;
  }
}

class _ActorIdentityException implements Exception {
  final String message;

  const _ActorIdentityException(this.message);
}

class _ActorUnauthorizedException implements Exception {
  const _ActorUnauthorizedException();
}

class _SessionRefreshResult {
  final ActorSession? session;
  final String? error;

  const _SessionRefreshResult._({this.session, this.error});

  const _SessionRefreshResult.success(ActorSession session)
    : this._(session: session);

  const _SessionRefreshResult.failed(String error) : this._(error: error);
}
