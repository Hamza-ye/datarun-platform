import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/form_screen.dart';
import 'package:datarun_mobile/presentation/screens/subject_detail_screen.dart';
import 'package:datarun_mobile/presentation/screens/work_list_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

const _savedMessage = 'Saved on this device. Waiting to sync.';

void main() {
  testWidgets('work list keeps pending local work visible and opens sync', (
    tester,
  ) async {
    final harness = _Harness(pendingCount: 2);

    await _pump(tester, harness.state, const WorkListScreen());

    expect(find.text('2 records saved on this device'), findsOneWidget);
    expect(find.text('Waiting to sync.'), findsOneWidget);

    await tester.tap(find.text('2 records saved on this device'));
    await tester.pumpAndSettle();

    expect(find.text('Sync'), findsOneWidget);
    expect(find.text('2 records saved on this device'), findsNWidgets(2));
  });

  testWidgets('successful new capture refreshes and confirms on work list', (
    tester,
  ) async {
    final harness = _Harness();

    await _pump(tester, harness.state, const WorkListScreen());
    await _openForm(tester);
    await tester.enterText(find.byType(TextFormField), 'New record');
    await tester.tap(find.text('Save'));
    await tester.pumpAndSettle();

    expect(find.byType(WorkListScreen), findsOneWidget);
    expect(find.text(_savedMessage), findsOneWidget);
    expect(find.text('1 record saved on this device'), findsOneWidget);
    expect(find.text('New record'), findsOneWidget);
    expect(harness.projection.subjectListReads, 1);
  });

  testWidgets('dismissing a form does not confirm or refresh', (tester) async {
    final harness = _Harness();

    await _pump(tester, harness.state, const WorkListScreen());
    await _openForm(tester);
    await tester.pageBack();
    await tester.pumpAndSettle();

    expect(find.byType(WorkListScreen), findsOneWidget);
    expect(find.text(_savedMessage), findsNothing);
    expect(harness.projection.subjectListReads, 0);
  });

  testWidgets('subject-linked capture confirms on subject detail', (
    tester,
  ) async {
    final harness = _Harness(
      initialEvents: [_event(subjectId: 'subject-1', name: 'Existing record')],
    );

    await _pump(
      tester,
      harness.state,
      const SubjectDetailScreen(subjectId: 'subject-1'),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.widgetWithText(FilledButton, 'Capture'));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextFormField), 'Follow-up');
    await tester.tap(find.text('Save'));
    await tester.pumpAndSettle();

    expect(find.byType(SubjectDetailScreen), findsOneWidget);
    expect(find.text(_savedMessage), findsOneWidget);
    expect(harness.assembler.lastSubjectId, 'subject-1');
    expect(harness.projection.subjectDetailReads, 2);
    expect(harness.projection.subjectListReads, 1);
  });

  testWidgets('empty work list uses neutral captured-work copy', (
    tester,
  ) async {
    final harness = _Harness();

    await _pump(tester, harness.state, const WorkListScreen());

    expect(
      find.text('No captured work yet.\nTap + to add a record.'),
      findsOneWidget,
    );
    expect(find.textContaining('No subjects'), findsNothing);
  });
}

Future<void> _pump(WidgetTester tester, AppState state, Widget home) {
  return tester.pumpWidget(
    ChangeNotifierProvider<AppState>.value(
      value: state,
      child: MaterialApp(home: home),
    ),
  );
}

Future<void> _openForm(WidgetTester tester) async {
  await tester.tap(find.byType(FloatingActionButton));
  await tester.pumpAndSettle();
  expect(find.byType(FormScreen), findsOneWidget);
}

class _Harness {
  _Harness({int pendingCount = 0, List<Event> initialEvents = const []})
    : eventStore = _FakeEventStore(pendingCount),
      projection = _FakeProjectionEngine(initialEvents) {
    assembler = _FakeEventAssembler(eventStore, projection);
    state = AppState(
      eventStore: eventStore,
      projectionEngine: projection,
      eventAssembler: assembler,
      configStore: _FakeConfigStore(),
      contextResolver: _FakeContextResolver(),
      syncService: _FakeSyncService(),
      identity: _FakeDeviceIdentity(),
    )..pendingCount = pendingCount;
  }

  final _FakeEventStore eventStore;
  final _FakeProjectionEngine projection;
  late final _FakeEventAssembler assembler;
  late final AppState state;
}

class _FakeEventStore implements EventStore {
  _FakeEventStore(this.pendingCount);

  int pendingCount;

  @override
  Future<List<Map<String, dynamic>>> getActiveAssignments() async => [];

  @override
  Future<int> unpushedCount() async => pendingCount;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeProjectionEngine implements ProjectionEngine {
  _FakeProjectionEngine(List<Event> initialEvents) {
    for (final event in initialEvents) {
      add(event);
    }
  }

  final Map<String, List<Event>> _eventsBySubject = {};
  int subjectListReads = 0;
  int subjectDetailReads = 0;

  void add(Event event) {
    final subjectId = event.subjectRef['id']!;
    _eventsBySubject.putIfAbsent(subjectId, () => []).add(event);
  }

  @override
  Future<List<SubjectSummary>> getSubjectList() async {
    subjectListReads++;
    return _eventsBySubject.entries.map((entry) {
      final latest = entry.value.last;
      return SubjectSummary(
        subjectId: entry.key,
        subjectType: 'subject',
        name: latest.payload['name'] as String?,
        latestTimestamp: latest.timestamp,
        captureCount: entry.value.length,
      );
    }).toList();
  }

  @override
  Future<List<Event>> getSubjectDetail(String subjectId) async {
    subjectDetailReads++;
    return List<Event>.from(_eventsBySubject[subjectId] ?? const []);
  }

  @override
  Future<Set<String>> getFlaggedEventIds() async => {};

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventAssembler implements EventAssembler {
  _FakeEventAssembler(this.eventStore, this.projection);

  final _FakeEventStore eventStore;
  final _FakeProjectionEngine projection;
  String? lastSubjectId;
  int _sequence = 0;

  @override
  Future<Event> assemble({
    required String? subjectId,
    required String shapeRef,
    required Map<String, dynamic> payload,
    String? activityRef,
  }) async {
    _sequence++;
    lastSubjectId = subjectId;
    final event = _event(
      id: 'event-$_sequence',
      subjectId: subjectId ?? 'subject-$_sequence',
      name: payload['name'] as String?,
      shapeRef: shapeRef,
      activityRef: activityRef,
    );
    eventStore.pendingCount++;
    projection.add(event);
    return event;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeConfigStore implements ConfigStore {
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
  Future<void> promotePending() async {}

  @override
  List<String> getActiveActivities() => ['field_visit'];

  @override
  List<ShapeDefinition> getShapesForActivity(String activityName) => [shape];

  @override
  ShapeDefinition? getShape(String shapeRef) => shape;

  @override
  ActivityActionDecision evaluateActivityAction({
    required String activityRef,
    required ActivityAction action,
    required Iterable<Map<String, dynamic>> activeAssignments,
  }) => const ActivityActionDecision.permitted();

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

class _FakeSyncService implements SyncService {
  @override
  Future<SyncResult> sync() async => SyncResult(pushedCount: 0, pulledCount: 0);

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeDeviceIdentity implements DeviceIdentity {
  @override
  String get deviceId => 'device-12345678';

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

Event _event({
  String id = 'event-0',
  required String subjectId,
  String? name,
  String shapeRef = 'field_visit/v1',
  String? activityRef = 'field_visit',
}) {
  return Event(
    id: id,
    type: 'capture',
    shapeRef: shapeRef,
    activityRef: activityRef,
    subjectRef: {'type': 'subject', 'id': subjectId},
    actorRef: {'type': 'actor', 'id': 'actor-1'},
    deviceId: 'device-12345678',
    deviceSeq: 1,
    timestamp: '2026-06-13T10:00:00Z',
    payload: {'name': ?name},
  );
}
