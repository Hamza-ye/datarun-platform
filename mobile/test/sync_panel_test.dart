import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/widgets/sync_panel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

void main() {
  testWidgets('shows never-synced state with no pending work', (tester) async {
    final state = _buildState();

    await _pumpPanel(tester, state);

    expect(find.text('Not synced yet'), findsOneWidget);
    expect(find.text('No records waiting to sync.'), findsOneWidget);
    expect(find.textContaining('Last successful sync:'), findsNothing);
  });

  testWidgets('shows pending work as saved locally and waiting', (
    tester,
  ) async {
    final state = _buildState(pendingCount: 2);

    await _pumpPanel(tester, state);

    expect(find.text('2 records saved on this device'), findsOneWidget);
    expect(find.text('Waiting to sync.'), findsOneWidget);
  });

  testWidgets('shows syncing state and disables duplicate sync', (
    tester,
  ) async {
    final state = _buildState()..isSyncing = true;

    await _pumpPanel(tester, state);

    expect(find.text('Syncing now'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    expect(
      tester.widget<FilledButton>(find.byType(FilledButton)).onPressed,
      isNull,
    );
  });

  testWidgets('shows successful counts and timestamp', (tester) async {
    final state = _buildState()
      ..lastSyncResult = SyncResult(pushedCount: 2, pulledCount: 3)
      ..lastSync = DateTime(2026, 6, 13, 9, 7);

    await _pumpPanel(tester, state);

    expect(find.text('Sync complete'), findsOneWidget);
    expect(find.text('2 records sent.'), findsOneWidget);
    expect(find.text('3 updates received.'), findsOneWidget);
    expect(find.text('Last successful sync: 09:07'), findsOneWidget);
  });

  testWidgets('shows failed sync while pending work stays saved', (
    tester,
  ) async {
    final state = _buildState(pendingCount: 2)
      ..lastSyncResult = SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: 'No connection',
      );

    await _pumpPanel(tester, state);

    expect(find.text('Sync failed'), findsOneWidget);
    expect(
      find.text('2 records still saved on this device and waiting to sync.'),
      findsOneWidget,
    );
    expect(find.text('No connection'), findsOneWidget);
    expect(find.text('Try Again'), findsOneWidget);
  });

  test('failed sync keeps the last successful timestamp', () async {
    final successfulAt = DateTime(2026, 6, 13, 8, 30);
    final state = _buildState(
      pendingCount: 1,
      result: SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: 'No connection',
      ),
    )..lastSync = successfulAt;

    await state.sync();

    expect(state.lastSync, successfulAt);
    expect(state.lastSyncResult?.error, 'No connection');
    expect(state.pendingCount, 1);
  });

  test('successful sync updates the successful timestamp', () async {
    final successfulAt = DateTime(2026, 6, 13, 10, 15);
    final state = _buildState(
      pendingCount: 1,
      result: SyncResult(pushedCount: 1, pulledCount: 0),
      now: () => successfulAt,
    );

    await state.sync();

    expect(state.lastSync, successfulAt);
    expect(state.lastSyncResult?.error, isNull);
  });
}

Future<void> _pumpPanel(WidgetTester tester, AppState state) {
  return tester.pumpWidget(
    ChangeNotifierProvider<AppState>.value(
      value: state,
      child: const MaterialApp(home: Scaffold(body: SyncPanel())),
    ),
  );
}

AppState _buildState({
  int pendingCount = 0,
  SyncResult? result,
  DateTime Function()? now,
}) {
  final eventStore = _FakeEventStore(pendingCount);
  final projectionEngine = _FakeProjectionEngine();
  final state = AppState(
    eventStore: eventStore,
    projectionEngine: projectionEngine,
    eventAssembler: _FakeEventAssembler(),
    configStore: _FakeConfigStore(),
    contextResolver: _FakeContextResolver(),
    syncService: _FakeSyncService(
      result ?? SyncResult(pushedCount: 0, pulledCount: 0),
    ),
    identity: _FakeDeviceIdentity(),
    now: now,
  );
  state.pendingCount = pendingCount;
  return state;
}

class _FakeEventStore implements EventStore {
  _FakeEventStore(this.pendingCount);

  int pendingCount;

  @override
  String? get actorId => '11111111-1111-1111-1111-111111111111';

  @override
  Future<List<Map<String, dynamic>>> getActiveAssignments() async => [];

  @override
  Future<int> unpushedCount() async => pendingCount;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeProjectionEngine implements ProjectionEngine {
  @override
  Future<List<SubjectSummary>> getSubjectList() async => [];

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeConfigStore implements ConfigStore {
  @override
  Future<void> promotePending() async {}

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeSyncService implements SyncService {
  _FakeSyncService(this.result);

  final SyncResult result;

  @override
  Future<SyncResult> sync() async => result;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeDeviceIdentity implements DeviceIdentity {
  static const _actorId = '11111111-1111-1111-1111-111111111111';

  @override
  String get deviceId => 'device-12345678';

  @override
  String? get activeActorId => _actorId;

  @override
  ActorSession? get activeSession =>
      const ActorSession(actorId: _actorId, token: 'test-token');

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventAssembler implements EventAssembler {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeContextResolver implements ContextResolver {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
