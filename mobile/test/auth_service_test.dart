import 'dart:convert';

import 'package:datarun_mobile/data/auth_service.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/oidc_config.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  const actorA = '11111111-1111-1111-1111-111111111111';
  const actorB = '22222222-2222-2222-2222-222222222222';
  final oidcConfig = OidcClientConfig(
    authorizationEndpoint: Uri.parse('https://provider.test/oauth/authorize'),
    tokenEndpoint: Uri.parse('https://provider.test/oauth/token'),
    clientId: 'datarun-mobile',
    redirectUri: Uri.parse('dev.datarun.mobile://oauth2redirect'),
  );

  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  test(
    'provider credential activates only after auth me resolves actor',
    () async {
      final identity = await DeviceIdentity.init();
      var tokenExchangeComplete = false;
      final handoff = _handoff(
        oidcConfig,
        onTokenRequest: (_) {
          tokenExchangeComplete = true;
          return _tokenResponse('access-token', refreshToken: 'refresh-token');
        },
      );
      final authClient = MockClient((request) async {
        expect(tokenExchangeComplete, isTrue);
        expect(request.url.path, '/api/auth/me');
        expect(request.headers['Authorization'], 'Bearer access-token');
        return http.Response(
          jsonEncode({'actor_id': actorA, 'auth_source': 'oidc-jwks'}),
          200,
          headers: {'content-type': 'application/json'},
        );
      });

      final result = await MobileAuthService(
        identity,
        oidcHandoff: handoff,
        client: authClient,
      ).signInWithOidc(serverUrl: 'http://server.test', oidcConfig: oidcConfig);

      expect(result.success, isTrue);
      expect(result.actorId, actorA);
      expect(identity.actorId, actorA);
      expect(identity.actorToken, 'access-token');
      expect(identity.actorRefreshTokenFor(actorA), 'refresh-token');
      expect(identity.actorOidcConfigFor(actorA)?.clientId, 'datarun-mobile');
    },
  );

  test('unbound credential creates no writable actor session', () async {
    final identity = await DeviceIdentity.init();
    final handoff = _handoff(
      oidcConfig,
      onTokenRequest: (_) => _tokenResponse(
        'unbound-access-token',
        refreshToken: 'unbound-refresh-token',
      ),
    );
    final authClient = MockClient((request) async {
      expect(request.url.path, '/api/auth/me');
      return http.Response(
        jsonEncode({'error': 'unbound'}),
        401,
        headers: {'content-type': 'application/json'},
      );
    });

    final result = await MobileAuthService(
      identity,
      oidcHandoff: handoff,
      client: authClient,
    ).signInWithOidc(serverUrl: 'http://server.test', oidcConfig: oidcConfig);

    expect(result.success, isFalse);
    expect(identity.activeSession, isNull);
    expect(identity.knownActorIds, isEmpty);

    final prefs = await SharedPreferences.getInstance();
    final storedValues = prefs.getKeys().map((key) => '${prefs.get(key)}');
    expect(storedValues.join('\n'), isNot(contains('unbound-access-token')));
    expect(storedValues.join('\n'), isNot(contains('unbound-refresh-token')));
  });

  test('missing actor response creates no writable actor session', () async {
    final identity = await DeviceIdentity.init();
    final handoff = _handoff(
      oidcConfig,
      onTokenRequest: (_) => _tokenResponse('missing-actor-access-token'),
    );
    final authClient = MockClient((request) async {
      expect(request.url.path, '/api/auth/me');
      return http.Response(
        jsonEncode({'auth_source': 'oidc-jwks'}),
        200,
        headers: {'content-type': 'application/json'},
      );
    });

    final result = await MobileAuthService(
      identity,
      oidcHandoff: handoff,
      client: authClient,
    ).signInWithOidc(serverUrl: 'http://server.test', oidcConfig: oidcConfig);

    expect(result.success, isFalse);
    expect(result.error, contains('missing actor_id'));
    expect(identity.activeSession, isNull);
    expect(identity.knownActorIds, isEmpty);
  });

  test('refresh updates token only when auth me resolves same actor', () async {
    final identity = await DeviceIdentity.init();
    await identity.activateActorSession(
      actorId: actorA,
      token: 'old-access-token',
      refreshToken: 'old-refresh-token',
      tokenExpiresAt: DateTime.utc(2026, 6, 19, 8),
      oidcConfig: oidcConfig,
    );

    final handoff = _handoff(
      oidcConfig,
      onTokenRequest: (request) {
        final form = Uri.splitQueryString(request.body);
        expect(form['grant_type'], 'refresh_token');
        expect(form['refresh_token'], 'old-refresh-token');
        return _tokenResponse(
          'new-access-token',
          refreshToken: 'new-refresh-token',
        );
      },
    );
    final authClient = MockClient((request) async {
      expect(request.url.path, '/api/auth/me');
      expect(request.headers['Authorization'], 'Bearer new-access-token');
      return http.Response(
        jsonEncode({'actor_id': actorA}),
        200,
        headers: {'content-type': 'application/json'},
      );
    });

    final result = await MobileAuthService(
      identity,
      oidcHandoff: handoff,
      client: authClient,
    ).refreshActiveSession(serverUrl: 'http://server.test');

    expect(result.success, isTrue);
    expect(identity.actorId, actorA);
    expect(identity.actorToken, 'new-access-token');
    expect(identity.actorRefreshTokenFor(actorA), 'new-refresh-token');
  });

  test(
    'refresh resolving another actor leaves prior session unchanged',
    () async {
      final identity = await DeviceIdentity.init();
      await identity.activateActorSession(
        actorId: actorA,
        token: 'old-access-token',
        refreshToken: 'old-refresh-token',
        tokenExpiresAt: DateTime.utc(2026, 6, 19, 8),
        oidcConfig: oidcConfig,
      );

      final handoff = _handoff(
        oidcConfig,
        onTokenRequest: (_) => _tokenResponse('other-access-token'),
      );
      final authClient = MockClient((request) async {
        expect(request.headers['Authorization'], 'Bearer other-access-token');
        return http.Response(
          jsonEncode({'actor_id': actorB}),
          200,
          headers: {'content-type': 'application/json'},
        );
      });

      final result = await MobileAuthService(
        identity,
        oidcHandoff: handoff,
        client: authClient,
      ).refreshActiveSession(serverUrl: 'http://server.test');

      expect(result.success, isFalse);
      expect(result.error, contains('Actor identity changed'));
      expect(identity.actorId, actorA);
      expect(identity.actorToken, 'old-access-token');
      expect(identity.actorRefreshTokenFor(actorA), 'old-refresh-token');
    },
  );
}

OidcPkceAuthHandoff _handoff(
  OidcClientConfig config, {
  required http.Response Function(http.Request request) onTokenRequest,
}) {
  final externalUserAgent = _FakeExternalUserAgent((request) {
    final state = request.authorizationUri.queryParameters['state']!;
    return request.redirectUri.replace(
      queryParameters: {'code': 'authorization-code', 'state': state},
    );
  });
  final tokenClient = MockClient((request) async {
    expect(request.url, config.tokenEndpoint);
    return onTokenRequest(request);
  });
  return OidcPkceAuthHandoff(
    externalUserAgent: externalUserAgent,
    client: tokenClient,
    now: () => DateTime.utc(2026, 6, 19, 9),
    codeVerifierFactory: () => 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQ',
    stateFactory: () => 'state-123',
  );
}

http.Response _tokenResponse(String accessToken, {String? refreshToken}) {
  return http.Response(
    jsonEncode({
      'access_token': accessToken,
      'refresh_token': ?refreshToken,
      'expires_in': 3600,
    }),
    200,
    headers: {'content-type': 'application/json'},
  );
}

class _FakeExternalUserAgent implements ExternalUserAgent {
  _FakeExternalUserAgent(this.callback);

  final Uri Function(ExternalUserAgentRequest request) callback;

  @override
  Future<Uri> authorize(ExternalUserAgentRequest request) async {
    return callback(request);
  }
}
