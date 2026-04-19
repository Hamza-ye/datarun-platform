import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/subject_detail_screen.dart';
import 'package:datarun_mobile/presentation/screens/form_screen.dart';
import 'package:datarun_mobile/presentation/widgets/sync_panel.dart';

/// S1: Work List — subject-centric entry point.
class WorkListScreen extends StatelessWidget {
  const WorkListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (context, state, _) {
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
                    style: const TextStyle(fontSize: 12, fontWeight: FontWeight.normal),
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
                              horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.orange,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            '${state.pendingCount}',
                            style: const TextStyle(
                                fontSize: 12, color: Colors.white),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ],
          ),
          body: state.subjects.isEmpty
              ? const Center(
                  child: Text('No subjects yet.\nTap + to create one.',
                      textAlign: TextAlign.center))
              : ListView.builder(
                  itemCount: state.subjects.length,
                  itemBuilder: (context, index) {
                    final s = state.subjects[index];
                    return ListTile(
                      title: Row(
                        children: [
                          Expanded(child: Text(s.name ?? 'Unnamed subject')),
                          if (s.flagCount > 0)
                            Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: Colors.red,
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: Text(
                                '${s.flagCount} flag${s.flagCount == 1 ? '' : 's'}',
                                style: const TextStyle(
                                    fontSize: 11, color: Colors.white),
                              ),
                            ),
                        ],
                      ),
                      subtitle: Text(
                          '${s.captureCount} capture${s.captureCount == 1 ? '' : 's'} · ${_formatTimestamp(s.latestTimestamp)}'),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) =>
                                SubjectDetailScreen(subjectId: s.subjectId),
                          ),
                        );
                      },
                    );
                  },
                ),
          floatingActionButton: FloatingActionButton(
            onPressed: () => _addNew(context),
            child: const Icon(Icons.add),
          ),
        );
      },
    );
  }

  void _addNew(BuildContext context) {
    // S1 → S3 shortcut: new subject, skip detail
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => const FormScreen(
          subjectId: null,
          shapeRef: 'basic_capture/v1',
        ),
      ),
    ).then((_) {
      // Refresh on return
      if (context.mounted) {
        context.read<AppState>().refresh();
      }
    });
  }

  void _showSyncPanel(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (_) => const SyncPanel(),
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
