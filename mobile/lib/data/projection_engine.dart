import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/subject_summary.dart';

class ProjectionEngine {
  final EventStore _eventStore;

  ProjectionEngine(this._eventStore);

  /// Derive subject list from all local events. Full replay.
  Future<List<SubjectSummary>> getSubjectList() async {
    final events = await _eventStore.getAll();
    final bySubject = <String, List<Event>>{};

    for (final e in events) {
      final sid = e.subjectRef['id']!;
      bySubject.putIfAbsent(sid, () => []).add(e);
    }

    return bySubject.entries.map((entry) {
      final subjectEvents = entry.value;
      // Events are already sorted DESC by timestamp from EventStore
      final latest = subjectEvents.first;
      // Try to extract name from earliest capture payload
      final firstCapture = subjectEvents.reversed
          .where((e) => e.type == 'capture')
          .firstOrNull;
      final name = firstCapture?.payload['name'] as String?;

      return SubjectSummary(
        subjectId: entry.key,
        subjectType: latest.subjectRef['type']!,
        name: name,
        latestTimestamp: latest.timestamp,
        captureCount: subjectEvents.where((e) => e.type == 'capture').length,
      );
    }).toList()
      ..sort((a, b) => b.latestTimestamp.compareTo(a.latestTimestamp));
  }

  /// Full event timeline for one subject.
  Future<List<Event>> getSubjectDetail(String subjectId) async {
    return _eventStore.getBySubject(subjectId);
  }
}
