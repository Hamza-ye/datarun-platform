/// Pure-function expression evaluator for IDR-018 JSON AST expressions.
/// Produces IDENTICAL results to the Java server evaluator.
/// No DB access, no async, no side effects.
class ExpressionEvaluator {
  static const _namespaces = ['payload.', 'entity.', 'context.', 'event.'];

  /// Evaluate a condition expression → boolean.
  static bool evaluateCondition(
    Map<String, dynamic> expression,
    Map<String, dynamic> values,
  ) {
    return _evaluateCondition(expression, values, allowLogical: true);
  }

  static bool _evaluateCondition(
    Map<String, dynamic> expression,
    Map<String, dynamic> values, {
    required bool allowLogical,
  }) {
    if (expression.containsKey('and')) {
      if (!allowLogical) return false;
      final operands = expression['and'];
      if (operands is! List) return false;
      return _evaluateAnd(operands, values);
    }
    if (expression.containsKey('or')) {
      if (!allowLogical) return false;
      final operands = expression['or'];
      if (operands is! List) return false;
      return _evaluateOr(operands, values);
    }
    if (expression.containsKey('not')) {
      if (!allowLogical) return false;
      final inner = expression['not'];
      if (inner is Map<String, dynamic>) {
        final result = _evaluateComparison(inner, values);
        return result == null ? false : !result;
      }
      return false;
    }
    // Bare comparison node
    return _evaluateComparison(expression, values) ?? false;
  }

  /// Evaluate a value expression → dynamic (for ref nodes and comparison-as-value).
  static dynamic evaluateValue(
    Map<String, dynamic> expression,
    Map<String, dynamic> values,
  ) {
    if (expression.containsKey('ref')) {
      final ref = expression['ref'];
      if (ref is! String || !_isReference(ref)) return null;
      return _resolveOperand(ref, values);
    }
    // Comparison as value → boolean
    return evaluateCondition(expression, values);
  }

  static bool _evaluateAnd(List operands, Map<String, dynamic> values) {
    for (final op in operands) {
      if (op is Map<String, dynamic>) {
        if ((_evaluateComparison(op, values) ?? false) == false) return false;
      } else {
        return false;
      }
    }
    return true;
  }

  static bool _evaluateOr(List operands, Map<String, dynamic> values) {
    for (final op in operands) {
      if (op is Map<String, dynamic>) {
        if (_evaluateComparison(op, values) ?? false) return true;
      }
    }
    return false;
  }

  static bool? _evaluateComparison(
    Map<String, dynamic> node,
    Map<String, dynamic> values,
  ) {
    if (node.length != 1) return null;
    final entry = node.entries.first;
    final operator = entry.key;
    final operands = entry.value;

    if (operator == 'not_null') {
      if (operands is! List) return null;
      final args = operands;
      if (args.isEmpty) return false;
      final resolved = _resolveOperand(args[0], values);
      return resolved != null;
    }

    if (operator == 'in') {
      if (operands is! List) return null;
      return _evaluateIn(operands, values);
    }

    // Binary comparison: [left, right]
    if (operands is! List) return null;
    final args = operands;
    if (args.length < 2) return false;
    final left = _resolveOperand(args[0], values);
    final right = _resolveOperand(args[1], values);

    // Null handling: any null → false
    if (left == null || right == null) return false;

    switch (operator) {
      case 'eq':
        return _equals(left, right);
      case 'neq':
        return !_equals(left, right);
      case 'gt':
        final comparison = _compare(left, right);
        return comparison != null && comparison > 0;
      case 'gte':
        final comparison = _compare(left, right);
        return comparison != null && comparison >= 0;
      case 'lt':
        final comparison = _compare(left, right);
        return comparison != null && comparison < 0;
      case 'lte':
        final comparison = _compare(left, right);
        return comparison != null && comparison <= 0;
      default:
        return null;
    }
  }

  static bool _evaluateIn(List operands, Map<String, dynamic> values) {
    if (operands.length < 2) return false;
    final needle = _resolveOperand(operands[0], values);
    if (needle == null) return false;

    final haystackRaw = operands[1];
    List? haystack;

    if (haystackRaw is List) {
      // Literal array
      haystack = haystackRaw;
    } else if (haystackRaw is String && _isReference(haystackRaw)) {
      // Field reference that resolves to a List
      final resolved = _resolveOperand(haystackRaw, values);
      if (resolved is List) {
        haystack = resolved;
      } else {
        return false;
      }
    } else {
      return false;
    }

    // Check membership with type coercion
    for (final item in haystack) {
      if (_equals(needle, item)) return true;
    }
    return false;
  }

  static dynamic _resolveOperand(dynamic operand, Map<String, dynamic> values) {
    if (operand is String && _isReference(operand)) {
      return values[operand];
    }
    // Literal value
    return operand;
  }

  static bool _isReference(String value) {
    for (final ns in _namespaces) {
      if (value.startsWith(ns)) return true;
    }
    return false;
  }

  static bool _equals(dynamic a, dynamic b) {
    if (a == b) return true;
    // Type coercion: string ↔ number
    final numA = _toNum(a);
    final numB = _toNum(b);
    if (numA != null && numB != null) {
      return numA == numB;
    }
    // String comparison after coercion attempt
    if (a is num && b is String) {
      return a.toString() == b;
    }
    if (a is String && b is num) {
      return a == b.toString();
    }
    return false;
  }

  static int? _compare(dynamic a, dynamic b) {
    final numA = _toNum(a);
    final numB = _toNum(b);
    if (numA != null && numB != null) {
      return numA.compareTo(numB);
    }
    return null;
  }

  static num? _toNum(dynamic value) {
    if (value is num) return value;
    if (value is String) return num.tryParse(value);
    return null;
  }
}
