import 'package:flutter/material.dart';
import 'package:datarun_mobile/data/auth_service.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/oidc_config.dart';

typedef CredentialActivator =
    Future<AuthSessionResult> Function({
      required String serverUrl,
      required ProviderCredential credential,
      OidcClientConfig? oidcConfig,
    });

/// Sign-in screen. The primary path is external-user-agent OIDC with PKCE.
class SetupScreen extends StatefulWidget {
  final DeviceIdentity identity;
  final VoidCallback onSetupComplete;
  final MobileAuthService? authService;
  final CredentialActivator? credentialActivator;
  final String title;

  const SetupScreen({
    super.key,
    required this.identity,
    required this.onSetupComplete,
    this.authService,
    this.credentialActivator,
    this.title = 'Sign in',
  });

  @override
  State<SetupScreen> createState() => _SetupScreenState();
}

class _SetupScreenState extends State<SetupScreen> {
  static const _defaultRedirectUri = 'dev.datarun.mobile://oauth2redirect';

  final _oidcFormKey = GlobalKey<FormState>();
  final _devFormKey = GlobalKey<FormState>();
  final _urlController = TextEditingController(text: 'http://10.0.2.2:8080');
  final _authorizationEndpointController = TextEditingController();
  final _tokenEndpointController = TextEditingController();
  final _clientIdController = TextEditingController();
  final _redirectUriController = TextEditingController(
    text: _defaultRedirectUri,
  );
  final _scopesController = TextEditingController(text: 'openid profile');
  final _tokenController = TextEditingController();
  bool _saving = false;

  MobileAuthService get _authService =>
      widget.authService ?? MobileAuthService(widget.identity);

  @override
  void dispose() {
    _urlController.dispose();
    _authorizationEndpointController.dispose();
    _tokenEndpointController.dispose();
    _clientIdController.dispose();
    _redirectUriController.dispose();
    _scopesController.dispose();
    _tokenController.dispose();
    super.dispose();
  }

  Future<void> _signIn() async {
    if (!_oidcFormKey.currentState!.validate()) return;

    setState(() => _saving = true);

    final serverUrl = _serverUrl();
    final oidcConfig = _oidcConfig();

    AuthSessionResult result;
    try {
      final credential = await _authService.obtainOidcCredential(oidcConfig);
      result = await _activateCredential(
        serverUrl: serverUrl,
        credential: credential,
        oidcConfig: oidcConfig,
      );
    } on AuthFlowException catch (e) {
      result = AuthSessionResult.failed(e.message);
    } on Exception {
      result = const AuthSessionResult.failed('Sign in could not be completed');
    }

    if (!mounted) return;
    setState(() => _saving = false);

    if (result.success) {
      widget.onSetupComplete();
      return;
    }
    _showError(result.error ?? 'Sign in could not be completed');
  }

  Future<void> _saveDevelopmentBearer() async {
    if (!_devFormKey.currentState!.validate()) return;
    final serverError = _requiredUri(_urlController.text);
    if (serverError != null) {
      _showError('Datarun server URL is required');
      return;
    }

    setState(() => _saving = true);

    final result = await _activateCredential(
      serverUrl: _serverUrl(),
      credential: ProviderCredential(accessToken: _tokenController.text.trim()),
    );

    if (!mounted) return;
    setState(() => _saving = false);

    if (result.success) {
      widget.onSetupComplete();
      return;
    }
    _showError(result.error ?? 'Could not verify development credential');
  }

  Future<AuthSessionResult> _activateCredential({
    required String serverUrl,
    required ProviderCredential credential,
    OidcClientConfig? oidcConfig,
  }) {
    final activator = widget.credentialActivator;
    if (activator != null) {
      return activator(
        serverUrl: serverUrl,
        credential: credential,
        oidcConfig: oidcConfig,
      );
    }
    return _authService.activateResolvedCredential(
      serverUrl: serverUrl,
      credential: credential,
      oidcConfig: oidcConfig,
    );
  }

  String _serverUrl() =>
      _urlController.text.trim().replaceAll(RegExp(r'/+$'), '');

  OidcClientConfig _oidcConfig() {
    return OidcClientConfig(
      authorizationEndpoint: Uri.parse(
        _authorizationEndpointController.text.trim(),
      ),
      tokenEndpoint: Uri.parse(_tokenEndpointController.text.trim()),
      clientId: _clientIdController.text.trim(),
      redirectUri: Uri.parse(_redirectUriController.text.trim()),
      scopes: _scopesController.text
          .split(RegExp(r'\s+'))
          .where((scope) => scope.isNotEmpty)
          .toList(),
    );
  }

  void _showError(String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  String? _requiredUri(String? value) {
    if (value == null || value.trim().isEmpty) return 'Required';
    final uri = Uri.tryParse(value.trim());
    if (uri == null || !uri.hasScheme) return 'Invalid URL';
    return null;
  }

  String? _requiredText(String? value) {
    if (value == null || value.trim().isEmpty) return 'Required';
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(24),
          children: [
            Text(
              'Sign in with your organization account',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            const Text(
              'The app opens your provider sign-in in the system browser, then checks your Datarun actor before work is available.',
            ),
            const SizedBox(height: 24),
            Form(
              key: _oidcFormKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  TextFormField(
                    controller: _urlController,
                    decoration: const InputDecoration(
                      labelText: 'Datarun server URL',
                      hintText: 'http://10.0.2.2:8080',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.url,
                    validator: _requiredUri,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _authorizationEndpointController,
                    decoration: const InputDecoration(
                      labelText: 'Authorization endpoint',
                      hintText: 'https://provider.example/auth',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.url,
                    validator: _requiredUri,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _tokenEndpointController,
                    decoration: const InputDecoration(
                      labelText: 'Token endpoint',
                      hintText: 'https://provider.example/token',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.url,
                    validator: _requiredUri,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _clientIdController,
                    decoration: const InputDecoration(
                      labelText: 'Client ID',
                      border: OutlineInputBorder(),
                    ),
                    validator: _requiredText,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _redirectUriController,
                    decoration: const InputDecoration(
                      labelText: 'Redirect URI',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.url,
                    validator: _requiredUri,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _scopesController,
                    decoration: const InputDecoration(
                      labelText: 'Scopes',
                      border: OutlineInputBorder(),
                    ),
                    validator: _requiredText,
                  ),
                  const SizedBox(height: 24),
                  FilledButton.icon(
                    onPressed: _saving ? null : _signIn,
                    icon: _saving
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.login),
                    label: const Text('Sign in'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            ExpansionTile(
              tilePadding: EdgeInsets.zero,
              title: const Text('Development setup'),
              subtitle: const Text(
                'Manual bearer credentials are for tests and synthetic demos.',
              ),
              children: [
                Form(
                  key: _devFormKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _tokenController,
                        decoration: const InputDecoration(
                          labelText: 'Bearer credential (development only)',
                          border: OutlineInputBorder(),
                        ),
                        maxLines: 2,
                        validator: (v) {
                          if (v == null || v.trim().isEmpty) return 'Required';
                          if (v.trim().length < 32) return 'Token too short';
                          return null;
                        },
                      ),
                      const SizedBox(height: 12),
                      OutlinedButton(
                        onPressed: _saving ? null : _saveDevelopmentBearer,
                        child: const Text('Use development credential'),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
