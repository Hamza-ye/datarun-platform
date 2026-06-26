import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:crypto/crypto.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;

import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/mobile_http_client.dart';
import 'package:datarun_mobile/data/oidc_config.dart';

class ProviderCredential {
  final String accessToken;
  final String? refreshToken;
  final DateTime? expiresAt;

  const ProviderCredential({
    required this.accessToken,
    this.refreshToken,
    this.expiresAt,
  });
}

class AuthSessionResult {
  final String? actorId;
  final String? error;

  const AuthSessionResult._({this.actorId, this.error});

  const AuthSessionResult.signedIn(String actorId) : this._(actorId: actorId);

  const AuthSessionResult.failed(String error) : this._(error: error);

  bool get success => error == null;
}

class ExternalUserAgentRequest {
  final Uri authorizationUri;
  final Uri redirectUri;

  const ExternalUserAgentRequest({
    required this.authorizationUri,
    required this.redirectUri,
  });
}

abstract class ExternalUserAgent {
  Future<Uri> authorize(ExternalUserAgentRequest request);
}

class MethodChannelExternalUserAgent implements ExternalUserAgent {
  static const _channel = MethodChannel('dev.datarun.mobile/auth_handoff');

  const MethodChannelExternalUserAgent();

  @override
  Future<Uri> authorize(ExternalUserAgentRequest request) async {
    final callbackUri = await _channel.invokeMethod<String>('authorize', {
      'authorizationUrl': request.authorizationUri.toString(),
      'redirectUri': request.redirectUri.toString(),
    });
    if (callbackUri == null || callbackUri.isEmpty) {
      throw const AuthFlowException('Sign in was cancelled');
    }
    return Uri.parse(callbackUri);
  }
}

class OidcPkceAuthHandoff {
  final ExternalUserAgent externalUserAgent;
  final http.Client _client;
  final Random _random;
  final DateTime Function() _now;
  final String Function()? _codeVerifierFactory;
  final String Function()? _stateFactory;

  OidcPkceAuthHandoff({
    ExternalUserAgent? externalUserAgent,
    http.Client? client,
    Random? random,
    DateTime Function()? now,
    String Function()? codeVerifierFactory,
    String Function()? stateFactory,
  }) : externalUserAgent =
           externalUserAgent ?? const MethodChannelExternalUserAgent(),
       _client = client ?? createMobileHttpClient(),
       _random = random ?? Random.secure(),
       _now = now ?? DateTime.now,
       _codeVerifierFactory = codeVerifierFactory,
       _stateFactory = stateFactory;

  Future<ProviderCredential> signIn(OidcClientConfig config) async {
    final codeVerifier = _codeVerifierFactory?.call() ?? _randomUrlSafe(32);
    final state = _stateFactory?.call() ?? _randomUrlSafe(24);
    final authorizationUri = _authorizationUri(
      config,
      codeVerifier: codeVerifier,
      state: state,
    );

    final callbackUri = await externalUserAgent.authorize(
      ExternalUserAgentRequest(
        authorizationUri: authorizationUri,
        redirectUri: config.redirectUri,
      ),
    );
    final callbackState = callbackUri.queryParameters['state'];
    if (callbackState != state) {
      throw const AuthFlowException('Sign in response could not be verified');
    }
    final error = callbackUri.queryParameters['error'];
    if (error != null && error.isNotEmpty) {
      throw const AuthFlowException('Provider did not complete sign in');
    }
    final code = callbackUri.queryParameters['code'];
    if (code == null || code.isEmpty) {
      throw const AuthFlowException(
        'Provider did not return an authorization code',
      );
    }

    return _exchangeCode(
      config,
      authorizationCode: code,
      codeVerifier: codeVerifier,
    );
  }

  Future<ProviderCredential> refresh(
    OidcClientConfig config, {
    required String refreshToken,
  }) async {
    final response = await _client.post(
      config.tokenEndpoint,
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: {
        'grant_type': 'refresh_token',
        'refresh_token': refreshToken,
        'client_id': config.clientId,
        if (config.scopes.isNotEmpty) 'scope': config.scopes.join(' '),
      },
    );
    return _parseTokenResponse(response);
  }

  Uri _authorizationUri(
    OidcClientConfig config, {
    required String codeVerifier,
    required String state,
  }) {
    final existing = Map<String, String>.from(
      config.authorizationEndpoint.queryParameters,
    );
    final params = <String, String>{
      ...existing,
      'response_type': 'code',
      'client_id': config.clientId,
      'redirect_uri': config.redirectUri.toString(),
      'scope': config.scopes.join(' '),
      'state': state,
      'code_challenge': codeChallengeForVerifier(codeVerifier),
      'code_challenge_method': 'S256',
    };
    return config.authorizationEndpoint.replace(queryParameters: params);
  }

  Future<ProviderCredential> _exchangeCode(
    OidcClientConfig config, {
    required String authorizationCode,
    required String codeVerifier,
  }) async {
    try {
      final response = await _client.post(
        config.tokenEndpoint,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: {
          'grant_type': 'authorization_code',
          'code': authorizationCode,
          'redirect_uri': config.redirectUri.toString(),
          'client_id': config.clientId,
          'code_verifier': codeVerifier,
        },
      );
      return _parseTokenResponse(response);
    } on AuthFlowException {
      rethrow;
    } on Exception {
      throw const AuthFlowException(
        'Provider token exchange failed: network error',
      );
    }
  }

  ProviderCredential _parseTokenResponse(http.Response response) {
    if (response.statusCode != 200) {
      throw AuthFlowException(
        'Provider token exchange failed: ${response.statusCode}',
      );
    }
    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final accessToken = body['access_token'] as String?;
    if (accessToken == null || accessToken.isEmpty) {
      throw const AuthFlowException('Provider token exchange failed');
    }
    final expiresIn = _expiresIn(body['expires_in']);
    return ProviderCredential(
      accessToken: accessToken,
      refreshToken: body['refresh_token'] as String?,
      expiresAt: expiresIn == null ? null : _now().add(expiresIn),
    );
  }

  Duration? _expiresIn(Object? raw) {
    if (raw is int) return Duration(seconds: raw);
    if (raw is String) {
      final seconds = int.tryParse(raw);
      if (seconds != null) return Duration(seconds: seconds);
    }
    return null;
  }

  String _randomUrlSafe(int byteLength) {
    final bytes = List<int>.generate(byteLength, (_) => _random.nextInt(256));
    return _base64UrlNoPadding(bytes);
  }

  static String codeChallengeForVerifier(String verifier) {
    return _base64UrlNoPadding(sha256.convert(utf8.encode(verifier)).bytes);
  }

  static String _base64UrlNoPadding(List<int> bytes) {
    return base64UrlEncode(bytes).replaceAll('=', '');
  }
}

class MobileAuthService {
  final DeviceIdentity _identity;
  final OidcPkceAuthHandoff _oidcHandoff;
  final http.Client _client;

  MobileAuthService(
    this._identity, {
    OidcPkceAuthHandoff? oidcHandoff,
    http.Client? client,
  }) : _oidcHandoff = oidcHandoff ?? OidcPkceAuthHandoff(),
       _client = client ?? createMobileHttpClient();

  Future<AuthSessionResult> signInWithOidc({
    required String serverUrl,
    required OidcClientConfig oidcConfig,
  }) async {
    try {
      final credential = await obtainOidcCredential(oidcConfig);
      return activateResolvedCredential(
        serverUrl: serverUrl,
        credential: credential,
        oidcConfig: oidcConfig,
      );
    } on AuthFlowException catch (e) {
      return AuthSessionResult.failed(e.message);
    } on Exception {
      return const AuthSessionResult.failed('Sign in could not be completed');
    }
  }

  Future<ProviderCredential> obtainOidcCredential(OidcClientConfig oidcConfig) {
    return _oidcHandoff.signIn(oidcConfig);
  }

  Future<AuthSessionResult> activateResolvedCredential({
    required String serverUrl,
    required ProviderCredential credential,
    OidcClientConfig? oidcConfig,
  }) async {
    try {
      final actorId = await resolveActorForToken(
        serverUrl: serverUrl,
        token: credential.accessToken,
      );
      await _identity.activateActorSession(
        actorId: actorId,
        token: credential.accessToken,
        serverUrl: serverUrl,
        refreshToken: credential.refreshToken,
        tokenExpiresAt: credential.expiresAt,
        oidcConfig: oidcConfig,
      );
      return AuthSessionResult.signedIn(actorId);
    } on AuthFlowException catch (e) {
      return AuthSessionResult.failed(e.message);
    } on Exception {
      return const AuthSessionResult.failed(
        'Actor sign in could not be verified',
      );
    }
  }

  Future<AuthSessionResult> activateDevelopmentBearerCredential({
    required String serverUrl,
    required String token,
  }) {
    return activateResolvedCredential(
      serverUrl: serverUrl,
      credential: ProviderCredential(accessToken: token),
    );
  }

  Future<AuthSessionResult> refreshActiveSession({
    required String serverUrl,
  }) async {
    final session = _identity.activeSession;
    if (session == null) {
      return const AuthSessionResult.failed('Needs sign-in to sync');
    }
    final refreshToken = session.refreshToken;
    final oidcConfig = session.oidcConfig;
    if (refreshToken == null || refreshToken.isEmpty || oidcConfig == null) {
      return const AuthSessionResult.failed('Needs sign-in to sync');
    }

    try {
      final credential = await _oidcHandoff.refresh(
        oidcConfig,
        refreshToken: refreshToken,
      );
      final actorId = await resolveActorForToken(
        serverUrl: serverUrl,
        token: credential.accessToken,
      );
      if (actorId != session.actorId) {
        return const AuthSessionResult.failed(
          'Actor identity changed; switch required',
        );
      }
      await _identity.activateActorSession(
        actorId: session.actorId,
        token: credential.accessToken,
        serverUrl: serverUrl,
        refreshToken: credential.refreshToken ?? refreshToken,
        tokenExpiresAt: credential.expiresAt,
        oidcConfig: oidcConfig,
      );
      return AuthSessionResult.signedIn(session.actorId);
    } on AuthFlowException catch (e) {
      return AuthSessionResult.failed(e.message);
    } on Exception {
      return const AuthSessionResult.failed('Needs sign-in to sync');
    }
  }

  Future<String> resolveActorForToken({
    required String serverUrl,
    required String token,
  }) async {
    final http.Response response;
    try {
      response = await _client.get(
        Uri.parse('$serverUrl/api/auth/me'),
        headers: {'Authorization': 'Bearer $token'},
      );
    } on Exception {
      throw const AuthFlowException(
        'Actor identity check failed: network error',
      );
    }
    if (response.statusCode == 401 || response.statusCode == 403) {
      throw const AuthFlowException('Needs sign-in to sync');
    }
    if (response.statusCode != 200) {
      throw AuthFlowException(
        'Actor identity check failed: ${response.statusCode}',
      );
    }
    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final actorId = body['actor_id'] as String?;
    if (actorId == null || actorId.isEmpty) {
      throw const AuthFlowException(
        'Actor identity check failed: missing actor_id',
      );
    }
    return actorId;
  }
}

class AuthFlowException implements Exception {
  final String message;

  const AuthFlowException(this.message);

  @override
  String toString() => message;
}
