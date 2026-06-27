import 'dart:io';

import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/field_asset_lookup.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/form_screen.dart';
import 'package:datarun_mobile/presentation/screens/work_list_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

void main() {
  sqfliteFfiInit();
  databaseFactory = databaseFactoryFfi;

  testWidgets('field user selects and confirms an assigned asset', (
    tester,
  ) async {
    final harness = _Harness();

    await _pump(tester, harness.state);
    await tester.tap(find.byType(FloatingActionButton));
    await tester.pumpAndSettle();

    expect(find.text(findAssetTitle), findsOneWidget);
    expect(find.text(findAssetHelper), findsOneWidget);
    expect(_dialogText('Pump A'), findsOneWidget);
    expect(_dialogText('Out-of-scope pump'), findsNothing);

    await tester.tap(_dialogText('Pump A'));
    await tester.pumpAndSettle();
    expect(find.textContaining(confirmAssetCopy), findsOneWidget);

    await tester.tap(find.text('Confirm'));
    await tester.pumpAndSettle();
    expect(find.byType(FormScreen), findsOneWidget);

    await tester.enterText(find.byType(TextFormField), 'Meter reading');
    await tester.tap(find.text('Save'));
    await tester.pumpAndSettle();

    expect(harness.assembler.lastSubjectId, _assetA);
    expect(harness.assembler.lastPayload?[fieldAssetSubjectBinding], _assetA);
    expect(harness.assembler.lastPayload?[assetCandidateEvidenceKey], isNull);
  });

  testWidgets(
    'field user saves missing asset as candidate evidence without lookup truth',
    (tester) async {
      final harness = (await tester.runAsync(_RealEventHarness.create))!;
      addTearDown(() async {
        await tester.runAsync(harness.dispose);
      });
      harness.state.lastSyncResult = SyncResult(
        pushedCount: 0,
        pulledCount: 0,
        error: 'No connection',
      );

      await _pump(tester, harness.state);
      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      expect(find.text(offlineAssetListCaveat), findsOneWidget);
      await tester.tap(find.text(missingAssetAction));
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextFormField).last, 'Unknown pump');
      await tester.tap(find.text(saveCandidateAction));
      await _pumpRealAsyncWork(tester);

      expect(find.byType(FormScreen), findsOneWidget);
      final recordField = find.descendant(
        of: find.byType(FormScreen),
        matching: find.byType(TextFormField),
      );
      await tester.enterText(recordField, 'Record about missing asset');
      expect(find.text('Record about missing asset'), findsOneWidget);
      final saveButton = tester.widget<TextButton>(
        find.widgetWithText(TextButton, 'Save'),
      );
      expect(saveButton.onPressed, isNotNull);
      await tester.runAsync(() async {
        saveButton.onPressed!();
        final deadline = DateTime.now().add(const Duration(seconds: 5));
        while (DateTime.now().isBefore(deadline)) {
          if ((await harness.eventStore.getUnpushed()).isNotEmpty) return;
          await Future<void>.delayed(const Duration(milliseconds: 20));
        }
        fail('Timed out waiting for candidate event to be stored.');
      });
      await tester.pump();
      final saveException = tester.takeException();
      expect(saveException, isNull);
      expect(find.text('Record label is required'), findsNothing);
      await _waitForFormClosed(tester);

      final pending = (await tester.runAsync(harness.eventStore.getUnpushed))!;
      expect(pending, hasLength(1));
      final event = pending.single;
      expect(event.subjectRef['type'], 'subject');
      expect(event.subjectRef['id'], isNot(_assetA));
      expect(event.subjectRef['id'], isNot(_assetB));
      expect(event.payload.containsKey(fieldAssetSubjectBinding), isFalse);
      final evidence =
          event.payload[assetCandidateEvidenceKey] as Map<String, dynamic>;
      expect(evidence['standing'], 'candidate');
      expect(evidence['review_label'], candidateAssetLabel);
      expect(evidence['display_label'], 'Unknown pump');
      expect(evidence['candidate_standing'], candidateNeedsReviewCopy);
      expect(evidence['activity_context'], {
        'activity_ref': _activityRef,
        'shape_ref': _shapeRef,
      });
      expect(evidence['lookup_standing'], containsPair('offline', true));
      expect(
        evidence['lookup_standing'],
        containsPair('message', offlineAssetListCaveat),
      );
      expect(evidence['capture_timestamp'], event.timestamp);
      expect(evidence['original_submitted_record_ref'], {
        'type': 'event',
        'id': event.id,
      });

      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();
      expect(find.text(findAssetTitle), findsOneWidget);
      expect(_dialogText('Pump A'), findsOneWidget);
      expect(find.text('Unknown pump'), findsNothing);
    },
  );

  test(
    'candidate evidence is stamped with original submitted record reference',
    () async {
      SharedPreferences.setMockInitialValues({});
      final identity = await DeviceIdentity.init();
      await identity.activateActorSession(
        actorId: _actorId,
        token: 'field-token',
      );
      final dbPath =
          '${Directory.systemTemp.path}/datarun_asset_candidate_${DateTime.now().microsecondsSinceEpoch}.db';
      final store = EventStore(dbPath: dbPath, actorId: identity.actorId);
      addTearDown(() async {
        await store.close();
        try {
          File(dbPath).deleteSync();
        } catch (_) {}
      });

      final event = await EventAssembler(identity, store).assemble(
        subjectId: null,
        shapeRef: _shapeRef,
        activityRef: _activityRef,
        payload: {
          'name': 'Candidate record',
          assetCandidateEvidenceKey: {
            'standing': 'candidate',
            'display_label': 'Unknown pump',
          },
        },
      );

      final evidence =
          event.payload[assetCandidateEvidenceKey] as Map<String, dynamic>;
      expect(evidence['capture_timestamp'], event.timestamp);
      expect(evidence['original_submitted_record_ref'], {
        'type': 'event',
        'id': event.id,
      });
      expect(event.subjectRef['type'], 'subject');
    },
  );
}

const _actorId = '11111111-1111-1111-1111-111111111111';
const _assetA = 'aaaaaaaa-0000-4000-8000-000000000001';
const _assetB = 'bbbbbbbb-0000-4000-8000-000000000002';
const _activityRef = 'field_asset_inspection';
const _shapeRef = 'asset_check/v1';
const _assignmentId = 'cccccccc-0000-4000-8000-000000000003';
const _geoScope = 'dddddddd-0000-4000-8000-000000000004';

Future<void> _pump(WidgetTester tester, AppState state) {
  return tester.pumpWidget(
    ChangeNotifierProvider<AppState>.value(
      value: state,
      child: const MaterialApp(home: WorkListScreen()),
    ),
  );
}

Future<void> _pumpRealAsyncWork(WidgetTester tester) async {
  await tester.pump();
  await tester.runAsync(
    () => Future<void>.delayed(const Duration(milliseconds: 50)),
  );
  await tester.pump(const Duration(milliseconds: 300));
}

Future<void> _waitForFormClosed(WidgetTester tester) async {
  final deadline = DateTime.now().add(const Duration(seconds: 5));
  while (DateTime.now().isBefore(deadline)) {
    await _pumpRealAsyncWork(tester);
    if (find.byType(FormScreen).evaluate().isEmpty) return;
  }
  fail('Timed out waiting for form route to close.');
}

Finder _dialogText(String text) => find.descendant(
  of: find.byType(AlertDialog).last,
  matching: find.text(text),
);

class _Harness {
  _Harness()
    : eventStore = _FakeEventStore(),
      projection = _FakeProjectionEngine() {
    assembler = _FakeEventAssembler(projection);
    state =
        AppState(
            eventStore: eventStore,
            projectionEngine: projection,
            eventAssembler: assembler,
            configStore: _FakeConfigStore(),
            contextResolver: _FakeContextResolver(),
            syncService: _FakeSyncService(),
            identity: _FakeDeviceIdentity(),
          )
          ..subjects = projection.subjects
          ..activeAssignments = [
            {
              'assignment_id': _assignmentId,
              'role': 'field_worker',
              'geo_scope': _geoScope,
              'subject_list': _assetA,
              'activity_list': _activityRef,
            },
          ];
  }

  final _FakeEventStore eventStore;
  final _FakeProjectionEngine projection;
  late final _FakeEventAssembler assembler;
  late final AppState state;
}

class _FakeConfigStore implements ConfigStore {
  static final shape = ShapeDefinition(
    shapeRef: _shapeRef,
    name: 'Asset check',
    version: 1,
    status: 'active',
    sensitivity: 'standard',
    subjectBinding: fieldAssetSubjectBinding,
    fields: [
      ShapeField(
        name: fieldAssetSubjectBinding,
        type: 'subject_ref',
        required: true,
        description: 'Asset',
      ),
      ShapeField(
        name: 'name',
        type: 'text',
        required: true,
        description: 'Record label',
      ),
    ],
  );

  @override
  int get configVersion => 1;

  @override
  Future<void> promotePending() async {}

  @override
  List<String> getActiveActivities() => [_activityRef];

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

class _FakeEventStore implements EventStore {
  @override
  String? get actorId => _actorId;

  @override
  Future<List<Map<String, dynamic>>> getActiveAssignments() async => [
    {
      'assignment_id': _assignmentId,
      'role': 'field_worker',
      'geo_scope': _geoScope,
      'subject_list': _assetA,
      'activity_list': _activityRef,
    },
  ];

  @override
  Future<int> unpushedCount() async => 0;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeProjectionEngine implements ProjectionEngine {
  final subjects = [
    SubjectSummary(
      subjectId: _assetA,
      subjectType: 'subject',
      name: 'Pump A',
      latestTimestamp: '2026-06-27T09:00:00Z',
      captureCount: 1,
    ),
    SubjectSummary(
      subjectId: _assetB,
      subjectType: 'subject',
      name: 'Out-of-scope pump',
      latestTimestamp: '2026-06-27T09:00:00Z',
      captureCount: 1,
    ),
  ];

  void add(Event event) {
    subjects.add(
      SubjectSummary(
        subjectId: event.subjectRef['id']!,
        subjectType: 'subject',
        name: event.payload['name'] as String?,
        latestTimestamp: event.timestamp,
        captureCount: 1,
      ),
    );
  }

  @override
  Future<List<SubjectSummary>> getSubjectList() async =>
      List<SubjectSummary>.from(subjects);

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventAssembler implements EventAssembler {
  _FakeEventAssembler(this.projection);

  final _FakeProjectionEngine projection;
  String? lastSubjectId;
  Map<String, dynamic>? lastPayload;
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
    lastPayload = Map<String, dynamic>.from(payload);
    final event = Event(
      id: 'event-$_sequence',
      type: 'capture',
      shapeRef: shapeRef,
      activityRef: activityRef,
      subjectRef: {
        'type': 'subject',
        'id': subjectId ?? 'generated-candidate-$_sequence',
      },
      actorRef: {'type': 'actor', 'id': _actorId},
      deviceId: 'device-1',
      deviceSeq: _sequence,
      syncWatermark: null,
      timestamp: '2026-06-27T10:00:0${_sequence}Z',
      payload: payload,
    );
    projection.add(event);
    return event;
  }

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

class _RealEventHarness {
  _RealEventHarness({
    required this.eventStore,
    required this.state,
    required this.dbPath,
  });

  final EventStore eventStore;
  final AppState state;
  final String dbPath;

  static Future<_RealEventHarness> create() async {
    SharedPreferences.setMockInitialValues({});
    final identity = await DeviceIdentity.init();
    await identity.activateActorSession(
      actorId: _actorId,
      token: 'field-token',
    );
    final dbPath =
        '${Directory.systemTemp.path}/datarun_asset_lookup_${DateTime.now().microsecondsSinceEpoch}.db';
    final eventStore = EventStore(dbPath: dbPath, actorId: identity.actorId);
    await eventStore.processAssignmentEvent(_assignmentCreatedEvent(identity));
    final projection = _FakeProjectionEngine();
    final state = AppState(
      eventStore: eventStore,
      projectionEngine: projection,
      eventAssembler: EventAssembler(identity, eventStore),
      configStore: _FakeConfigStore(),
      contextResolver: _FakeContextResolver(),
      syncService: _FakeSyncService(),
      identity: identity,
    );
    await state.refresh();
    return _RealEventHarness(
      eventStore: eventStore,
      state: state,
      dbPath: dbPath,
    );
  }

  Future<void> dispose() async {
    await eventStore.close();
    try {
      File(dbPath).deleteSync();
    } catch (_) {}
  }
}

Event _assignmentCreatedEvent(DeviceIdentity identity) {
  return Event(
    id: 'assignment-event-1',
    type: 'assignment_changed',
    shapeRef: 'assignment_created/v1',
    subjectRef: {'type': 'subject', 'id': _assignmentId},
    actorRef: {'type': 'actor', 'id': _actorId},
    deviceId: identity.deviceId,
    deviceSeq: 0,
    timestamp: '2026-06-27T09:00:00Z',
    payload: {
      'target_actor': {'type': 'actor', 'id': _actorId},
      'role': 'field_worker',
      'scope': {
        'geographic': _geoScope,
        'subject_list': [_assetA],
        'activity': [_activityRef],
      },
      'valid_from': '2026-06-27T09:00:00Z',
      'valid_to': null,
    },
  );
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
  String? get activeActorId => _actorId;

  @override
  ActorSession? get activeSession =>
      const ActorSession(actorId: _actorId, token: 'test-token');

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
