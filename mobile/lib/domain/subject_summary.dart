class SubjectSummary {
  final String subjectId;
  final String subjectType;
  final String? name; // extracted from first capture payload
  final String latestTimestamp;
  final int captureCount;

  SubjectSummary({
    required this.subjectId,
    required this.subjectType,
    this.name,
    required this.latestTimestamp,
    required this.captureCount,
  });
}
