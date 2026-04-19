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

// Phase 0: server URL. TODO: make configurable.
const serverUrl = 'http://192.168.8.8:8080'; // LAN IP for real device

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final identity = await DeviceIdentity.init();
  final eventStore = EventStore();
  final projectionEngine = ProjectionEngine(eventStore);
  final eventAssembler = EventAssembler(identity, eventStore);
  final configStore = ConfigStore(eventStore);
  await configStore.init();
  final syncService = SyncService(eventStore, identity, serverUrl, configStore);

  final appState = AppState(
    eventStore: eventStore,
    projectionEngine: projectionEngine,
    eventAssembler: eventAssembler,
    configStore: configStore,
    syncService: syncService,
    identity: identity,
  );

  await appState.refresh();

  runApp(
    ChangeNotifierProvider.value(
      value: appState,
      child: const DatarunApp(),
    ),
  );
}

class DatarunApp extends StatelessWidget {
  const DatarunApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Datarun',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: const WorkListScreen(),
    );
  }
}
