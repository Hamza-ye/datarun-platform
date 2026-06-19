import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/form_screen.dart';
import 'package:datarun_mobile/presentation/screens/work_list_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

void main() {
  testWidgets('get work syncs existing state into ready-to-capture', (
    tester,
  ) async {
    final harness = _Harness();
    harness.syncService.onSync = () {
      harness.config
        ..version = 1
        ..hasForms = true;
      harness.eventStore.assignments = [_assignment()];
    };

    await _pump(tester, harness.state);

    expect(find.text('Get your work'), findsOneWidget);
    expect(find.text('Get Work'), findsOneWidget);

    await tester.tap(find.text('Get Work'));
    await tester.pumpAndSettle();

    expect(harness.syncService.calls, 1);
    expect(find.text('Ready to capture'), findsOneWidget);
    expect(find.byType(FloatingActionButton), findsOneWidget);
  });

  testWidgets('getting work shows progress without a duplicate action', (
    tester,
  ) async {
    final harness = _Harness()..state.isSyncing = true;

    await _pump(tester, harness.state);

    expect(find.text('Getting your work'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNWidgets(2));
    expect(find.text('Get Work'), findsNothing);
    expect(find.text('Try Again'), findsNothing);
  });

  testWidgets('failed first sync explains error and retries', (tester) async {
    final harness = _Harness()
      ..state.lastSyncResult = SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: 'No connection',
      );

    await _pump(tester, harness.state);

    expect(find.text("Couldn't get work"), findsOneWidget);
    expect(find.text('No connection'), findsOneWidget);

    await tester.tap(find.text('Try Again'));
    await tester.pumpAndSettle();

    expect(harness.syncService.calls, 1);
    expect(find.text('Work setup unavailable'), findsOneWidget);
  });

  testWidgets('successful sync without forms explains unavailable setup', (
    tester,
  ) async {
    final harness = _Harness()
      ..state.lastSyncResult = SyncResult(pushedCount: 0, pulledCount: 0);

    await _pump(tester, harness.state);

    expect(find.text('Work setup unavailable'), findsOneWidget);
    expect(find.text('Sync Again'), findsOneWidget);
    expect(find.byType(FloatingActionButton), findsNothing);
  });

  testWidgets('configured forms without assignment remain advisory', (
    tester,
  ) async {
    final harness = _Harness(configVersion: 1, hasForms: true);

    await _pump(tester, harness.state);

    expect(find.text('No assigned work available'), findsOneWidget);
    expect(find.text('Check Again'), findsOneWidget);
    expect(find.byType(FloatingActionButton), findsOneWidget);

    await tester.tap(find.byType(FloatingActionButton));
    await tester.pumpAndSettle();

    expect(find.byType(FormScreen), findsOneWidget);
    expect(find.text('No current assignment covers capture.'), findsOneWidget);
  });

  testWidgets('assignment and forms show ready-to-capture state', (
    tester,
  ) async {
    final harness = _Harness(
      configVersion: 1,
      hasForms: true,
      assignments: [_assignment()],
    );

    await _pump(tester, harness.state);

    expect(find.text('Ready to capture'), findsOneWidget);
    expect(find.text('Tap + to add a record.'), findsOneWidget);
    expect(find.byType(FloatingActionButton), findsOneWidget);
  });

  testWidgets(
    'existing records remain visible when readiness needs attention',
    (tester) async {
      final harness = _Harness();
      harness.state.subjects = [
        SubjectSummary(
          subjectId: 'subject-1',
          subjectType: 'subject',
          name: 'Existing record',
          latestTimestamp: '2026-06-13T10:00:00Z',
          captureCount: 1,
        ),
      ];

      await _pump(tester, harness.state);

      expect(find.text('Get your work'), findsOneWidget);
      expect(find.text('Existing record'), findsOneWidget);
    },
  );

  testWidgets('session menu exposes switch user and sign out actions', (
    tester,
  ) async {
    final harness = _Harness(
      configVersion: 1,
      hasForms: true,
      assignments: [_assignment()],
    );

    await tester.pumpWidget(
      ChangeNotifierProvider<AppState>.value(
        value: harness.state,
        child: MaterialApp(
          home: WorkListScreen(
            onSwitchUser: (_) async {},
            onSignOut: (_) async {},
          ),
        ),
      ),
    );

    await tester.tap(find.byTooltip('User session'));
    await tester.pumpAndSettle();

    expect(find.text('Switch user'), findsOneWidget);
    expect(find.text('Sign out'), findsOneWidget);
  });
}

Future<void> _pump(WidgetTester tester, AppState state) {
  return tester.pumpWidget(
    ChangeNotifierProvider<AppState>.value(
      value: state,
      child: const MaterialApp(home: WorkListScreen()),
    ),
  );
}

Map<String, dynamic> _assignment() => {
  'assignment_id': 'assignment-1',
  'role': 'field_worker',
};

class _Harness {
  _Harness({
    int configVersion = 0,
    bool hasForms = false,
    List<Map<String, dynamic>> assignments = const [],
  }) : config = _FakeConfigStore(configVersion, hasForms),
       eventStore = _FakeEventStore(assignments),
       syncService = _FakeSyncService() {
    state = AppState(
      eventStore: eventStore,
      projectionEngine: _FakeProjectionEngine(),
      eventAssembler: _FakeEventAssembler(),
      configStore: config,
      contextResolver: _FakeContextResolver(),
      syncService: syncService,
      identity: _FakeDeviceIdentity(),
    )..activeAssignments = List<Map<String, dynamic>>.from(assignments);
  }

  final _FakeConfigStore config;
  final _FakeEventStore eventStore;
  final _FakeSyncService syncService;
  late final AppState state;
}

class _FakeConfigStore implements ConfigStore {
  _FakeConfigStore(this.version, this.hasForms);

  int version;
  bool hasForms;

  static final shape = ShapeDefinition(
    shapeRef: 'field_visit/v1',
    name: 'Field visit',
    version: 1,
    status: 'active',
    sensitivity: 'standard',
    fields: [
      ShapeField(
        name: 'name',
        type: 'text',
        required: true,
        description: 'Name',
      ),
    ],
  );

  @override
  int get configVersion => version;

  @override
  Future<void> promotePending() async {}

  @override
  List<String> getActiveActivities() => hasForms ? ['field_visit'] : [];

  @override
  List<ShapeDefinition> getShapesForActivity(String activityName) =>
      hasForms ? [shape] : [];

  @override
  ShapeDefinition? getShape(String shapeRef) => hasForms ? shape : null;

  @override
  ActivityActionDecision evaluateActivityAction({
    required String activityRef,
    required ActivityAction action,
    required Iterable<Map<String, dynamic>> activeAssignments,
  }) {
    if (activeAssignments.isEmpty) {
      return const ActivityActionDecision.warn(
        'No current assignment covers capture.',
      );
    }
    return const ActivityActionDecision.permitted();
  }

  @override
  Map<String, dynamic>? getDefaultExpression(
    String activityRef,
    String shapeRef,
    String fieldName,
  ) => null;

  @override
  Map<String, dynamic>? getShowCondition(
    String activityRef,
    String shapeRef,
    String fieldName,
  ) => null;

  @override
  Map<String, dynamic>? getWarningExpression(
    String activityRef,
    String shapeRef,
    String fieldName,
  ) => null;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventStore implements EventStore {
  _FakeEventStore(List<Map<String, dynamic>> assignments)
    : assignments = List<Map<String, dynamic>>.from(assignments);

  List<Map<String, dynamic>> assignments;

  @override
  String? get actorId => '11111111-1111-1111-1111-111111111111';

  @override
  Future<List<Map<String, dynamic>>> getActiveAssignments() async =>
      List<Map<String, dynamic>>.from(assignments);

  @override
  Future<int> unpushedCount() async => 0;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeSyncService implements SyncService {
  int calls = 0;
  VoidCallback? onSync;
  SyncResult result = SyncResult(pushedCount: 0, pulledCount: 0);

  @override
  Future<SyncResult> sync() async {
    calls++;
    onSync?.call();
    return result;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeProjectionEngine implements ProjectionEngine {
  @override
  Future<List<SubjectSummary>> getSubjectList() async => [];

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeContextResolver implements ContextResolver {
  @override
  Future<Map<String, dynamic>> resolve({
    String? subjectId,
    String? activityRef,
    DateTime? now,
  }) async => {};

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventAssembler implements EventAssembler {
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
