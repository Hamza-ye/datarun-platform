import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/device_identity.dart';
import 'package:datarun_mobile/domain/event.dart';

class SyncResult {
  final int pushedCount;
  final int pulledCount;
  final String? error;

  SyncResult({required this.pushedCount, required this.pulledCount, this.error});
}

class SyncService {
  final EventStore _eventStore;
  final DeviceIdentity _identity;
  final String _baseUrl;
  final ConfigStore _configStore;
  static const _watermarkKey = 'sync_watermark';

  SyncService(this._eventStore, this._identity, this._baseUrl, this._configStore);

  Future<SyncResult> sync() async {
    int pushed = 0;
    int pulled = 0;

    // Push phase
    try {
      final unpushed = await _eventStore.getUnpushed();
      if (unpushed.isNotEmpty) {
        final prefs = await SharedPreferences.getInstance();
        final lastPullWatermark = prefs.getInt(_watermarkKey) ?? 0;
        final response = await http.post(
          Uri.parse('$_baseUrl/api/sync/push'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'events': unpushed.map((e) => e.toEnvelope()).toList(),
            'device_id': _identity.deviceId,
            'last_pull_watermark': lastPullWatermark,
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

      // Build pull headers: include actor token if available (Phase 2b auth)
      final pullHeaders = <String, String>{'Content-Type': 'application/json'};
      final token = _identity.actorToken;
      if (token != null) {
        pullHeaders['Authorization'] = 'Bearer $token';
      }

      while (true) {
        final response = await http.post(
          Uri.parse('$_baseUrl/api/sync/pull'),
          headers: pullHeaders,
          body: jsonEncode({
            'since_watermark': watermark,
            'limit': 100,
            'config_version': _configStore.configVersion,
          }),
        );
        if (response.statusCode == 401) {
          return SyncResult(
              pushedCount: pushed,
              pulledCount: pulled,
              error: 'Unauthorized — invalid or missing actor token');
        }
        if (response.statusCode != 200) break;

        final body = jsonDecode(response.body) as Map<String, dynamic>;
        final events = (body['events'] as List)
            .map((e) => Event.fromServerJson(e as Map<String, dynamic>))
            .toList();

        if (events.isEmpty) break;

        for (final event in events) {
          await _eventStore.insertFromServer(event);
          // Process subjects_merged events to update local alias table
          if (event.type == 'subjects_merged') {
            final retiredId = event.payload['retired_id'] as String?;
            final survivingId = event.payload['surviving_id'] as String?;
            if (retiredId != null && survivingId != null) {
              await _eventStore.upsertAlias(retiredId, survivingId, event.timestamp);
            }
          }
          // Process assignment events to maintain local scope knowledge (Phase 2b)
          if (event.type == 'assignment_changed') {
            await _eventStore.processAssignmentEvent(event);
          }
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

    // Config download: fetch new config if pull response indicated a newer version
    try {
      final configHeaders = <String, String>{
        'Content-Type': 'application/json',
      };
      final token = _identity.actorToken;
      if (token != null) {
        configHeaders['Authorization'] = 'Bearer $token';
      }
      final currentVersion = _configStore.configVersion;
      if (currentVersion > 0) {
        configHeaders['If-None-Match'] = '$currentVersion';
      }
      final configResponse = await http.get(
        Uri.parse('$_baseUrl/api/sync/config'),
        headers: configHeaders,
      );
      if (configResponse.statusCode == 200) {
        final configBody =
            jsonDecode(configResponse.body) as Map<String, dynamic>;
        await _configStore.applyConfig(configBody);
      }
      // 304 Not Modified — skip
    } on Exception {
      // Config download errors are non-fatal
    }

    // Selective-retain: purge out-of-scope events from other actors (Phase 2c)
    if (pulled > 0) {
      try {
        await _eventStore.purgeOutOfScopeEvents(_identity.deviceId);
      } on Exception {
        // Purge errors are non-fatal
      }
    }

    return SyncResult(pushedCount: pushed, pulledCount: pulled);
  }
}
