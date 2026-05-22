enum FlagSeverity {
  blocking('blocking'),
  informational('informational');

  const FlagSeverity(this.value);

  final String value;

  static FlagSeverity? fromValue(String value) {
    for (final severity in FlagSeverity.values) {
      if (severity.value == value) return severity;
    }
    return null;
  }
}

class FlagSeverityCatalog {
  static const Map<String, FlagSeverity> defaults = {
    'concurrent_state_change': FlagSeverity.blocking,
    'stale_reference': FlagSeverity.informational,
    'identity_conflict': FlagSeverity.blocking,
    'scope_violation': FlagSeverity.blocking,
    'temporal_authority_expired': FlagSeverity.informational,
    'role_stale': FlagSeverity.blocking,
    'domain_uniqueness_violation': FlagSeverity.blocking,
    'transition_violation': FlagSeverity.informational,
  };

  static Map<String, FlagSeverity> parseOverrides(Object? raw) {
    if (raw is! Map) return {};

    final parsed = <String, FlagSeverity>{};
    for (final entry in raw.entries) {
      final category = entry.key;
      final severity = entry.value;
      if (category is! String || severity is! String) continue;
      if (!defaults.containsKey(category)) continue;

      final parsedSeverity = FlagSeverity.fromValue(severity);
      if (parsedSeverity != null) {
        parsed[category] = parsedSeverity;
      }
    }
    return parsed;
  }

  static FlagSeverity? effectiveSeverity(
    String category,
    Map<String, FlagSeverity> overrides,
  ) => overrides[category] ?? defaults[category];
}
