import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/subject_detail_screen.dart';
import 'package:datarun_mobile/presentation/screens/form_screen.dart';
import 'package:datarun_mobile/presentation/widgets/sync_panel.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/domain/field_asset_lookup.dart';

/// S1: Work List — subject-centric entry point.
class WorkListScreen extends StatelessWidget {
  final Future<void> Function(BuildContext context)? onSignOut;
  final Future<void> Function(BuildContext context)? onSwitchUser;

  const WorkListScreen({super.key, this.onSignOut, this.onSwitchUser});

  static const _savedMessage = 'Saved on this device. Waiting to sync.';

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (context, state, _) {
        final readiness = _readiness(state);
        return Scaffold(
          appBar: AppBar(
            title: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Datarun'),
                if (state.activeAssignments.isNotEmpty)
                  Text(
                    state.activeAssignments
                        .map((a) => a['role'] as String)
                        .toSet()
                        .join(', '),
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.normal,
                    ),
                  ),
              ],
            ),
            actions: [
              // Sync indicator
              InkWell(
                onTap: () => _showSyncPanel(context),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (state.isSyncing)
                        const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      else
                        const Icon(Icons.sync),
                      if (state.pendingCount > 0) ...[
                        const SizedBox(width: 4),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.orange,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            '${state.pendingCount}',
                            style: const TextStyle(
                              fontSize: 12,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
              if (onSignOut != null || onSwitchUser != null)
                PopupMenuButton<_SessionAction>(
                  tooltip: 'User session',
                  onSelected: (action) async {
                    if (action == _SessionAction.switchUser) {
                      await onSwitchUser?.call(context);
                    } else {
                      await onSignOut?.call(context);
                    }
                  },
                  itemBuilder: (context) => [
                    if (onSwitchUser != null)
                      const PopupMenuItem(
                        value: _SessionAction.switchUser,
                        child: ListTile(
                          leading: Icon(Icons.switch_account),
                          title: Text('Switch user'),
                        ),
                      ),
                    if (onSignOut != null)
                      const PopupMenuItem(
                        value: _SessionAction.signOut,
                        child: ListTile(
                          leading: Icon(Icons.logout),
                          title: Text('Sign out'),
                        ),
                      ),
                  ],
                ),
            ],
          ),
          body: Column(
            children: [
              if (state.pendingCount > 0)
                _PendingSyncStatus(
                  count: state.pendingCount,
                  onTap: () => _showSyncPanel(context),
                ),
              if (state.subjects.isNotEmpty && !readiness.isReady)
                _WorkReadinessCard(readiness: readiness, onSync: state.sync),
              Expanded(
                child: state.subjects.isEmpty
                    ? Center(
                        child: _WorkReadinessCard(
                          readiness: readiness,
                          onSync: state.sync,
                          centered: true,
                        ),
                      )
                    : ListView.builder(
                        itemCount: state.subjects.length,
                        itemBuilder: (context, index) {
                          final s = state.subjects[index];
                          return ListTile(
                            title: Row(
                              children: [
                                Expanded(
                                  child: Text(s.name ?? 'Unnamed subject'),
                                ),
                                if (s.flagCount > 0)
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 6,
                                      vertical: 2,
                                    ),
                                    decoration: BoxDecoration(
                                      color: Colors.red,
                                      borderRadius: BorderRadius.circular(10),
                                    ),
                                    child: Text(
                                      '${s.flagCount} flag${s.flagCount == 1 ? '' : 's'}',
                                      style: const TextStyle(
                                        fontSize: 11,
                                        color: Colors.white,
                                      ),
                                    ),
                                  ),
                              ],
                            ),
                            subtitle: Text(
                              '${s.captureCount} capture${s.captureCount == 1 ? '' : 's'} · ${_formatTimestamp(s.latestTimestamp)}',
                            ),
                            trailing: const Icon(Icons.chevron_right),
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (_) => SubjectDetailScreen(
                                    subjectId: s.subjectId,
                                  ),
                                ),
                              );
                            },
                          );
                        },
                      ),
              ),
            ],
          ),
          floatingActionButton: readiness.hasCaptureForms
              ? FloatingActionButton(
                  onPressed: () => _addNew(context),
                  child: const Icon(Icons.add),
                )
              : null,
        );
      },
    );
  }

  Future<void> _addNew(BuildContext context) async {
    final state = context.read<AppState>();
    final configStore = state.configStore;
    final activeActivities = configStore.getActiveActivities();

    if (activeActivities.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('No activities configured')));
      return;
    }

    // Collect capture-available shapes across active activities, tracking the
    // activity for each shape.
    final allShapes = <ShapeDefinition>[];
    final shapeToActivity = <String, String>{};
    for (final actName in activeActivities) {
      for (final shape in configStore.getShapesForActivity(actName)) {
        allShapes.add(shape);
        shapeToActivity[shape.shapeRef] = actName;
      }
    }

    if (allShapes.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('No shapes available')));
      return;
    }

    if (allShapes.length == 1) {
      await _openForm(
        context,
        shapeRef: allShapes.first.shapeRef,
        activityRef: shapeToActivity[allShapes.first.shapeRef],
      );
      return;
    }

    final selected = await showDialog<ShapeDefinition>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Select form'),
        children: allShapes.map((shape) {
          return SimpleDialogOption(
            onPressed: () => Navigator.pop(ctx, shape),
            child: Text(shape.name),
          );
        }).toList(),
      ),
    );
    if (selected == null || !context.mounted) return;
    await _openForm(
      context,
      shapeRef: selected.shapeRef,
      activityRef: shapeToActivity[selected.shapeRef],
    );
  }

  Future<void> _openForm(
    BuildContext context, {
    required String shapeRef,
    required String? activityRef,
  }) async {
    final state = context.read<AppState>();
    final shape = state.configStore.getShape(shapeRef);
    String? subjectId;
    Map<String, dynamic> initialValues = const {};

    if (shape?.subjectBinding == fieldAssetSubjectBinding) {
      final lookup = await _chooseFieldAsset(context, activityRef, shapeRef);
      if (lookup == null || !context.mounted) return;
      subjectId = lookup.subjectId;
      initialValues = lookup.initialValues;
    }

    final result = await Navigator.push<CaptureSaveResult>(
      context,
      MaterialPageRoute(
        builder: (_) => FormScreen(
          subjectId: subjectId,
          shapeRef: shapeRef,
          activityRef: activityRef,
          initialValues: initialValues,
        ),
      ),
    );
    if (result == null || !context.mounted) return;

    await context.read<AppState>().refresh();
    if (!context.mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text(_savedMessage)));
  }

  Future<_FieldAssetLookupResult?> _chooseFieldAsset(
    BuildContext context,
    String? activityRef,
    String shapeRef,
  ) async {
    final state = context.read<AppState>();
    final assets = _fieldAssetOptions(state, activityRef);
    final status = _assetLookupStatus(state, assets.isEmpty);
    final result = await showDialog<_FieldAssetDialogResult>(
      context: context,
      builder: (ctx) => _FieldAssetLookupDialog(assets: assets, status: status),
    );
    if (result == null) return null;
    if (result.subjectId != null) {
      return _FieldAssetLookupResult(
        subjectId: result.subjectId,
        initialValues: {fieldAssetSubjectBinding: result.subjectId},
      );
    }
    final label = result.candidateLabel?.trim();
    if (label == null || label.isEmpty) return null;
    return _FieldAssetLookupResult(
      subjectId: null,
      initialValues: {
        assetCandidateEvidenceKey: _candidateEvidence(
          label: label,
          activityRef: activityRef,
          shapeRef: shapeRef,
          state: state,
          status: status,
        ),
      },
    );
  }

  List<_FieldAssetOption> _fieldAssetOptions(
    AppState state,
    String? activityRef,
  ) {
    final labelsById = {
      for (final subject in state.subjects) subject.subjectId: subject.name,
    };
    final seen = <String>{};
    final options = <_FieldAssetOption>[];
    for (final assignment in state.activeAssignments) {
      if (!_assignmentCoversActivity(assignment, activityRef)) continue;
      for (final subjectId in _csvList(assignment['subject_list'])) {
        if (!seen.add(subjectId)) continue;
        options.add(
          _FieldAssetOption(
            subjectId: subjectId,
            label: labelsById[subjectId] ?? _assetFallbackLabel(subjectId),
          ),
        );
      }
    }
    options.sort((a, b) => a.label.compareTo(b.label));
    return options;
  }

  bool _assignmentCoversActivity(
    Map<String, dynamic> assignment,
    String? activityRef,
  ) {
    if (activityRef == null) return true;
    final activityList = _csvList(assignment['activity_list']);
    return activityList.isEmpty || activityList.contains(activityRef);
  }

  List<String> _csvList(Object? value) {
    if (value is! String || value.trim().isEmpty) return const [];
    return value
        .split(',')
        .map((part) => part.trim())
        .where((part) => part.isNotEmpty)
        .toList(growable: false);
  }

  String _assetFallbackLabel(String subjectId) {
    final short = subjectId.length <= 8 ? subjectId : subjectId.substring(0, 8);
    return 'Asset $short';
  }

  _AssetLookupStatus _assetLookupStatus(AppState state, bool incomplete) {
    final error = state.lastSyncResult?.error;
    if (error == 'No connection') {
      return _AssetLookupStatus(
        state: 'offline_saved_list',
        message: offlineAssetListCaveat,
        offline: true,
        incomplete: incomplete,
      );
    }
    if (error != null) {
      return _AssetLookupStatus(
        state: 'lookup_unavailable',
        message: staleAssetListCaveat,
        unavailable: true,
        incomplete: incomplete,
      );
    }
    if (incomplete) {
      return const _AssetLookupStatus(
        state: 'incomplete_scoped_list',
        message: noMatchingAssets,
        incomplete: true,
      );
    }
    if (state.lastSync == null) {
      return const _AssetLookupStatus(
        state: 'stale_saved_list',
        message: staleAssetListCaveat,
        stale: true,
      );
    }
    return const _AssetLookupStatus(state: 'current_online_list');
  }

  Map<String, dynamic> _candidateEvidence({
    required String label,
    required String? activityRef,
    required String shapeRef,
    required AppState state,
    required _AssetLookupStatus status,
  }) {
    return {
      'standing': 'candidate',
      'review_label': candidateAssetLabel,
      'display_label': label,
      'candidate_standing': candidateNeedsReviewCopy,
      'activity_context': {'activity_ref': activityRef, 'shape_ref': shapeRef},
      'actor_session_provenance': {
        'actor_id': state.identity.activeActorId,
        'session': state.identity.activeSession == null
            ? 'local_actor_session_unavailable'
            : 'local_actor_session',
      },
      'assignment_scope_context': state.activeAssignments
          .map(
            (assignment) => {
              'assignment_id': assignment['assignment_id'],
              'role': assignment['role'],
              'subject_list': _csvList(assignment['subject_list']),
              'activity_list': _csvList(assignment['activity_list']),
              'geographic_scope': assignment['geo_scope'],
            },
          )
          .toList(growable: false),
      'lookup_standing': {
        'state': status.state,
        'message': status.message,
        'offline': status.offline,
        'stale': status.stale,
        'incomplete': status.incomplete,
        'unavailable': status.unavailable,
      },
    };
  }

  void _showSyncPanel(BuildContext context) {
    showModalBottomSheet(context: context, builder: (_) => const SyncPanel());
  }

  _WorkReadiness _readiness(AppState state) {
    final configStore = state.configStore;
    final hasCaptureForms = configStore.getActiveActivities().any(
      (activity) => configStore.getShapesForActivity(activity).isNotEmpty,
    );

    if (!hasCaptureForms) {
      if (state.isSyncing) {
        return const _WorkReadiness(
          kind: _WorkReadinessKind.syncing,
          title: 'Getting your work',
          message: 'Downloading current assignments, forms, and updates.',
          hasCaptureForms: false,
        );
      }

      final error = state.lastSyncResult?.error;
      if (error != null) {
        return _WorkReadiness(
          kind: _WorkReadinessKind.failed,
          title: "Couldn't get work",
          message: 'Check the connection or account, then try again.',
          detail: error,
          actionLabel: 'Try Again',
          hasCaptureForms: false,
        );
      }

      if (configStore.configVersion == 0 && state.lastSyncResult == null) {
        return const _WorkReadiness(
          kind: _WorkReadinessKind.needsSync,
          title: 'Get your work',
          message:
              'Sync this device to download current assignments and forms.',
          actionLabel: 'Get Work',
          hasCaptureForms: false,
        );
      }

      return const _WorkReadiness(
        kind: _WorkReadinessKind.setupUnavailable,
        title: 'Work setup unavailable',
        message:
            'No active capture forms are available on this device. Sync again or contact your administrator.',
        actionLabel: 'Sync Again',
        hasCaptureForms: false,
      );
    }

    if (state.activeAssignments.isEmpty) {
      if (state.isSyncing) {
        return const _WorkReadiness(
          kind: _WorkReadinessKind.syncing,
          title: 'Getting your work',
          message: 'Checking for current assignments and updates.',
          hasCaptureForms: true,
        );
      }

      final error = state.lastSyncResult?.error;
      if (error != null) {
        return _WorkReadiness(
          kind: _WorkReadinessKind.failed,
          title: "Couldn't get work",
          message: 'Check the connection or account, then try again.',
          detail: error,
          actionLabel: 'Try Again',
          hasCaptureForms: true,
        );
      }

      return const _WorkReadiness(
        kind: _WorkReadinessKind.noAssignment,
        title: 'No assigned work available',
        message:
            'Sync to check for current assignments. Configured capture remains available, but records may need review.',
        actionLabel: 'Check Again',
        hasCaptureForms: true,
      );
    }

    return const _WorkReadiness(
      kind: _WorkReadinessKind.ready,
      title: 'Ready to capture',
      message: 'Tap + to add a record.',
      hasCaptureForms: true,
    );
  }

  String _formatTimestamp(String iso) {
    final dt = DateTime.tryParse(iso);
    if (dt == null) return iso;
    final now = DateTime.now();
    final diff = now.difference(dt);
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    return '${diff.inDays}d ago';
  }
}

class _FieldAssetLookupResult {
  final String? subjectId;
  final Map<String, dynamic> initialValues;

  const _FieldAssetLookupResult({
    required this.subjectId,
    this.initialValues = const {},
  });
}

class _FieldAssetOption {
  final String subjectId;
  final String label;

  const _FieldAssetOption({required this.subjectId, required this.label});
}

class _AssetLookupStatus {
  final String state;
  final String? message;
  final bool offline;
  final bool stale;
  final bool incomplete;
  final bool unavailable;

  const _AssetLookupStatus({
    required this.state,
    this.message,
    this.offline = false,
    this.stale = false,
    this.incomplete = false,
    this.unavailable = false,
  });
}

class _FieldAssetDialogResult {
  final String? subjectId;
  final String? candidateLabel;

  const _FieldAssetDialogResult.selected(this.subjectId)
    : candidateLabel = null;

  const _FieldAssetDialogResult.candidate(this.candidateLabel)
    : subjectId = null;
}

class _FieldAssetLookupDialog extends StatefulWidget {
  final List<_FieldAssetOption> assets;
  final _AssetLookupStatus status;

  const _FieldAssetLookupDialog({required this.assets, required this.status});

  @override
  State<_FieldAssetLookupDialog> createState() =>
      _FieldAssetLookupDialogState();
}

class _FieldAssetLookupDialogState extends State<_FieldAssetLookupDialog> {
  final _searchController = TextEditingController();
  String _query = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final normalized = _query.trim().toLowerCase();
    final filtered = normalized.isEmpty
        ? widget.assets
        : widget.assets
              .where(
                (asset) =>
                    asset.label.toLowerCase().contains(normalized) ||
                    asset.subjectId.toLowerCase().contains(normalized),
              )
              .toList(growable: false);
    final status = widget.status.message;

    return AlertDialog(
      title: const Text(findAssetTitle),
      content: SizedBox(
        width: 420,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(findAssetHelper),
            if (status != null) ...[
              const SizedBox(height: 8),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(
                    Icons.info_outline,
                    size: 18,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  const SizedBox(width: 8),
                  Expanded(child: Text(status)),
                ],
              ),
            ],
            const SizedBox(height: 12),
            TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                labelText: 'Search assets',
                border: OutlineInputBorder(),
              ),
              onChanged: (value) => setState(() => _query = value),
            ),
            const SizedBox(height: 12),
            ConstrainedBox(
              constraints: const BoxConstraints(maxHeight: 280),
              child: filtered.isEmpty
                  ? const Padding(
                      padding: EdgeInsets.symmetric(vertical: 16),
                      child: Text(noMatchingAssets),
                    )
                  : ListView.builder(
                      shrinkWrap: true,
                      itemCount: filtered.length,
                      itemBuilder: (context, index) {
                        final asset = filtered[index];
                        return ListTile(
                          leading: const Icon(Icons.inventory_2_outlined),
                          title: Text(asset.label),
                          onTap: () => _confirmAsset(context, asset),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        TextButton(
          onPressed: () => _saveCandidate(context),
          child: const Text(missingAssetAction),
        ),
      ],
    );
  }

  Future<void> _confirmAsset(
    BuildContext context,
    _FieldAssetOption asset,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Confirm asset'),
        content: Text('$confirmAssetCopy\n\n${asset.label}'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Back'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Confirm'),
          ),
        ],
      ),
    );
    if (confirmed == true && context.mounted) {
      Navigator.pop(context, _FieldAssetDialogResult.selected(asset.subjectId));
    }
  }

  Future<void> _saveCandidate(BuildContext context) async {
    var candidateLabel = _query.trim();
    final label = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text(missingAssetAction),
        content: TextFormField(
          initialValue: candidateLabel,
          autofocus: true,
          onChanged: (value) => candidateLabel = value,
          decoration: const InputDecoration(
            labelText: 'Candidate asset label',
            helperText: candidateNeedsReviewCopy,
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, candidateLabel.trim()),
            child: const Text(saveCandidateAction),
          ),
        ],
      ),
    );
    if (label != null && label.trim().isNotEmpty && context.mounted) {
      Navigator.pop(context, _FieldAssetDialogResult.candidate(label.trim()));
    }
  }
}

enum _SessionAction { switchUser, signOut }

enum _WorkReadinessKind {
  needsSync,
  syncing,
  failed,
  setupUnavailable,
  noAssignment,
  ready,
}

class _WorkReadiness {
  final _WorkReadinessKind kind;
  final String title;
  final String message;
  final String? detail;
  final String? actionLabel;
  final bool hasCaptureForms;

  const _WorkReadiness({
    required this.kind,
    required this.title,
    required this.message,
    this.detail,
    this.actionLabel,
    required this.hasCaptureForms,
  });

  bool get isReady => kind == _WorkReadinessKind.ready;
  bool get isSyncing => kind == _WorkReadinessKind.syncing;
}

class _WorkReadinessCard extends StatelessWidget {
  final _WorkReadiness readiness;
  final Future<void> Function() onSync;
  final bool centered;

  const _WorkReadinessCard({
    required this.readiness,
    required this.onSync,
    this.centered = false,
  });

  @override
  Widget build(BuildContext context) {
    final content = Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: centered
          ? CrossAxisAlignment.center
          : CrossAxisAlignment.start,
      children: [
        if (readiness.isSyncing)
          const Padding(
            padding: EdgeInsets.only(bottom: 16),
            child: CircularProgressIndicator(),
          )
        else
          Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Icon(
              _icon,
              size: 32,
              color: Theme.of(context).colorScheme.primary,
            ),
          ),
        Text(
          readiness.title,
          textAlign: centered ? TextAlign.center : TextAlign.start,
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 6),
        Text(
          readiness.message,
          textAlign: centered ? TextAlign.center : TextAlign.start,
        ),
        if (readiness.detail != null) ...[
          const SizedBox(height: 8),
          Text(
            readiness.detail!,
            textAlign: centered ? TextAlign.center : TextAlign.start,
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
        if (readiness.actionLabel != null) ...[
          const SizedBox(height: 16),
          FilledButton(
            onPressed: readiness.isSyncing ? null : onSync,
            child: Text(readiness.actionLabel!),
          ),
        ],
      ],
    );

    if (centered) {
      return Padding(
        padding: const EdgeInsets.all(32),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: content,
        ),
      );
    }

    return Material(
      color: Theme.of(context).colorScheme.surfaceContainerLow,
      child: Padding(padding: const EdgeInsets.all(20), child: content),
    );
  }

  IconData get _icon {
    return switch (readiness.kind) {
      _WorkReadinessKind.needsSync => Icons.cloud_download_outlined,
      _WorkReadinessKind.syncing => Icons.sync,
      _WorkReadinessKind.failed => Icons.cloud_off_outlined,
      _WorkReadinessKind.setupUnavailable => Icons.assignment_late_outlined,
      _WorkReadinessKind.noAssignment => Icons.work_outline,
      _WorkReadinessKind.ready => Icons.check_circle_outline,
    };
  }
}

class _PendingSyncStatus extends StatelessWidget {
  final int count;
  final VoidCallback onTap;

  const _PendingSyncStatus({required this.count, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Material(
      color: colors.secondaryContainer,
      child: ListTile(
        leading: const Icon(Icons.cloud_upload_outlined),
        title: Text(
          '$count record${count == 1 ? '' : 's'} saved on this device',
        ),
        subtitle: const Text('Waiting to sync.'),
        trailing: const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }
}
