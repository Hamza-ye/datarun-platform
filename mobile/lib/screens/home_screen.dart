import 'package:flutter/material.dart';

import '../data/db.dart';
import '../data/prefs.dart';
import '../net/sync_client.dart';
import 'capture_screen.dart';
import 'setup_screen.dart';

class HomeScreen extends StatefulWidget {
  final Prefs prefs;
  final LocalDb db;
  final SyncClient client;
  const HomeScreen({
    super.key,
    required this.prefs,
    required this.db,
    required this.client,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<EventRow> _events = [];
  bool _syncing = false;
  String? _lastSyncMessage;

  @override
  void initState() {
    super.initState();
    _refreshList();
  }

  Future<void> _refreshList() async {
    final rows = await widget.db.allEvents();
    if (!mounted) return;
    setState(() => _events = rows);
  }

  Future<void> _sync() async {
    setState(() {
      _syncing = true;
      _lastSyncMessage = null;
    });
    try {
      // Refresh config (villages) opportunistically.
      await widget.client.fetchConfig();
      final push = await widget.client.pushPending();
      final pull = await widget.client.pullAll();
      final parts = <String>[];
      if (push != null) parts.add('push: $push');
      parts.add('pull: received=${pull.received} latest_wm=${pull.latestWatermark}');
      setState(() => _lastSyncMessage = parts.join('  ·  '));
    } catch (e) {
      setState(() => _lastSyncMessage = 'Sync error: $e');
    } finally {
      if (mounted) setState(() => _syncing = false);
      await _refreshList();
    }
  }

  Future<void> _openCapture() async {
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => CaptureScreen(
          prefs: widget.prefs,
          db: widget.db,
        ),
      ),
    );
    await _refreshList();
  }

  Future<void> _reprovision() async {
    await widget.prefs.clear();
    if (!mounted) return;
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => SetupScreen(
          prefs: widget.prefs,
          db: widget.db,
          client: widget.client,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final pending = _events.where((e) => e.syncState == 'pending').length;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Datarun Mobile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Re-provision',
            onPressed: _reprovision,
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            color: Colors.teal.shade50,
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Actor: ${widget.prefs.actorId}',
                    style: const TextStyle(fontSize: 11)),
                Text('Device: ${widget.prefs.deviceId}',
                    style: const TextStyle(fontSize: 11)),
                Text('Pending (offline): $pending',
                    style: const TextStyle(fontWeight: FontWeight.bold)),
                if (_lastSyncMessage != null) ...[
                  const SizedBox(height: 6),
                  Text(_lastSyncMessage!, style: const TextStyle(fontSize: 11)),
                ],
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8),
            child: Row(
              children: [
                Expanded(
                  child: FilledButton.icon(
                    onPressed: _syncing ? null : _sync,
                    icon: _syncing
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2))
                        : const Icon(Icons.sync),
                    label: Text(_syncing ? 'Syncing…' : 'Sync'),
                  ),
                ),
              ],
            ),
          ),
          const Divider(height: 1),
          Expanded(
            child: _events.isEmpty
                ? const Center(
                    child: Text('No events yet. Tap + to capture a household.'))
                : ListView.separated(
                    itemCount: _events.length,
                    separatorBuilder: (_, i) => const Divider(height: 1),
                    itemBuilder: (_, i) => _EventTile(_events[i]),
                  ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openCapture,
        icon: const Icon(Icons.add),
        label: const Text('New capture'),
      ),
    );
  }
}

class _EventTile extends StatelessWidget {
  final EventRow row;
  const _EventTile(this.row);

  Color _stateColor() {
    switch (row.syncState) {
      case 'pending':
        return Colors.orange;
      case 'synced':
        return Colors.green;
      case 'remote':
        return Colors.blue;
    }
    return Colors.grey;
  }

  String _summary() {
    final payload = row.envelope['payload'] as Map? ?? {};
    if (row.shapeRef == 'household_observation/v1') {
      return '${payload['household_name']}  ·  size ${payload['household_size']}';
    }
    if (row.shapeRef == 'conflict_detected/v1') {
      return 'FLAG: ${payload['category']}  ·  ${payload['reason'] ?? ''}';
    }
    if (row.shapeRef.startsWith('assignment_')) {
      return 'Assignment';
    }
    return row.shapeRef;
  }

  @override
  Widget build(BuildContext context) {
    final isFlag = row.shapeRef == 'conflict_detected/v1';
    return ListTile(
      leading: CircleAvatar(
        backgroundColor: _stateColor(),
        radius: 8,
      ),
      title: Text(
        _summary(),
        style: TextStyle(
          fontWeight: isFlag ? FontWeight.bold : FontWeight.normal,
          color: isFlag ? Colors.red.shade800 : null,
        ),
      ),
      subtitle: Text(
        '${row.shapeRef}  ·  seq=${row.deviceSeq}  ·  ${row.syncState}',
        style: const TextStyle(fontSize: 11),
      ),
      dense: true,
    );
  }
}
