/// Pure-function expression evaluator for IDR-018 JSON AST expressions.
/// Produces IDENTICAL results to the Java server evaluator.
/// No DB access, no async, no side effects.
class ExpressionEvaluator {
  static const _namespaces = ['payload.', 'entity.', 'context.', 'event.'];

  /// Evaluate a condition expression → boolean.
  static bool evaluateCondition(
      Map<String, dynamic> expression, Map<String, dynamic> values) {
    if (expression.containsKey('and')) {
      return _evaluateAnd(expression['and'] as List, values);
    }
    if (expression.containsKey('or')) {
      return _evaluateOr(expression['or'] as List, values);
    }
    if (expression.containsKey('not')) {
      final inner = expression['not'];
      if (inner is Map<String, dynamic>) {
        return !evaluateCondition(inner, values);
      }
      return false;
    }
    // Bare comparison node
    return _evaluateComparison(expression, values);
  }

  /// Evaluate a value expression → dynamic (for ref nodes and comparison-as-value).
  static dynamic evaluateValue(
      Map<String, dynamic> expression, Map<String, dynamic> values) {
    if (expression.containsKey('ref')) {
      final ref = expression['ref'] as String;
      return _resolveOperand(ref, values);
    }
    // Comparison as value → boolean
    return evaluateCondition(expression, values);
  }

  static bool _evaluateAnd(List operands, Map<String, dynamic> values) {
    for (final op in operands) {
      if (op is Map<String, dynamic>) {
        if (!evaluateCondition(op, values)) return false;
      } else {
        return false;
      }
    }
    return true;
  }

  static bool _evaluateOr(List operands, Map<String, dynamic> values) {
    for (final op in operands) {
      if (op is Map<String, dynamic>) {
        if (evaluateCondition(op, values)) return true;
      }
    }
    return false;
  }

  static bool _evaluateComparison(
      Map<String, dynamic> node, Map<String, dynamic> values) {
    final entry = node.entries.first;
    final operator = entry.key;
    final operands = entry.value;

    if (operator == 'not_null') {
      final args = operands as List;
      if (args.isEmpty) return false;
      final resolved = _resolveOperand(args[0], values);
      return resolved != null;
    }

    if (operator == 'in') {
      return _evaluateIn(operands as List, values);
    }

    // Binary comparison: [left, right]
    final args = operands as List;
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
        return _compare(left, right) > 0;
      case 'gte':
        return _compare(left, right) >= 0;
      case 'lt':
        return _compare(left, right) < 0;
      case 'lte':
        return _compare(left, right) <= 0;
      default:
        return false;
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

  static int _compare(dynamic a, dynamic b) {
    final numA = _toNum(a);
    final numB = _toNum(b);
    if (numA != null && numB != null) {
      return numA.compareTo(numB);
    }
    // If coercion fails, treat as incomparable (returns 0 which makes gt/lt false)
    // But actually for failed coercion we should make comparisons false
    // Returning 0 makes gt→false, lt→false, gte→true, lte→true
    // That's wrong. We need to signal failure.
    // Since null handling already covers null operands, reaching here means
    // both are non-null but non-numeric. Use string comparison as fallback.
    final strA = a.toString();
    final strB = b.toString();
    return strA.compareTo(strB);
  }

  static num? _toNum(dynamic value) {
    if (value is num) return value;
    if (value is String) return num.tryParse(value);
    return null;
  }
}
