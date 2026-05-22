import 'package:flutter_test/flutter_test.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';

void main() {
  group('ActivityActionAdvisory', () {
    final roleActions = ActivityRoleActions.fromRoles('monitoring', {
      'field_worker': ['capture'],
      'supervisor': ['capture', 'review'],
    });

    test('allows capture for current assignment role and activity', () {
      final decision = ActivityActionAdvisory.evaluate(
        roleActions: roleActions,
        activeAssignments: [
          {'role': 'field_worker', 'activity_list': 'monitoring'},
        ],
        activityRef: 'monitoring',
        action: ActivityAction.capture,
      );

      expect(decision.allowed, true);
      expect(decision.warning, isNull);
    });

    test('warns and blocks review when current role lacks that action', () {
      final decision = ActivityActionAdvisory.evaluate(
        roleActions: roleActions,
        activeAssignments: [
          {'role': 'field_worker', 'activity_list': 'monitoring'},
        ],
        activityRef: 'monitoring',
        action: ActivityAction.review,
      );

      expect(decision.allowed, false);
      expect(decision.warning, contains('does not allow review'));
    });

    test('activity-scoped assignments do not grant actions elsewhere', () {
      final decision = ActivityActionAdvisory.evaluate(
        roleActions: roleActions,
        activeAssignments: [
          {'role': 'supervisor', 'activity_list': 'other_activity'},
        ],
        activityRef: 'monitoring',
        action: ActivityAction.review,
      );

      expect(decision.allowed, false);
      expect(decision.warning, contains('No current assignment covers'));
    });

    test('unrestricted activity scope covers configured activity', () {
      final decision = ActivityActionAdvisory.evaluate(
        roleActions: roleActions,
        activeAssignments: [
          {'role': 'supervisor', 'activity_list': null},
        ],
        activityRef: 'monitoring',
        action: ActivityAction.review,
      );

      expect(decision.allowed, true);
    });
  });
}
