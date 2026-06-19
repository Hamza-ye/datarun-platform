import 'package:flutter/material.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';
import 'package:datarun_mobile/data/event_assembler.dart';
import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/context_resolver.dart';
import 'package:datarun_mobile/data/sync_service.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';

/// Central app state. Notifies listeners when projections change.
class AppState extends ChangeNotifier {
  final EventStore eventStore;
  final ProjectionEngine projectionEngine;
  final EventAssembler eventAssembler;
  final ConfigStore configStore;
  final ContextResolver contextResolver;
  final SyncService syncService;
  final DeviceIdentity identity;
  final DateTime Function() _now;

  List<SubjectSummary> subjects = [];
  List<Map<String, dynamic>> activeAssignments = [];
  int pendingCount = 0;
  DateTime? lastSync;
  SyncResult? lastSyncResult;
  bool isSyncing = false;

  AppState({
    required this.eventStore,
    required this.projectionEngine,
    required this.eventAssembler,
    required this.configStore,
    required this.contextResolver,
    required this.syncService,
    required this.identity,
    DateTime Function()? now,
  }) : _now = now ?? DateTime.now;

  Future<void> refresh() async {
    if (identity.activeSession == null ||
        (eventStore.actorId != null &&
            eventStore.actorId != identity.activeActorId)) {
      subjects = [];
      activeAssignments = [];
      pendingCount = 0;
      notifyListeners();
      return;
    }

    // Promote pending config at a safe transition point under the accepted two-slot model.
    await configStore.promotePending();
    subjects = await projectionEngine.getSubjectList();
    activeAssignments = await eventStore.getActiveAssignments();
    pendingCount = await eventStore.unpushedCount();
    notifyListeners();
  }

  Future<SyncResult> sync() async {
    isSyncing = true;
    notifyListeners();
    try {
      final result = await syncService.sync();
      lastSyncResult = result;
      if (result.error == null) {
        lastSync = _now();
      }
      await refresh();
      return result;
    } finally {
      isSyncing = false;
      notifyListeners();
    }
  }
}
