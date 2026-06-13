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
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

const _correctionMessage =
    'Correction saved on this device. Original record remains in history. Waiting to sync.';

void main() {
  testWidgets('eligible capture opens prefilled append-only correction form', (
    tester,
  ) async {
    final harness = _Harness();

    await _pump(tester, harness.state);
    await _openCorrection(tester);

    expect(find.byType(FormScreen), findsOneWidget);
    expect(find.text('Add correction'), findsOneWidget);
    expect(
      find.text('Saving creates a new record. The original stays in history.'),
      findsOneWidget,
    );
    expect(_field(tester, 'Name').initialValue, 'Site Alpha');
    expect(_field(tester, 'Notes').initialValue, 'Original reading');

    await tester.tap(find.text('Save correction'));
    await tester.pumpAndSettle();

    expect(find.byType(FormScreen), findsOneWidget);
    expect(
      find.text('Change at least one field before saving a correction.'),
      findsOneWidget,
    );
    expect(harness.assembler.calls, 0);
  });

  testWidgets('reverting an edit does not append a duplicate correction', (
    tester,
  ) async {
    final harness = _Harness();

    await _pump(tester, harness.state);
    await _openCorrection(tester);

    await tester.enterText(_fieldFinder('Notes'), 'Corrected reading');
    await tester.enterText(_fieldFinder('Notes'), 'Original reading');
    await tester.tap(find.text('Save correction'));
    await tester.pumpAndSettle();

    expect(find.byType(FormScreen), findsOneWidget);
    expect(
      find.text('Change at least one field before saving a correction.'),
      findsOneWidget,
    );
    expect(harness.assembler.calls, 0);
  });

  testWidgets('changed correction appends and preserves original history', (
    tester,
  ) async {
    final harness = _Harness();
    final original = harness.projection.events.single;
    final originalPayload = Map<String, dynamic>.from(original.payload);

    await _pump(tester, harness.state);
    await _openCorrection(tester);

    await tester.enterText(_fieldFinder('Notes'), 'Corrected reading');
    await tester.tap(find.text('Save correction'));
    await tester.pumpAndSettle();

    expect(find.byType(SubjectDetailScreen), findsOneWidget);
    expect(find.text(_correctionMessage), findsOneWidget);
    expect(harness.assembler.calls, 1);
    expect(harness.assembler.lastSubjectId, 'subject-1');
    expect(harness.assembler.lastShapeRef, 'field_visit/v1');
    expect(harness.assembler.lastActivityRef, 'field_visit');
    expect(harness.projection.events, hasLength(2));
    expect(harness.projection.events.first, same(original));
    expect(original.payload, originalPayload);

    final correction = harness.projection.events.last;
    expect(correction.id, isNot(original.id));
    expect(correction.type, 'capture');
    expect(correction.subjectRef, original.subjectRef);
    expect(correction.shapeRef, original.shapeRef);
    expect(correction.activityRef, original.activityRef);
    expect(correction.payload['notes'], 'Corrected reading');
    expect(find.byType(ExpansionTile), findsNWidgets(2));
  });

  testWidgets('correction keeps the selected event subject ref', (
    tester,
  ) async {
    final harness = _Harness(
      initialEvent: _event(
        id: 'original-1',
        subjectId: 'retired-subject',
        payload: {'name': 'Site Alpha', 'notes': 'Original reading'},
      ),
    );

    await _pump(tester, harness.state, subjectId: 'canonical-subject');
    await _openCorrection(tester);
    await tester.enterText(_fieldFinder('Notes'), 'Corrected reading');
    await tester.tap(find.text('Save correction'));
    await tester.pumpAndSettle();

    expect(harness.assembler.lastSubjectId, 'retired-subject');
    expect(harness.projection.events.last.subjectRef['id'], 'retired-subject');
  });

  testWidgets('unavailable shape and non-capture event cannot be corrected', (
    tester,
  ) async {
    final unavailable = _Harness(shapeAvailable: false);

    await _pump(tester, unavailable.state);
    await tester.pumpAndSettle();
    await tester.tap(find.byType(ExpansionTile));
    await tester.pumpAndSettle();

    expect(find.text('Add correction'), findsNothing);

    final review = _Harness(
      initialEvent: _event(
        id: 'review-1',
        type: 'review',
        payload: {'notes': 'Reviewed'},
      ),
    );

    await _pump(tester, review.state);
    await tester.pumpAndSettle();
    await tester.tap(find.byType(ExpansionTile));
    await tester.pumpAndSettle();

    expect(find.text('Add correction'), findsNothing);
  });
}

Future<void> _pump(
  WidgetTester tester,
  AppState state, {
  String subjectId = 'subject-1',
}) {
  return tester.pumpWidget(
    ChangeNotifierProvider<AppState>.value(
      value: state,
      child: MaterialApp(
        home: SubjectDetailScreen(key: ValueKey(state), subjectId: subjectId),
      ),
    ),
  );
}

Future<void> _openCorrection(WidgetTester tester) async {
  await tester.pumpAndSettle();
  await tester.tap(find.byType(ExpansionTile));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Add correction'));
  await tester.pumpAndSettle();
}

TextFormField _field(WidgetTester tester, String label) =>
    tester.widget<TextFormField>(_fieldFinder(label));

Finder _fieldFinder(String label) => find.widgetWithText(TextFormField, label);

class _Harness {
  _Harness({bool shapeAvailable = true, Event? initialEvent})
    : eventStore = _FakeEventStore(),
      projection = _FakeProjectionEngine([
        initialEvent ??
            _event(
              id: 'original-1',
              payload: {'name': 'Site Alpha', 'notes': 'Original reading'},
            ),
      ]),
      configStore = _FakeConfigStore(shapeAvailable) {
    assembler = _FakeEventAssembler(eventStore, projection);
    state = AppState(
      eventStore: eventStore,
      projectionEngine: projection,
      eventAssembler: assembler,
      configStore: configStore,
      contextResolver: _FakeContextResolver(),
      syncService: _FakeSyncService(),
      identity: _FakeDeviceIdentity(),
    );
  }

  final _FakeEventStore eventStore;
  final _FakeProjectionEngine projection;
  final _FakeConfigStore configStore;
  late final _FakeEventAssembler assembler;
  late final AppState state;
}

class _FakeEventAssembler implements EventAssembler {
  _FakeEventAssembler(this.eventStore, this.projection);

  final _FakeEventStore eventStore;
  final _FakeProjectionEngine projection;
  int calls = 0;
  String? lastSubjectId;
  String? lastShapeRef;
  String? lastActivityRef;

  @override
  Future<Event> assemble({
    required String? subjectId,
    required String shapeRef,
    required Map<String, dynamic> payload,
    String? activityRef,
  }) async {
    calls++;
    lastSubjectId = subjectId;
    lastShapeRef = shapeRef;
    lastActivityRef = activityRef;
    final event = _event(
      id: 'correction-$calls',
      subjectId: subjectId ?? 'new-subject',
      payload: Map<String, dynamic>.from(payload),
      shapeRef: shapeRef,
      activityRef: activityRef,
    );
    eventStore.pendingCount++;
    projection.events.add(event);
    return event;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeProjectionEngine implements ProjectionEngine {
  _FakeProjectionEngine(this.events);

  final List<Event> events;

  @override
  Future<List<Event>> getSubjectDetail(String subjectId) async =>
      List<Event>.from(events);

  @override
  Future<Set<String>> getFlaggedEventIds() async => {};

  @override
  Future<List<SubjectSummary>> getSubjectList() async {
    final latest = events.last;
    return [
      SubjectSummary(
        subjectId: latest.subjectRef['id']!,
        subjectType: 'subject',
        name: latest.payload['name'] as String?,
        latestTimestamp: latest.timestamp,
        captureCount: events.length,
      ),
    ];
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventStore implements EventStore {
  int pendingCount = 0;

  @override
  Future<List<Map<String, dynamic>>> getActiveAssignments() async => [
    {'role': 'field_worker'},
  ];

  @override
  Future<int> unpushedCount() async => pendingCount;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeConfigStore implements ConfigStore {
  _FakeConfigStore(this.shapeAvailable);

  final bool shapeAvailable;

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
      ShapeField(
        name: 'notes',
        type: 'narrative',
        required: false,
        description: 'Notes',
      ),
    ],
  );

  @override
  Future<void> promotePending() async {}

  @override
  ShapeDefinition? getShape(String shapeRef) =>
      shapeAvailable && shapeRef == shape.shapeRef ? shape : null;

  @override
  List<String> getActiveActivities() => shapeAvailable ? ['field_visit'] : [];

  @override
  List<ShapeDefinition> getShapesForActivity(String activityName) =>
      shapeAvailable ? [shape] : [];

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
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeDeviceIdentity implements DeviceIdentity {
  @override
  String get deviceId => 'device-12345678';

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

Event _event({
  required String id,
  String subjectId = 'subject-1',
  String type = 'capture',
  String shapeRef = 'field_visit/v1',
  String? activityRef = 'field_visit',
  required Map<String, dynamic> payload,
}) {
  return Event(
    id: id,
    type: type,
    shapeRef: shapeRef,
    activityRef: activityRef,
    subjectRef: {'type': 'subject', 'id': subjectId},
    actorRef: {'type': 'actor', 'id': 'actor-1'},
    deviceId: 'device-12345678',
    deviceSeq: id.hashCode,
    timestamp: id.startsWith('correction')
        ? '2026-06-13T11:00:00Z'
        : '2026-06-13T10:00:00Z',
    payload: payload,
  );
}
