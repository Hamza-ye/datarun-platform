import 'package:flutter/material.dart';

import '../data/db.dart';
import '../data/prefs.dart';
import '../net/sync_client.dart';
import 'home_screen.dart';

/// First-launch / re-provisioning screen. Accepts server URL + actor_id +
/// bearer token, typically copied from `POST /dev/bootstrap`.
class SetupScreen extends StatefulWidget {
  final Prefs prefs;
  final LocalDb db;
  final SyncClient client;
  const SetupScreen({
    super.key,
    required this.prefs,
    required this.db,
    required this.client,
  });

  @override
  State<SetupScreen> createState() => _SetupScreenState();
}

class _SetupScreenState extends State<SetupScreen> {
  late final TextEditingController _url;
  late final TextEditingController _actorId;
  late final TextEditingController _token;
  bool _busy = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _url = TextEditingController(
        text: widget.prefs.serverUrl ?? 'http://10.0.2.2:8080');
    _actorId = TextEditingController(text: widget.prefs.actorId ?? '');
    _token = TextEditingController(text: widget.prefs.token ?? '');
  }

  @override
  void dispose() {
    _url.dispose();
    _actorId.dispose();
    _token.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await widget.prefs.setProvisioning(
        serverUrl: _url.text.trim(),
        actorId: _actorId.text.trim(),
        token: _token.text.trim(),
      );
      await widget.client.fetchConfig();
      if (!mounted) return;
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (_) => HomeScreen(
            prefs: widget.prefs,
            db: widget.db,
            client: widget.client,
          ),
        ),
      );
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Datarun — Device setup')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const Text(
              'Paste values from POST /dev/bootstrap. '
              'Android emulator → host is 10.0.2.2.',
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _url,
              decoration: const InputDecoration(
                labelText: 'Server URL',
                hintText: 'http://10.0.2.2:8080',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _actorId,
              decoration: const InputDecoration(labelText: 'Actor ID (UUID)'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _token,
              decoration: const InputDecoration(labelText: 'Bearer token'),
              obscureText: true,
            ),
            const SizedBox(height: 12),
            Text('Device ID: ${widget.prefs.deviceId}',
                style: const TextStyle(fontFamily: 'monospace', fontSize: 11)),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _busy ? null : _submit,
              child: Text(_busy ? 'Connecting…' : 'Save & fetch config'),
            ),
            if (_error != null) ...[
              const SizedBox(height: 16),
              Text(_error!, style: const TextStyle(color: Colors.red)),
            ],
          ],
        ),
      ),
    );
  }
}
