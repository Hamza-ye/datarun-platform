import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';

/// ADR-002 Addendum (Phase 3e, DD-2): integrity and identity events are
/// discriminated by `shape_ref`, never by envelope `type`. The four named
/// predicates below replace the former `_isSystemEventType` string switch.
/// Each predicate answers a single architectural question.
bool isIntegrityFlag(Event e) => e.shapeRef.startsWith('conflict_detected/');

bool isIntegrityResolution(Event e) =>
    e.shapeRef.startsWith('conflict_resolved/');

bool isIdentityLifecycle(Event e) =>
    e.shapeRef.startsWith('subjects_merged/') ||
    e.shapeRef.startsWith('subject_split/');

/// assignment_changed remains an envelope-level type (ADR-4 S3), not a shape.
bool isAssignmentEvent(Event e) => e.type == 'assignment_changed';

/// True if the event was authored by the platform / server and should be
/// excluded from domain-subject state derivation.
bool _isNonDomainEvent(Event e) =>
    isIntegrityFlag(e) ||
    isIntegrityResolution(e) ||
    isIdentityLifecycle(e) ||
    isAssignmentEvent(e);

class ProjectionEngine {
  final EventStore _eventStore;

  ProjectionEngine(this._eventStore);

  /// Derive subject list from all local events. Full replay with alias resolution + flag exclusion.
  /// Since sync is scope-filtered (server returns only authorized events), the work list
  /// shows only in-scope subjects by construction. No client-side scope filtering needed.
  Future<List<SubjectSummary>> getSubjectList() async {
    final events = await _eventStore.getAll();
    final aliases = await _eventStore.getAllAliases();

    // Build flagged event set from conflict_detected flag events.
    final flaggedEventIds = <String>{};
    final resolvedFlagIds = <String>{};

    for (final e in events) {
      if (isIntegrityFlag(e)) {
        final sourceId = e.payload['source_event_id'] as String?;
        if (sourceId != null) flaggedEventIds.add(sourceId);
      }
    }

    // Un-flag events that have been resolved with accepted or reclassified.
    for (final e in events) {
      if (isIntegrityResolution(e)) {
        final resolution = e.payload['resolution'] as String?;
        final sourceId = e.payload['source_event_id'] as String?;
        if (sourceId != null &&
            (resolution == 'accepted' || resolution == 'reclassified')) {
          flaggedEventIds.remove(sourceId);
        }
        // Track all resolved flag IDs regardless of resolution type.
        final flagEventId = e.payload['flag_event_id'] as String?;
        if (flagEventId != null) resolvedFlagIds.add(flagEventId);
      }
    }

    // Count unresolved flags per subject (for badge display).
    final flagCountBySubject = <String, int>{};
    for (final e in events) {
      if (isIntegrityFlag(e) && !resolvedFlagIds.contains(e.id)) {
        final sid = _resolveSubjectId(e.subjectRef['id']!, aliases);
        flagCountBySubject[sid] = (flagCountBySubject[sid] ?? 0) + 1;
      }
    }

    // Group domain events by resolved subject.
    final bySubject = <String, List<Event>>{};
    for (final e in events) {
      if (_isNonDomainEvent(e)) continue;
      final sid = _resolveSubjectId(e.subjectRef['id']!, aliases);
      bySubject.putIfAbsent(sid, () => []).add(e);
    }

    return bySubject.entries.map((entry) {
      final subjectEvents = entry.value;
      // Events are already sorted DESC by timestamp from EventStore
      final latest = subjectEvents.first;

      // For state derivation, exclude flagged events
      final stateEvents =
          subjectEvents.where((e) => !flaggedEventIds.contains(e.id)).toList();

      // Try to extract name from earliest capture payload (state events only).
      // subjectEvents already excludes non-domain events, so type == 'capture'
      // here is safe to treat as a domain capture.
      final firstCapture = stateEvents.reversed
          .where((e) => e.type == 'capture')
          .firstOrNull;
      final name = firstCapture?.payload['name'] as String?;

      return SubjectSummary(
        subjectId: entry.key,
        subjectType: latest.subjectRef['type']!,
        name: name,
        latestTimestamp: latest.timestamp,
        captureCount:
            stateEvents.where((e) => e.type == 'capture').length,
        flagCount: flagCountBySubject[entry.key] ?? 0,
      );
    }).toList()
      ..sort((a, b) => b.latestTimestamp.compareTo(a.latestTimestamp));
  }

  /// Full event timeline for one subject.
  /// Returns all events including flagged (UI marks them), with alias resolution.
  Future<List<Event>> getSubjectDetail(String subjectId) async {
    final events = await _eventStore.getBySubject(subjectId);

    // Also fetch events for any retired IDs that alias to this subject
    final aliases = await _eventStore.getAllAliases();
    final retiredIds = aliases.entries
        .where((e) => e.value == subjectId)
        .map((e) => e.key)
        .toList();

    final allEvents = [...events];
    for (final retiredId in retiredIds) {
      final retiredEvents = await _eventStore.getBySubject(retiredId);
      allEvents.addAll(retiredEvents);
    }

    // Deduplicate (in case of overlap) and sort DESC by timestamp
    final seen = <String>{};
    final deduped = <Event>[];
    for (final e in allEvents) {
      if (seen.add(e.id)) deduped.add(e);
    }
    deduped.sort((a, b) => b.timestamp.compareTo(a.timestamp));
    return deduped;
  }

  /// Compute the set of flagged event IDs (for UI indicators).
  Future<Set<String>> getFlaggedEventIds() async {
    final events = await _eventStore.getAll();
    final flagged = <String>{};

    for (final e in events) {
      if (isIntegrityFlag(e)) {
        final sourceId = e.payload['source_event_id'] as String?;
        if (sourceId != null) flagged.add(sourceId);
      }
    }

    // Remove events resolved with accepted or reclassified
    for (final e in events) {
      if (isIntegrityResolution(e)) {
        final resolution = e.payload['resolution'] as String?;
        final sourceId = e.payload['source_event_id'] as String?;
        if (sourceId != null &&
            (resolution == 'accepted' || resolution == 'reclassified')) {
          flagged.remove(sourceId);
        }
      }
    }

    return flagged;
  }

  /// Resolve a subject ID through the alias table (single-hop after eager closure).
  String _resolveSubjectId(String subjectId, Map<String, String> aliases) {
    return aliases[subjectId] ?? subjectId;
  }
}
