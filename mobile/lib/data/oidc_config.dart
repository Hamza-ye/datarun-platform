class OidcClientConfig {
  final Uri authorizationEndpoint;
  final Uri tokenEndpoint;
  final String clientId;
  final Uri redirectUri;
  final List<String> scopes;

  const OidcClientConfig({
    required this.authorizationEndpoint,
    required this.tokenEndpoint,
    required this.clientId,
    required this.redirectUri,
    this.scopes = const ['openid', 'profile'],
  });

  Map<String, dynamic> toJson() => {
    'authorization_endpoint': authorizationEndpoint.toString(),
    'token_endpoint': tokenEndpoint.toString(),
    'client_id': clientId,
    'redirect_uri': redirectUri.toString(),
    'scopes': scopes,
  };

  factory OidcClientConfig.fromJson(Map<String, dynamic> json) {
    final scopes = json['scopes'] as List<dynamic>?;
    return OidcClientConfig(
      authorizationEndpoint: Uri.parse(
        json['authorization_endpoint'] as String,
      ),
      tokenEndpoint: Uri.parse(json['token_endpoint'] as String),
      clientId: json['client_id'] as String,
      redirectUri: Uri.parse(json['redirect_uri'] as String),
      scopes: scopes == null
          ? const ['openid', 'profile']
          : scopes.map((scope) => scope as String).toList(),
    );
  }
}
