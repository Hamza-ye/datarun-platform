import 'dart:convert';
import 'package:http/http.dart' as http;

import '../data/db.dart';
import '../data/prefs.dart';

class SyncException implements Exception {
  final String message;
  final int? statusCode;
  final String? body;
  SyncException(this.message, {this.statusCode, this.body});
  @override
  String toString() =>
      'SyncException($statusCode): $message${body != null ? "\n$body" : ""}';
}

class PushResult {
  final int accepted;
  final int duplicates;
  final int flagsRaised;
  PushResult(this.accepted, this.duplicates, this.flagsRaised);
  @override
  String toString() =>
      'accepted=$accepted duplicates=$duplicates flags_raised=$flagsRaised';
}

class PullResult {
  final int received;
  final int latestWatermark;
  PullResult(this.received, this.latestWatermark);
}

/// Wire client for /api/sync/{config,push,pull}. Bearer-authenticated on every
/// request. Protocol documented in `contracts/sync-protocol.md`.
class SyncClient {
  final Prefs prefs;
  final LocalDb db;
  final http.Client _http;

  SyncClient(this.prefs, this.db, {http.Client? httpClient})
      : _http = httpClient ?? http.Client();

  Map<String, String> get _authHeaders => {
        'Authorization': 'Bearer ${prefs.token}',
        'Content-Type': 'application/json',
      };

  Uri _uri(String path) => Uri.parse('${prefs.serverUrl}$path');

  /// Fetch config (scope-filtered villages). Persists villages locally.
  Future<void> fetchConfig() async {
    final resp = await _http.get(_uri('/api/sync/config'), headers: _authHeaders);
    if (resp.statusCode != 200) {
      throw SyncException('config fetch failed',
          statusCode: resp.statusCode, body: resp.body);
    }
    final body = jsonDecode(resp.body) as Map<String, Object?>;
    final villages =
        (body['villages'] as List).cast<Map<String, Object?>>();
    await db.replaceVillages(villages);
  }

  /// Push every pending event. On 200 OK each pending row is marked synced.
  /// Batch is all-or-nothing on validation — a 400 leaves rows pending so the
  /// user can inspect and retry. Idempotent: re-sending already-synced events
  /// simply counts as duplicates.
  Future<PushResult?> pushPending() async {
    final pending = await db.pendingEnvelopes();
    if (pending.isEmpty) return null;
    final resp = await _http.post(
      _uri('/api/sync/push'),
      headers: _authHeaders,
      body: jsonEncode({'events': pending}),
    );
    if (resp.statusCode != 200) {
      throw SyncException('push failed',
          statusCode: resp.statusCode, body: resp.body);
    }
    final body = jsonDecode(resp.body) as Map<String, Object?>;
    // We don't know per-event sync_watermark from the aggregate response; mark
    // them synced with watermark=null-in-DB-but-acked. Pull will later replace
    // with authoritative watermarks.
    for (final env in pending) {
      await db.markSynced(env['id'] as String, -1);
    }
    return PushResult(
      body['accepted'] as int,
      body['duplicates'] as int,
      body['flags_raised'] as int,
    );
  }

  /// Pull everything newer than the last watermark. Paginates until drained.
  Future<PullResult> pullAll() async {
    int watermark = prefs.lastPullWatermark;
    int received = 0;
    while (true) {
      final resp = await _http.post(
        _uri('/api/sync/pull'),
        headers: _authHeaders,
        body: jsonEncode({'since_watermark': watermark, 'limit': 100}),
      );
      if (resp.statusCode != 200) {
        throw SyncException('pull failed',
            statusCode: resp.statusCode, body: resp.body);
      }
      final body = jsonDecode(resp.body) as Map<String, Object?>;
      final events = (body['events'] as List).cast<Map<String, Object?>>();
      for (final env in events) {
        await db.upsertRemote(env);
      }
      received += events.length;
      final latest = (body['latest_watermark'] as num).toInt();
      if (latest <= watermark) break;
      watermark = latest;
      if (events.length < 100) break;
    }
    await prefs.setLastPullWatermark(watermark);
    return PullResult(received, watermark);
  }

  void dispose() => _http.close();
}
