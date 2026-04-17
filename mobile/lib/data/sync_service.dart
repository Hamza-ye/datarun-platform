import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/event.dart';

class SyncResult {
  final int pushedCount;
  final int pulledCount;
  final String? error;

  SyncResult({required this.pushedCount, required this.pulledCount, this.error});
}

class SyncService {
  final EventStore _eventStore;
  final String _baseUrl;
  static const _watermarkKey = 'sync_watermark';

  SyncService(this._eventStore, this._baseUrl);

  Future<SyncResult> sync() async {
    int pushed = 0;
    int pulled = 0;

    // Push phase
    try {
      final unpushed = await _eventStore.getUnpushed();
      if (unpushed.isNotEmpty) {
        final response = await http.post(
          Uri.parse('$_baseUrl/api/sync/push'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'events': unpushed.map((e) => e.toEnvelope()).toList(),
          }),
        );
        if (response.statusCode == 200) {
          final body = jsonDecode(response.body) as Map<String, dynamic>;
          pushed = (body['accepted'] as int) + (body['duplicates'] as int);
          await _eventStore.markPushed(unpushed.map((e) => e.id).toList());
        } else {
          return SyncResult(
              pushedCount: 0,
              pulledCount: 0,
              error: 'Push failed: ${response.statusCode}');
        }
      }
    } on Exception {
      return SyncResult(
          pushedCount: 0, pulledCount: 0, error: 'No connection');
    }

    // Pull phase
    try {
      final prefs = await SharedPreferences.getInstance();
      var watermark = prefs.getInt(_watermarkKey) ?? 0;

      while (true) {
        final response = await http.post(
          Uri.parse('$_baseUrl/api/sync/pull'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'since_watermark': watermark,
            'limit': 100,
          }),
        );
        if (response.statusCode != 200) break;

        final body = jsonDecode(response.body) as Map<String, dynamic>;
        final events = (body['events'] as List)
            .map((e) => Event.fromServerJson(e as Map<String, dynamic>))
            .toList();

        if (events.isEmpty) break;

        for (final event in events) {
          await _eventStore.insertFromServer(event);
        }
        pulled += events.length;

        final latestWatermark = body['latest_watermark'] as int;
        watermark = latestWatermark;
        await prefs.setInt(_watermarkKey, watermark);

        if (events.length < 100) break; // Last page
      }
    } on Exception {
      // Pull errors are non-fatal — pushed data is already safe
    }

    return SyncResult(pushedCount: pushed, pulledCount: pulled);
  }
}
