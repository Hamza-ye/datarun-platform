import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('signed-out refresh hides prior actor-local work state', () async {
    final state =
        AppState(
            eventStore: _FakeEventStore(),
            projectionEngine: _FakeProjectionEngine(),
            eventAssembler: _FakeEventAssembler(),
            configStore: _FakeConfigStore(),
            contextResolver: _FakeContextResolver(),
            syncService: _FakeSyncService(),
            identity: _SignedOutIdentity(),
          )
          ..subjects = [
            SubjectSummary(
              subjectId: 'subject-1',
              subjectType: 'subject',
              name: 'Prior actor subject',
              latestTimestamp: '2026-06-19T09:00:00Z',
              captureCount: 1,
            ),
          ]
          ..activeAssignments = [
            {'role': 'field_worker'},
          ]
          ..pendingCount = 1;

    await state.refresh();

    expect(state.subjects, isEmpty);
    expect(state.activeAssignments, isEmpty);
    expect(state.pendingCount, 0);
  });
}

class _FakeEventStore implements EventStore {
  @override
  String? get actorId => '11111111-1111-1111-1111-111111111111';

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeProjectionEngine implements ProjectionEngine {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeEventAssembler implements EventAssembler {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeConfigStore implements ConfigStore {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeContextResolver implements ContextResolver {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeSyncService implements SyncService {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _SignedOutIdentity implements DeviceIdentity {
  @override
  ActorSession? get activeSession => null;

  @override
  String? get activeActorId => null;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
