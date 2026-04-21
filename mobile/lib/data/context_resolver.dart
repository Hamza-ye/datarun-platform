import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/data/projection_engine.dart';

/// Resolves `context.*` properties for expression evaluation (ADR-5 S8, IDR-018).
///
/// Per IDR-018 rule 4, `context.*` values are pre-resolved at form-open rather
/// than evaluated lazily. Missing/unresolvable values are emitted as `null`;
/// expression evaluation tolerates nulls in comparison operators per grammar.
///
/// Phase 3d.3 resolves the subset that is knowable from the device without
/// Phase 4 primitives (Trigger Engine, Pattern Registry):
///
///   context.actor.role             — from local_assignments (first active)
///   context.actor.scope_name       — from local_assignments (first active)
///   context.event_count            — count of existing events for subject
///   context.days_since_last_event  — days since newest event timestamp
///   context.subject_state          — null (requires Phase 4 Pattern Registry)
///   context.subject_pattern        — null (requires Phase 4 Pattern Registry)
///   context.activity_stage         — null (requires Phase 4 Trigger Engine)
class ContextResolver {
  final EventStore _eventStore;
  final ProjectionEngine _projection;

  ContextResolver(this._eventStore, this._projection);

  /// Returns a flat map of `context.*` properties suitable for merging into
  /// the expression evaluator's values map.
  ///
  /// [subjectId] is null for new-subject capture flows; in that case
  /// event_count=0 and days_since_last_event=null.
  Future<Map<String, dynamic>> resolve({
    String? subjectId,
    String? activityRef,
    DateTime? now,
  }) async {
    final clock = now ?? DateTime.now().toUtc();
    final result = <String, dynamic>{};

    // Actor — first active assignment (Phase 3d: single-actor device model)
    final assignments = await _eventStore.getActiveAssignments();
    if (assignments.isNotEmpty) {
      final a = assignments.first;
      result['context.actor.role'] = a['role'];
      // scope_name: best-effort label derived from assignment payload.
      // Prefer geo_scope (path), fall back to subject_list or activity_list.
      final scopeName = a['geo_scope'] as String? ??
          a['subject_list'] as String? ??
          a['activity_list'] as String?;
      result['context.actor.scope_name'] = scopeName;
    } else {
      result['context.actor.role'] = null;
      result['context.actor.scope_name'] = null;
    }

    // Subject-derived properties
    if (subjectId == null) {
      result['context.event_count'] = 0;
      result['context.days_since_last_event'] = null;
    } else {
      final events = await _projection.getSubjectDetail(subjectId);
      result['context.event_count'] = events.length;
      if (events.isEmpty) {
        result['context.days_since_last_event'] = null;
      } else {
        // getSubjectDetail returns DESC by timestamp
        final latest = DateTime.tryParse(events.first.timestamp);
        if (latest == null) {
          result['context.days_since_last_event'] = null;
        } else {
          final diff = clock.difference(latest.toUtc()).inDays;
          result['context.days_since_last_event'] = diff;
        }
      }
    }

    // Phase 4 placeholders — pre-resolved as null per IDR-018 rule 4
    result['context.subject_state'] = null;
    result['context.subject_pattern'] = null;
    result['context.activity_stage'] = null;

    return result;
  }
}
