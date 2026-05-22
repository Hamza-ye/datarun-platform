/// Phase 4 activity work actions from IDR-021/IDR-023.
///
/// This intentionally excludes `assignment_changed`; assignment lifecycle
/// authorization is outside activity role-action policy.
enum ActivityAction {
  capture('capture'),
  review('review'),
  alert('alert'),
  taskCreated('task_created'),
  taskCompleted('task_completed');

  const ActivityAction(this.type);

  final String type;

  static ActivityAction? fromType(String type) {
    for (final action in ActivityAction.values) {
      if (action.type == type) return action;
    }
    return null;
  }
}

class ActivityRoleActions {
  final String activityRef;
  final Map<String, Set<ActivityAction>> _actionsByRole;

  ActivityRoleActions({
    required this.activityRef,
    required Map<String, Set<ActivityAction>> actionsByRole,
  }) : _actionsByRole = Map.unmodifiable({
         for (final entry in actionsByRole.entries)
           entry.key: Set.unmodifiable(entry.value),
       });

  factory ActivityRoleActions.empty(String activityRef) =>
      ActivityRoleActions(activityRef: activityRef, actionsByRole: {});

  factory ActivityRoleActions.fromRoles(String activityRef, Object? rolesRaw) {
    if (rolesRaw is! Map) return ActivityRoleActions.empty(activityRef);

    final parsed = <String, Set<ActivityAction>>{};
    for (final entry in rolesRaw.entries) {
      final role = entry.key;
      final actionsRaw = entry.value;
      if (role is! String || role.trim().isEmpty || actionsRaw is! Iterable) {
        continue;
      }

      final actions = <ActivityAction>{};
      for (final rawAction in actionsRaw) {
        if (rawAction is! String) continue;
        final action = ActivityAction.fromType(rawAction);
        if (action != null) actions.add(action);
      }
      if (actions.isNotEmpty) {
        parsed[role] = actions;
      }
    }

    return ActivityRoleActions(activityRef: activityRef, actionsByRole: parsed);
  }

  Set<ActivityAction> permittedActionsForRole(String role) =>
      _actionsByRole[role] ?? const {};

  Set<String> permittedActionTypesForRole(String role) =>
      permittedActionsForRole(role).map((action) => action.type).toSet();

  bool rolePermits(String? role, ActivityAction action) =>
      role != null && (_actionsByRole[role]?.contains(action) ?? false);
}

class ActivityActionDecision {
  final bool allowed;
  final String? warning;

  const ActivityActionDecision._({required this.allowed, this.warning});

  const ActivityActionDecision.allowed() : this._(allowed: true, warning: null);

  const ActivityActionDecision.blocked(String warning)
    : this._(allowed: false, warning: warning);
}

class ActivityActionAdvisory {
  static ActivityActionDecision evaluate({
    required ActivityRoleActions roleActions,
    required Iterable<Map<String, dynamic>> activeAssignments,
    required String activityRef,
    required ActivityAction action,
  }) {
    final coveringAssignments = activeAssignments
        .where(
          (assignment) => assignmentCoversActivity(assignment, activityRef),
        )
        .toList();

    if (coveringAssignments.isEmpty) {
      return ActivityActionDecision.blocked(
        'No current assignment covers $activityRef for ${action.type}.',
      );
    }

    for (final assignment in coveringAssignments) {
      final role = assignment['role'] as String?;
      if (roleActions.rolePermits(role, action)) {
        return const ActivityActionDecision.allowed();
      }
    }

    return ActivityActionDecision.blocked(
      'Your current role does not allow ${action.type} in $activityRef.',
    );
  }

  static bool assignmentCoversActivity(
    Map<String, dynamic> assignment,
    String activityRef,
  ) {
    final raw = assignment['activity_list'];
    if (raw == null) return true;
    if (raw is String) {
      if (raw.trim().isEmpty) return false;
      return raw
          .split(',')
          .map((activity) => activity.trim())
          .contains(activityRef);
    }
    if (raw is Iterable) {
      return raw.whereType<String>().contains(activityRef);
    }
    return false;
  }
}
