class PatternState {
  final String composition;
  final Map<String, dynamic> stateKey;
  final String currentState;
  final String pendingSince;
  final int timeInState;
  final Map<String, dynamic> patternSpecific;
  final List<String> unsupportedPatternSpecificFields;

  const PatternState({
    required this.composition,
    required this.stateKey,
    required this.currentState,
    required this.pendingSince,
    required this.timeInState,
    required this.patternSpecific,
    required this.unsupportedPatternSpecificFields,
  });

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{
      'composition': composition,
      'state_key': stateKey,
      'current_state': currentState,
      'pending_since': pendingSince,
      'time_in_state': timeInState,
      'pattern_specific': patternSpecific,
    };
    if (unsupportedPatternSpecificFields.isNotEmpty) {
      json['unsupported_pattern_specific_fields'] =
          unsupportedPatternSpecificFields;
    }
    return json;
  }
}
