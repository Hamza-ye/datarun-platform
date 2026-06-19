import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/data/auth_service.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/oidc_config.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/work_list_screen.dart';
import 'package:datarun_mobile/presentation/screens/setup_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final identity = await DeviceIdentity.init();
  runApp(DatarunApp(identity: identity));
}

class DatarunApp extends StatefulWidget {
  final DeviceIdentity identity;
  const DatarunApp({super.key, required this.identity});

  @override
  State<DatarunApp> createState() => _DatarunAppState();
}

class _DatarunAppState extends State<DatarunApp> {
  AppState? _appState;

  @override
  void initState() {
    super.initState();
    if (widget.identity.isSetupComplete) {
      _bootstrap();
    }
  }

  Future<void> _bootstrap() async {
    final previousAppState = _appState;
    if (previousAppState != null) {
      await previousAppState.eventStore.close();
    }

    final identity = widget.identity;
    final serverUrl = identity.serverUrl!;
    final eventStore = EventStore(actorId: identity.actorId);
    final projectionEngine = ProjectionEngine(eventStore);
    final eventAssembler = EventAssembler(identity, eventStore);
    final configStore = ConfigStore(eventStore);
    await configStore.init();
    final contextResolver = ContextResolver(eventStore, projectionEngine);
    final authService = MobileAuthService(identity);
    final syncService = SyncService(
      eventStore,
      identity,
      serverUrl,
      configStore,
      authService: authService,
    );

    final appState = AppState(
      eventStore: eventStore,
      projectionEngine: projectionEngine,
      eventAssembler: eventAssembler,
      configStore: configStore,
      contextResolver: contextResolver,
      syncService: syncService,
      identity: identity,
    );

    await appState.refresh();
    setState(() => _appState = appState);
  }

  @override
  Widget build(BuildContext context) {
    if (_appState != null) {
      return ChangeNotifierProvider.value(
        value: _appState!,
        child: MaterialApp(
          title: 'Datarun',
          theme: ThemeData(
            colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
            useMaterial3: true,
          ),
          home: WorkListScreen(onSignOut: _signOut, onSwitchUser: _switchUser),
        ),
      );
    }
    return MaterialApp(
      title: 'Datarun',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: SetupScreen(
        identity: widget.identity,
        authService: MobileAuthService(widget.identity),
        onSetupComplete: () => _bootstrap(),
      ),
    );
  }

  Future<void> _signOut(BuildContext context) async {
    final pendingCount = _appState?.pendingCount ?? 0;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Sign out'),
        content: Text(
          pendingCount > 0
              ? '$pendingCount record${pendingCount == 1 ? '' : 's'} saved locally and waiting to sync. The same user must sign in to sync this work.'
              : 'Sign out of this active user session.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Sign out'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;

    final previous = _appState;
    await widget.identity.clearActiveActorSession();
    if (!mounted) return;
    setState(() => _appState = null);
    await previous?.eventStore.close();
  }

  Future<void> _switchUser(BuildContext context) async {
    final currentState = _appState;
    if (currentState == null) return;

    await Navigator.of(context).push<void>(
      MaterialPageRoute(
        builder: (routeContext) => SetupScreen(
          title: 'Switch user',
          identity: widget.identity,
          authService: MobileAuthService(widget.identity),
          credentialActivator:
              ({
                required String serverUrl,
                required ProviderCredential credential,
                OidcClientConfig? oidcConfig,
              }) async {
                final result = await currentState.syncService
                    .switchToProviderCredential(
                      credential,
                      oidcConfig: oidcConfig,
                    );
                if (!result.success) {
                  return AuthSessionResult.failed(
                    result.error ?? 'Switch user failed',
                  );
                }
                return AuthSessionResult.signedIn(result.actorId!);
              },
          onSetupComplete: () {
            Navigator.pop(routeContext);
            _bootstrap();
          },
        ),
      ),
    );
  }
}
