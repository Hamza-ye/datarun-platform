import 'dart:convert';
import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:datarun_mobile/domain/expression_evaluator.dart';

void main() {
  late List<dynamic> cases;

  setUpAll(() {
    final fixtureJson = File(
            '${Directory.current.path}/../contracts/fixtures/expression-evaluation.json')
        .readAsStringSync();
    final parsed = jsonDecode(fixtureJson) as Map<String, dynamic>;
    cases = parsed['cases'] as List;
  });

  group('ExpressionEvaluator — shared fixtures', () {
    test('all fixture cases loaded', () {
      expect(cases.length, 50);
    });

    test('fixture cases produce correct results', () {
      for (final c in cases) {
        final id = c['id'] as String;
        final expression = c['expression'] as Map<String, dynamic>;
        final values = Map<String, dynamic>.from(c['values'] as Map);

        if (c.containsKey('expected_value')) {
          // Value expression test
          final expectedValue = c['expected_value'];
          final result = ExpressionEvaluator.evaluateValue(expression, values);
          expect(result, equals(expectedValue),
              reason: 'Fixture $id: expected value $expectedValue, got $result');
        } else {
          // Condition expression test
          final expected = c['expected'] as bool;
          final result =
              ExpressionEvaluator.evaluateCondition(expression, values);
          expect(result, equals(expected),
              reason: 'Fixture $id: expected $expected, got $result');
        }
      }
    });
  });

  group('ExpressionEvaluator — individual operators', () {
    test('eq with matching strings', () {
      expect(
          ExpressionEvaluator.evaluateCondition(
              {'eq': ['payload.x', 'hello']}, {'payload.x': 'hello'}),
          isTrue);
    });

    test('neq with different values', () {
      expect(
          ExpressionEvaluator.evaluateCondition(
              {'neq': ['payload.x', 'a']}, {'payload.x': 'b'}),
          isTrue);
    });

    test('not_null with present value', () {
      expect(
          ExpressionEvaluator.evaluateCondition(
              {'not_null': ['payload.x']}, {'payload.x': 42}),
          isTrue);
    });

    test('not_null with absent value', () {
      expect(
          ExpressionEvaluator.evaluateCondition(
              {'not_null': ['payload.x']}, {}),
          isFalse);
    });

    test('in with literal array', () {
      expect(
          ExpressionEvaluator.evaluateCondition(
              {'in': ['payload.s', ['a', 'b', 'c']]}, {'payload.s': 'b'}),
          isTrue);
    });

    test('ref returns resolved value', () {
      expect(
          ExpressionEvaluator.evaluateValue(
              {'ref': 'context.actor.scope_name'},
              {'context.actor.scope_name': 'Test Scope'}),
          equals('Test Scope'));
    });

    test('ref with unresolved returns null', () {
      expect(
          ExpressionEvaluator.evaluateValue(
              {'ref': 'context.missing'}, {}),
          isNull);
    });
  });
}
