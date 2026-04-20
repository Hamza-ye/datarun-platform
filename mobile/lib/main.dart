import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/config_store.dart';
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
    final identity = widget.identity;
    final serverUrl = identity.serverUrl!;
    final eventStore = EventStore();
    final projectionEngine = ProjectionEngine(eventStore);
    final eventAssembler = EventAssembler(identity, eventStore);
    final configStore = ConfigStore(eventStore);
    await configStore.init();
    final syncService =
        SyncService(eventStore, identity, serverUrl, configStore);

    final appState = AppState(
      eventStore: eventStore,
      projectionEngine: projectionEngine,
      eventAssembler: eventAssembler,
      configStore: configStore,
      syncService: syncService,
      identity: identity,
    );

    await appState.refresh();
    setState(() => _appState = appState);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Datarun',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: _appState != null
          ? ChangeNotifierProvider.value(
              value: _appState!,
              child: const WorkListScreen(),
            )
          : SetupScreen(
              identity: widget.identity,
              onSetupComplete: () => _bootstrap(),
            ),
    );
  }
}
