import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';

import '../data/db.dart';
import '../data/prefs.dart';

/// Shape-driven capture form for `household_observation/v1`.
///
/// Offline-safe: writes to local DB as `pending`. No network call. Sync is
/// triggered explicitly from the Home screen.
class CaptureScreen extends StatefulWidget {
  final Prefs prefs;
  final LocalDb db;
  const CaptureScreen({super.key, required this.prefs, required this.db});

  @override
  State<CaptureScreen> createState() => _CaptureScreenState();
}

class _CaptureScreenState extends State<CaptureScreen> {
  final _formKey = GlobalKey<FormState>();
  final _householdName = TextEditingController();
  final _headOfHousehold = TextEditingController();
  final _householdSize = TextEditingController();
  final _notes = TextEditingController();
  List<Village> _villages = [];
  Village? _selectedVillage;
  bool _loading = true;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _loadVillages();
  }

  Future<void> _loadVillages() async {
    final list = await widget.db.villages();
    if (!mounted) return;
    setState(() {
      _villages = list;
      _selectedVillage = list.isNotEmpty ? list.first : null;
      _loading = false;
    });
  }

  @override
  void dispose() {
    _householdName.dispose();
    _headOfHousehold.dispose();
    _householdSize.dispose();
    _notes.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedVillage == null) return;
    setState(() => _saving = true);

    final now = DateTime.now().toUtc().toIso8601String();
    final seq = await widget.prefs.nextDeviceSeq();
    final subjectId = const Uuid().v4();

    final envelope = <String, Object?>{
      'id': const Uuid().v4(),
      'type': 'capture',
      'shape_ref': 'household_observation/v1',
      'activity_ref': 'household_observation',
      'subject_ref': {'type': 'subject', 'id': subjectId},
      'actor_ref': {'type': 'actor', 'id': widget.prefs.actorId},
      'device_id': widget.prefs.deviceId,
      'device_seq': seq,
      'sync_watermark': null,
      'timestamp': now,
      'payload': {
        'household_name': _householdName.text.trim(),
        'head_of_household_name': _headOfHousehold.text.trim(),
        'household_size': int.parse(_householdSize.text.trim()),
        'village_ref': _selectedVillage!.id,
        'latitude': null,
        'longitude': null,
        'visit_notes': _notes.text.trim().isEmpty ? null : _notes.text.trim(),
      },
    };
    await widget.db.insertLocalCapture(envelope);
    if (!mounted) return;
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (_villages.isEmpty) {
      return Scaffold(
        appBar: AppBar(title: const Text('New household capture')),
        body: const Padding(
          padding: EdgeInsets.all(16),
          child: Text(
            'No villages in local config. Sync from the Home screen first '
            '(server must have assigned this CHV to at least one village).',
          ),
        ),
      );
    }
    return Scaffold(
      appBar: AppBar(title: const Text('New household capture')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            DropdownButtonFormField<Village>(
              initialValue: _selectedVillage,
              decoration: const InputDecoration(labelText: 'Village'),
              items: _villages
                  .map((v) => DropdownMenuItem(
                        value: v,
                        child: Text('${v.name} (${v.districtName})'),
                      ))
                  .toList(),
              onChanged: (v) => setState(() => _selectedVillage = v),
              validator: (v) => v == null ? 'Required' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _householdName,
              decoration: const InputDecoration(labelText: 'Household name'),
              validator: (v) =>
                  v == null || v.trim().isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _headOfHousehold,
              decoration:
                  const InputDecoration(labelText: 'Head of household'),
              validator: (v) =>
                  v == null || v.trim().isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _householdSize,
              decoration: const InputDecoration(labelText: 'Household size'),
              keyboardType: TextInputType.number,
              validator: (v) {
                final n = int.tryParse(v?.trim() ?? '');
                if (n == null || n < 1) return 'Must be ≥ 1';
                return null;
              },
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _notes,
              decoration: const InputDecoration(labelText: 'Visit notes'),
              maxLines: 3,
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: _saving ? null : _save,
              icon: const Icon(Icons.save),
              label: Text(_saving ? 'Saving…' : 'Save (offline)'),
            ),
            const SizedBox(height: 8),
            const Text(
              'Saved locally as pending. Use Sync from the Home screen to push.',
              style: TextStyle(fontSize: 11, color: Colors.black54),
            ),
          ],
        ),
      ),
    );
  }
}
