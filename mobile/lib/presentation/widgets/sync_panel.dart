import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';

/// U1: Sync Panel — modal bottom sheet.
class SyncPanel extends StatelessWidget {
  const SyncPanel({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (context, state, _) {
        final result = state.lastSyncResult;
        return Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Sync', style: Theme.of(context).textTheme.titleLarge),
                  IconButton(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.close),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              _buildStatus(context, state),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: state.isSyncing ? null : state.sync,
                  child: state.isSyncing
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            color: Colors.white,
                          ),
                        )
                      : Text(result?.error != null ? 'Try Again' : 'Sync Now'),
                ),
              ),
              const SizedBox(height: 12),
              if (state.lastSync != null)
                Text(
                  'Last successful sync: ${_formatTime(state.lastSync!)}',
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

  Widget _buildStatus(BuildContext context, AppState state) {
    final textTheme = Theme.of(context).textTheme;
    final result = state.lastSyncResult;

    if (state.isSyncing) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Syncing now', style: textTheme.titleMedium),
          const Text('Sending saved records and checking for updates.'),
        ],
      );
    }

    if (result?.error != null) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Sync failed',
            style: textTheme.titleMedium?.copyWith(
              color: Theme.of(context).colorScheme.error,
            ),
          ),
          if (state.pendingCount > 0)
            Text(
              '${_count(state.pendingCount, 'record')} still saved on this device and waiting to sync.',
            ),
          const Text('Try again when the connection or account is available.'),
          const SizedBox(height: 8),
          Text(result!.error!, style: textTheme.bodySmall),
        ],
      );
    }

    if (result != null) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Sync complete', style: textTheme.titleMedium),
          Text('${_count(result.pushedCount, 'record')} sent.'),
          Text('${_count(result.pulledCount, 'update')} received.'),
        ],
      );
    }

    if (state.pendingCount > 0) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '${_count(state.pendingCount, 'record')} saved on this device',
            style: textTheme.titleMedium,
          ),
          const Text('Waiting to sync.'),
        ],
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          state.lastSync == null ? 'Not synced yet' : 'Synced',
          style: textTheme.titleMedium,
        ),
        const Text('No records waiting to sync.'),
      ],
    );
  }

  String _count(int count, String noun) {
    return '$count $noun${count == 1 ? '' : 's'}';
  }

  String _formatTime(DateTime dt) {
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}
