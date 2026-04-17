import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';

/// U1: Sync Panel — modal bottom sheet.
class SyncPanel extends StatefulWidget {
  const SyncPanel({super.key});

  @override
  State<SyncPanel> createState() => _SyncPanelState();
}

class _SyncPanelState extends State<SyncPanel> {
  String? _resultMessage;

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (context, state, _) {
        return Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Sync',
                      style: Theme.of(context).textTheme.titleLarge),
                  IconButton(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.close),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Text('● Push: ${state.pendingCount} event${state.pendingCount == 1 ? '' : 's'} to send'),
              const Text('○ Pull: checking for updates'),
              const SizedBox(height: 16),
              if (_resultMessage != null) ...[
                Text(_resultMessage!,
                    style: TextStyle(
                        color: _resultMessage!.contains('failed')
                            ? Colors.red
                            : Colors.green)),
                const SizedBox(height: 8),
              ],
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: state.isSyncing ? null : () => _sync(context),
                  child: state.isSyncing
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(
                              strokeWidth: 2, color: Colors.white))
                      : const Text('Sync Now'),
                ),
              ),
              const SizedBox(height: 12),
              Text(
                state.lastSync != null
                    ? 'Last sync: ${_formatTime(state.lastSync!)}'
                    : 'Never synced',
                style: Theme.of(context).textTheme.bodySmall,
              ),
              Text(
                'Device ID: ${state.identity.deviceId.substring(0, 8)}...',
                style: Theme.of(context).textTheme.bodySmall,
              ),
              const SizedBox(height: 8),
            ],
          ),
        );
      },
    );
  }

  Future<void> _sync(BuildContext context) async {
    final state = context.read<AppState>();
    final result = await state.sync();
    if (mounted) {
      setState(() {
        if (result.error != null) {
          _resultMessage = 'Sync failed: ${result.error}';
        } else {
          _resultMessage =
              '● Push: ${result.pushedCount} sent ✓\n○ Pull: ${result.pulledCount} received ✓';
        }
      });
    }
  }

  String _formatTime(DateTime dt) {
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}
