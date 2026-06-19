import 'dart:convert';

import 'package:datarun_mobile/data/auth_service.dart';
import 'package:datarun_mobile/data/oidc_config.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  const verifier = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQ';
  const state = 'state-123';
  final config = OidcClientConfig(
    authorizationEndpoint: Uri.parse('https://provider.test/oauth/authorize'),
    tokenEndpoint: Uri.parse('https://provider.test/oauth/token'),
    clientId: 'datarun-mobile',
    redirectUri: Uri.parse('dev.datarun.mobile://oauth2redirect'),
    scopes: const ['openid', 'profile'],
  );

  test('uses external user-agent authorization-code PKCE handoff', () async {
    late ExternalUserAgentRequest handoffRequest;
    final externalUserAgent = _FakeExternalUserAgent((request) {
      handoffRequest = request;
      expect(request.authorizationUri.scheme, 'https');
      expect(request.authorizationUri.host, 'provider.test');
      expect(request.authorizationUri.queryParameters['response_type'], 'code');
      expect(
        request.authorizationUri.queryParameters['code_challenge_method'],
        'S256',
      );
      expect(
        request.authorizationUri.queryParameters['code_challenge'],
        OidcPkceAuthHandoff.codeChallengeForVerifier(verifier),
      );
      expect(request.authorizationUri.queryParameters['code_verifier'], isNull);
      expect(
        request.authorizationUri.toString(),
        isNot(contains('access-token')),
      );
      return request.redirectUri.replace(
        queryParameters: {'code': 'authorization-code', 'state': state},
      );
    });

    final tokenRequests = <http.Request>[];
    final client = MockClient((request) async {
      tokenRequests.add(request);
      expect(request.url, config.tokenEndpoint);
      expect(request.url.query, isEmpty);
      final form = Uri.splitQueryString(request.body);
      expect(form['grant_type'], 'authorization_code');
      expect(form['code'], 'authorization-code');
      expect(form['code_verifier'], verifier);
      expect(form['client_id'], 'datarun-mobile');
      return http.Response(
        jsonEncode({
          'access_token': 'access-token',
          'refresh_token': 'refresh-token',
          'expires_in': 3600,
          'token_type': 'Bearer',
        }),
        200,
        headers: {'content-type': 'application/json'},
      );
    });

    final handoff = OidcPkceAuthHandoff(
      externalUserAgent: externalUserAgent,
      client: client,
      now: () => DateTime.utc(2026, 6, 19, 9),
      codeVerifierFactory: () => verifier,
      stateFactory: () => state,
    );

    final credential = await handoff.signIn(config);

    expect(credential.accessToken, 'access-token');
    expect(credential.refreshToken, 'refresh-token');
    expect(credential.expiresAt, DateTime.utc(2026, 6, 19, 10));
    expect(handoffRequest.redirectUri, config.redirectUri);
    expect(tokenRequests, hasLength(1));
  });

  test('rejects callback state mismatch before token exchange', () async {
    final externalUserAgent = _FakeExternalUserAgent((request) {
      return request.redirectUri.replace(
        queryParameters: {'code': 'authorization-code', 'state': 'wrong-state'},
      );
    });
    final client = MockClient((request) async {
      fail('Token endpoint must not be called after a state mismatch');
    });
    final handoff = OidcPkceAuthHandoff(
      externalUserAgent: externalUserAgent,
      client: client,
      codeVerifierFactory: () => verifier,
      stateFactory: () => state,
    );

    await expectLater(
      handoff.signIn(config),
      throwsA(
        isA<AuthFlowException>().having(
          (error) => error.message,
          'message',
          allOf(
            contains('could not be verified'),
            isNot(contains('authorization-code')),
          ),
        ),
      ),
    );
  });
}

class _FakeExternalUserAgent implements ExternalUserAgent {
  _FakeExternalUserAgent(this.callback);

  final Uri Function(ExternalUserAgentRequest request) callback;

  @override
  Future<Uri> authorize(ExternalUserAgentRequest request) async {
    return callback(request);
  }
}
