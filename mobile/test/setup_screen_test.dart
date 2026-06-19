import 'package:datarun_mobile/data/auth_service.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/oidc_config.dart';
import 'package:datarun_mobile/presentation/screens/setup_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets(
    'primary login uses external sign in copy and hides bearer entry',
    (tester) async {
      _useTallScreen(tester);
      final authService = _FakeAuthService();
      var completed = false;

      await tester.pumpWidget(
        MaterialApp(
          home: SetupScreen(
            identity: _FakeDeviceIdentity(),
            authService: authService,
            onSetupComplete: () => completed = true,
          ),
        ),
      );

      expect(
        find.text('Sign in with your organization account'),
        findsOneWidget,
      );
      expect(find.text('Bearer credential (development only)'), findsNothing);
      expect(find.text('Paste bearer credential'), findsNothing);

      await _fillOidcFields(tester);
      await tester.ensureVisible(find.widgetWithText(FilledButton, 'Sign in'));
      await tester.tap(find.widgetWithText(FilledButton, 'Sign in'));
      await tester.pumpAndSettle();

      expect(authService.handoffCalls, 1);
      expect(authService.activateCalls, 1);
      expect(authService.activatedServerUrl, 'http://server.test');
      expect(authService.capturedConfig?.clientId, 'mobile-client');
      expect(completed, isTrue);
    },
  );

  testWidgets('development bearer path is separated from product sign in', (
    tester,
  ) async {
    _useTallScreen(tester);
    final authService = _FakeAuthService();

    await tester.pumpWidget(
      MaterialApp(
        home: SetupScreen(
          identity: _FakeDeviceIdentity(),
          authService: authService,
          onSetupComplete: () {},
        ),
      ),
    );

    await tester.tap(find.text('Development setup'));
    await tester.pumpAndSettle();

    expect(find.text('Bearer credential (development only)'), findsOneWidget);
    await tester.enterText(
      _field('Bearer credential (development only)'),
      'manual-bearer-token-00000000000000000000',
    );
    await tester.ensureVisible(
      find.widgetWithText(OutlinedButton, 'Use development credential'),
    );
    await tester.tap(
      find.widgetWithText(OutlinedButton, 'Use development credential'),
    );
    await tester.pumpAndSettle();

    expect(authService.handoffCalls, 0);
    expect(authService.activateCalls, 1);
    expect(
      authService.activatedCredential?.accessToken,
      contains('manual-bearer'),
    );
    expect(authService.activatedOidcConfig, isNull);
  });

  testWidgets('switch-user activator receives OIDC credential', (tester) async {
    _useTallScreen(tester);
    final authService = _FakeAuthService();
    ProviderCredential? switchedCredential;
    OidcClientConfig? switchedConfig;

    await tester.pumpWidget(
      MaterialApp(
        home: SetupScreen(
          title: 'Switch user',
          identity: _FakeDeviceIdentity(),
          authService: authService,
          credentialActivator:
              ({
                required String serverUrl,
                required ProviderCredential credential,
                OidcClientConfig? oidcConfig,
              }) async {
                switchedCredential = credential;
                switchedConfig = oidcConfig;
                return const AuthSessionResult.signedIn(
                  '11111111-1111-1111-1111-111111111111',
                );
              },
          onSetupComplete: () {},
        ),
      ),
    );

    await _fillOidcFields(tester);
    await tester.ensureVisible(find.widgetWithText(FilledButton, 'Sign in'));
    await tester.tap(find.widgetWithText(FilledButton, 'Sign in'));
    await tester.pumpAndSettle();

    expect(authService.handoffCalls, 1);
    expect(authService.activateCalls, 0);
    expect(switchedCredential?.accessToken, 'oidc-access-token');
    expect(switchedConfig?.clientId, 'mobile-client');
  });
}

void _useTallScreen(WidgetTester tester) {
  tester.view.physicalSize = const Size(800, 1200);
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);
}

Future<void> _fillOidcFields(WidgetTester tester) async {
  await tester.enterText(_field('Datarun server URL'), 'http://server.test/');
  await tester.enterText(
    _field('Authorization endpoint'),
    'https://provider.test/oauth/authorize',
  );
  await tester.enterText(
    _field('Token endpoint'),
    'https://provider.test/oauth/token',
  );
  await tester.enterText(_field('Client ID'), 'mobile-client');
}

Finder _field(String label) => find.widgetWithText(TextFormField, label);

class _FakeAuthService implements MobileAuthService {
  int handoffCalls = 0;
  int activateCalls = 0;
  OidcClientConfig? capturedConfig;
  OidcClientConfig? activatedOidcConfig;
  ProviderCredential? activatedCredential;
  String? activatedServerUrl;

  @override
  Future<ProviderCredential> obtainOidcCredential(
    OidcClientConfig oidcConfig,
  ) async {
    handoffCalls++;
    capturedConfig = oidcConfig;
    return ProviderCredential(
      accessToken: 'oidc-access-token',
      refreshToken: 'oidc-refresh-token',
      expiresAt: DateTime.utc(2026, 6, 19, 10),
    );
  }

  @override
  Future<AuthSessionResult> activateResolvedCredential({
    required String serverUrl,
    required ProviderCredential credential,
    OidcClientConfig? oidcConfig,
  }) async {
    activateCalls++;
    activatedServerUrl = serverUrl;
    activatedCredential = credential;
    activatedOidcConfig = oidcConfig;
    return const AuthSessionResult.signedIn(
      '11111111-1111-1111-1111-111111111111',
    );
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeDeviceIdentity implements DeviceIdentity {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
