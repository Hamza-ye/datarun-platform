import 'package:datarun_mobile/data/config_store.dart';
import 'package:datarun_mobile/data/event_store.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/pattern_state.dart';

/// Canonical projection timestamp format: UTC with fixed six-digit microseconds.
String _formatProjectionTimestamp(DateTime value) {
  final utc = value.toUtc();
  String two(int v) => v.toString().padLeft(2, '0');
  String three(int v) => v.toString().padLeft(3, '0');
  return '${utc.year.toString().padLeft(4, '0')}-'
      '${two(utc.month)}-${two(utc.day)}T'
      '${two(utc.hour)}:${two(utc.minute)}:${two(utc.second)}.'
      '${three(utc.millisecond)}${three(utc.microsecond)}Z';
}

class PatternProjectionEngine {
  final EventStore _eventStore;
  final ConfigStore _configStore;

  PatternProjectionEngine(this._eventStore, this._configStore);

  Future<List<PatternState>> projectCurrent({DateTime? asOf}) async {
    final events = await _eventStore.getAll();
    return project(
      events: events,
      activityConfigs: _configStore.activityConfigs,
      patternDefinitions: _configStore.patternDefinitions,
      asOf: asOf ?? DateTime.now().toUtc(),
    );
  }

  List<PatternState> project({
    required List<Event> events,
    required Map<String, Map<String, dynamic>> activityConfigs,
    required Map<String, Map<String, dynamic>> patternDefinitions,
    required DateTime asOf,
  }) {
    final bindingsByActivity = _parseBindings(
      activityConfigs,
      patternDefinitions,
    );
    final excludedEventIds = _nonAcceptedFlaggedEventIds(events);
    final subjectAliases = _subjectAliases(events);
    final assignmentsById = <String, _AssignmentFact>{};
    final latestAssignmentsBySubjectActivity = <String, _AssignmentFact>{};
    final states = <String, _StateInstance>{};
    final ordered = [...events]..sort(_compareEvents);

    for (final event in ordered) {
      if (excludedEventIds.contains(event.id) || _isProjectionMetadata(event)) {
        continue;
      }
      if (event.type == 'assignment_changed') {
        _applyAssignmentEvent(
          event: event,
          bindingsByActivity: bindingsByActivity,
          states: states,
          subjectAliases: subjectAliases,
          assignmentsById: assignmentsById,
          latestAssignmentsBySubjectActivity:
              latestAssignmentsBySubjectActivity,
        );
        continue;
      }
      final activityRef = event.activityRef;
      if (activityRef == null) continue;
      final bindings = bindingsByActivity[activityRef] ?? const <_Binding>[];
      for (final binding in bindings) {
        if (binding.composition == 'subject') {
          _applySubjectEvent(
            event,
            binding,
            states,
            subjectAliases,
            latestAssignmentsBySubjectActivity,
          );
        } else if (binding.composition == 'event') {
          _applyEventEvent(event, binding, states);
        }
      }
    }

    final output = states.values.toList()
      ..sort((a, b) => a.key.compareTo(b.key));
    return output.map((s) => s.toPatternState(asOf)).toList();
  }

  Map<String, List<_Binding>> _parseBindings(
    Map<String, Map<String, dynamic>> activityConfigs,
    Map<String, Map<String, dynamic>> patternDefinitions,
  ) {
    final result = <String, List<_Binding>>{};
    for (final entry in activityConfigs.entries) {
      final pattern = entry.value['pattern'];
      if (pattern is! Map) continue;
      final bindings = <_Binding>[];
      final subject = pattern['subject'];
      if (subject is Map) {
        final parsed = _parseBinding(
          entry.key,
          Map<String, dynamic>.from(subject),
          'subject',
          patternDefinitions,
        );
        if (parsed != null) bindings.add(parsed);
      }
      final eventBindings = pattern['event'];
      if (eventBindings is List) {
        for (final raw in eventBindings) {
          if (raw is! Map) continue;
          final parsed = _parseBinding(
            entry.key,
            Map<String, dynamic>.from(raw),
            'event',
            patternDefinitions,
          );
          if (parsed != null) bindings.add(parsed);
        }
      }
      if (bindings.isNotEmpty) result[entry.key] = bindings;
    }
    return result;
  }

  _Binding? _parseBinding(
    String activityRef,
    Map<String, dynamic> binding,
    String expectedComposition,
    Map<String, Map<String, dynamic>> patternDefinitions,
  ) {
    final ref = binding['ref'];
    final composition = binding['composition'];
    if (ref is! String || composition is! String) return null;
    if (composition != expectedComposition) return null;
    final definition = patternDefinitions[ref];
    if (definition == null || definition['binding_enabled'] != true) {
      return null;
    }
    return _Binding(
      activityRef: activityRef,
      ref: ref,
      composition: composition,
      definition: definition,
      shapeRolesByRef: _reverseRoleMap(binding['shape_roles']),
      activationRolesByRef: _reverseRoleMap(binding['activation_roles']),
      parameters: binding['parameters'] is Map
          ? Map<String, dynamic>.from(binding['parameters'] as Map)
          : const {},
    );
  }

  Map<String, String> _reverseRoleMap(Object? roles) {
    final result = <String, String>{};
    if (roles is! Map) return result;
    for (final entry in roles.entries) {
      final values = entry.value;
      if (entry.key is! String || values is! List) continue;
      for (final value in values) {
        if (value is String) result[value] = entry.key as String;
      }
    }
    return result;
  }

  Set<String> _nonAcceptedFlaggedEventIds(List<Event> events) {
    final flagToResolver = <String, String>{};
    final flagsBySource = <String, Set<String>>{};
    final acceptedFlagIds = <String>{};
    for (final event in events) {
      if (_isIntegrityFlag(event)) {
        final sourceId = event.payload['source_event_id'];
        if (sourceId is String) {
          final resolver = _resolverKey(event.payload['designated_resolver']);
          if (resolver != null) flagToResolver[event.id] = resolver;
          flagsBySource.putIfAbsent(sourceId, () => <String>{}).add(event.id);
        }
      }
    }
    for (final event in events) {
      if (_isIntegrityResolution(event) &&
          event.payload['resolution'] == 'accepted') {
        final flagEventId = event.payload['flag_event_id'];
        if (flagEventId is String) {
          final resolver = flagToResolver[flagEventId];
          if (resolver != null && resolver == _resolverKey(event.actorRef)) {
            acceptedFlagIds.add(flagEventId);
          }
        }
      }
    }
    final excluded = <String>{};
    for (final entry in flagsBySource.entries) {
      if (!entry.value.every(acceptedFlagIds.contains)) {
        excluded.add(entry.key);
      }
    }
    return excluded;
  }

  String? _resolverKey(dynamic ref) {
    if (ref is! Map) return null;
    final type = ref['type'];
    final id = ref['id'];
    if (type is! String || id is! String) return null;
    return '$type:$id';
  }

  Map<String, String> _subjectAliases(List<Event> events) {
    final aliases = <String, String>{};
    final ordered = [...events]..sort(_compareEvents);
    for (final event in ordered) {
      if (!event.shapeRef.startsWith('subjects_merged/')) continue;
      final retiredId = event.payload['retired_id'];
      final survivingId = event.payload['surviving_id'];
      if (retiredId is! String || survivingId is! String) continue;
      for (final entry in aliases.entries.toList()) {
        if (entry.value == retiredId) aliases[entry.key] = survivingId;
      }
      aliases[retiredId] = survivingId;
    }
    return aliases;
  }

  void _applySubjectEvent(
    Event event,
    _Binding binding,
    Map<String, _StateInstance> states,
    Map<String, String> subjectAliases,
    Map<String, _AssignmentFact> latestAssignmentsBySubjectActivity,
  ) {
    final shapeRole = binding.shapeRole(event.shapeRef);
    if (shapeRole == null) return;
    final canonicalSubjectId = _canonicalSubjectId(
      event.subjectRef,
      subjectAliases,
    );
    final subjectRef = _canonicalSubjectRef(
      event.subjectRef,
      canonicalSubjectId,
    );
    final key = _subjectKey(subjectRef, binding);
    final current = states[key];
    final transition = _matchingTransition(
      event: event,
      binding: binding,
      current: current,
      shapeRole: shapeRole,
      activationRole: null,
    );
    if (transition == null) return;
    final next =
        current ??
        _StateInstance.subject(
          key: key,
          subjectRef: subjectRef,
          binding: binding,
        );
    if (current == null) {
      _applyInitialAssignment(
        next,
        canonicalSubjectId,
        binding,
        latestAssignmentsBySubjectActivity,
      );
    }
    _applyTransition(next, event, binding, transition, shapeRole);
    states[key] = next;
  }

  void _applyAssignmentEvent({
    required Event event,
    required Map<String, List<_Binding>> bindingsByActivity,
    required Map<String, _StateInstance> states,
    required Map<String, String> subjectAliases,
    required Map<String, _AssignmentFact> assignmentsById,
    required Map<String, _AssignmentFact> latestAssignmentsBySubjectActivity,
  }) {
    final assignmentId = event.subjectRef['id'];
    if (assignmentId is! String) return;
    if (event.shapeRef == 'assignment_created/v1') {
      final fact = _assignmentFact(event, assignmentId, subjectAliases);
      if (fact == null) return;
      assignmentsById[assignmentId] = fact;
      _applyAssignmentFact(
        event: event,
        fact: fact,
        ending: false,
        bindingsByActivity: bindingsByActivity,
        states: states,
        latestAssignmentsBySubjectActivity: latestAssignmentsBySubjectActivity,
      );
    } else if (event.shapeRef == 'assignment_ended/v1') {
      final fact = assignmentsById[assignmentId];
      if (fact == null) return;
      _applyAssignmentFact(
        event: event,
        fact: fact,
        ending: true,
        bindingsByActivity: bindingsByActivity,
        states: states,
        latestAssignmentsBySubjectActivity: latestAssignmentsBySubjectActivity,
      );
    }
  }

  _AssignmentFact? _assignmentFact(
    Event event,
    String assignmentId,
    Map<String, String> subjectAliases,
  ) {
    final targetActor = event.payload['target_actor'];
    final scope = event.payload['scope'];
    final subjectList = scope is Map ? scope['subject_list'] : null;
    if (targetActor is! Map || subjectList is! List || subjectList.isEmpty) {
      return null;
    }
    final subjectIds = <String>[];
    for (final subjectId in subjectList) {
      if (subjectId is String) {
        subjectIds.add(subjectAliases[subjectId] ?? subjectId);
      }
    }
    if (subjectIds.isEmpty) return null;

    final activityList = scope is Map ? scope['activity'] : null;
    final activityRefs = <String>{};
    if (activityList is List) {
      for (final activity in activityList) {
        if (activity is String) activityRefs.add(activity);
      }
    }
    return _AssignmentFact(
      assignmentId: assignmentId,
      targetActor: Map<String, dynamic>.from(targetActor),
      subjectIds: subjectIds,
      activityRefs: activityList is List ? activityRefs : null,
    );
  }

  void _applyAssignmentFact({
    required Event event,
    required _AssignmentFact fact,
    required bool ending,
    required Map<String, List<_Binding>> bindingsByActivity,
    required Map<String, _StateInstance> states,
    required Map<String, _AssignmentFact> latestAssignmentsBySubjectActivity,
  }) {
    for (final entry in bindingsByActivity.entries) {
      final activityRef = entry.key;
      if (!fact.appliesToActivity(activityRef)) continue;
      for (final binding in entry.value) {
        if (binding.composition != 'subject') continue;
        final shapeRole = binding.shapeRole(event.shapeRef);
        if (shapeRole != 'transfer') continue;
        for (final subjectId in fact.subjectIds) {
          final assignmentKey = _subjectActivityKey(subjectId, activityRef);
          if (ending) {
            final latest = latestAssignmentsBySubjectActivity[assignmentKey];
            if (latest?.assignmentId == fact.assignmentId) {
              latestAssignmentsBySubjectActivity.remove(assignmentKey);
            }
          } else {
            latestAssignmentsBySubjectActivity[assignmentKey] = fact;
          }
          final subjectRef = {'type': 'subject', 'id': subjectId};
          final stateKey = _subjectKey(subjectRef, binding);
          final current = states[stateKey];
          final transition = _matchingTransition(
            event: event,
            binding: binding,
            current: current,
            shapeRole: shapeRole,
            activationRole: null,
          );
          if (transition == null || current == null) continue;
          _applyTransition(current, event, binding, transition, shapeRole);
          if (ending) {
            _clearCurrentAssigneeIfMatches(current, fact);
          } else {
            _setCurrentAssignee(current, fact.targetActor);
          }
        }
      }
    }
  }

  void _applyEventEvent(
    Event event,
    _Binding binding,
    Map<String, _StateInstance> states,
  ) {
    final activationRole = binding.activationRole(event.shapeRef);
    final shapeRole = binding.shapeRole(event.shapeRef);
    var sourceEventId = activationRole != null
        ? event.id
        : _sourceEventId(event.payload);
    if (sourceEventId == null && shapeRole != null) sourceEventId = event.id;
    if (sourceEventId == null) return;

    final key = _eventKey(sourceEventId, binding);
    final current = states[key];
    final transition = _matchingTransition(
      event: event,
      binding: binding,
      current: current,
      shapeRole: shapeRole,
      activationRole: activationRole,
    );
    if (transition == null) return;
    final next =
        current ??
        _StateInstance.event(
          key: key,
          sourceEventId: sourceEventId,
          subjectRef: Map<String, dynamic>.from(event.subjectRef),
          binding: binding,
        );
    _applyTransition(next, event, binding, transition, shapeRole);
    states[key] = next;
  }

  Map<String, dynamic>? _matchingTransition({
    required Event event,
    required _Binding binding,
    required _StateInstance? current,
    required String? shapeRole,
    required String? activationRole,
  }) {
    final transitions = binding.definition['transitions'];
    if (transitions is! List) return null;
    for (final raw in transitions) {
      if (raw is! Map) continue;
      final transition = Map<String, dynamic>.from(raw);
      if (transition['event_type'] != event.type) continue;
      if (transition.containsKey('activation_role')) {
        if (activationRole == null ||
            activationRole != transition['activation_role']) {
          continue;
        }
      } else if (transition.containsKey('shape_role')) {
        if (shapeRole == null || shapeRole != transition['shape_role']) {
          continue;
        }
      } else {
        continue;
      }
      if (!_fromMatches(transition['from'], current)) continue;
      if (transition['requires_existing_instance'] == true && current == null) {
        continue;
      }
      if (!_branchMatches(
        transition['branch'],
        event.payload,
        current,
        binding,
      )) {
        continue;
      }
      return transition;
    }
    return null;
  }

  bool _fromMatches(Object? from, _StateInstance? current) {
    if (from == null) return current == null;
    final currentState = current?.currentState;
    if (currentState == null) return false;
    if (from is String) return from == 'any' || from == currentState;
    if (from is List) return from.contains(currentState);
    return false;
  }

  bool _branchMatches(
    Object? rawBranch,
    Map<String, dynamic> payload,
    _StateInstance? current,
    _Binding binding,
  ) {
    if (rawBranch is! Map) return true;
    final branch = Map<String, dynamic>.from(rawBranch);
    final field = branch['field'];
    if (field is String && branch.containsKey('equals')) {
      if (payload[field] != branch['equals']) return false;
    }
    final whenLevel = branch['when_level'];
    if (whenLevel is String) {
      final currentLevel = current?.intAttribute('level', -1) ?? -1;
      final levels = binding.parameters['levels'];
      final eventLevel = payload['level'];
      if (eventLevel is int && eventLevel != currentLevel) return false;
      if (whenLevel == 'less_than_levels') {
        return levels is int && currentLevel > 0 && currentLevel < levels;
      }
      if (whenLevel == 'equals_levels') {
        return levels is int && currentLevel > 0 && currentLevel == levels;
      }
    }
    return true;
  }

  void _applyTransition(
    _StateInstance state,
    Event event,
    _Binding binding,
    Map<String, dynamic> transition,
    String? shapeRole,
  ) {
    final previousState = state.currentState;
    final to = transition['to'] as String;
    final nextState = to == 'same' ? previousState : to;
    final nextAttributes = <String, Object?>{...state.attributes};
    final attributes = transition['attributes'];
    if (attributes is Map) {
      for (final entry in attributes.entries) {
        final value = entry.value;
        if (value == 'current_plus_1') {
          nextAttributes[entry.key as String] =
              state.intAttribute(entry.key as String, 0) + 1;
        } else if (value is int || value is String) {
          nextAttributes[entry.key as String] = value;
        }
      }
    }

    state.currentState = nextState;
    state.attributes
      ..clear()
      ..addAll(nextAttributes);
    final effect = transition['effect'] as String? ?? '';
    if (state.pendingSince == null || effect.startsWith('SC')) {
      state.pendingSince = DateTime.parse(event.timestamp).toUtc();
    }
    _applyPatternSpecific(state, event, binding, shapeRole);
  }

  void _applyPatternSpecific(
    _StateInstance state,
    Event event,
    _Binding binding,
    String? shapeRole,
  ) {
    switch (binding.ref) {
      case 'capture_with_review/v1':
        if (shapeRole == 'review_decision') {
          final decision = event.payload['decision'];
          if (decision is String) {
            state.patternSpecific['latest_review_outcome'] = decision;
          }
        }
        state.unsupportedPatternSpecific
          ..add('pending_review_count')
          ..add('accepted_count')
          ..add('returned_count');
      case 'multi_step_approval/v1':
        if (shapeRole == 'submission' && event.type == 'capture') {
          state.patternSpecific['submission_count'] =
              (state.patternSpecific['submission_count'] as int? ?? 0) + 1;
          state.patternSpecific['approval_chain'] = <Map<String, dynamic>>[];
        }
        if (shapeRole == 'level_decision' && event.type == 'review') {
          final chain =
              state.patternSpecific.putIfAbsent(
                    'approval_chain',
                    () => <Map<String, dynamic>>[],
                  )
                  as List;
          chain.add({
            'level': event.payload['level'] ?? state.intAttribute('level', 1),
            'actor_ref': Map<String, dynamic>.from(event.actorRef),
            'decision': event.payload['decision'],
            'timestamp': _formatProjectionTimestamp(
              DateTime.parse(event.timestamp).toUtc(),
            ),
          });
        }
        state.patternSpecific['current_level'] = state.currentState == 'pending'
            ? state.intAttribute('level', 1)
            : null;
        state.patternSpecific['time_at_current_level'] =
            state.currentState == 'pending';
      case 'transfer_with_acknowledgment/v1':
        if (shapeRole == 'dispatch') {
          state.attributes['dispatch_timestamp'] = DateTime.parse(
            event.timestamp,
          ).toUtc();
        }
        if (shapeRole == 'receipt') {
          final dispatchTimestamp = state.attributes['dispatch_timestamp'];
          if (dispatchTimestamp is DateTime) {
            final seconds = DateTime.parse(
              event.timestamp,
            ).toUtc().difference(dispatchTimestamp.toUtc()).inSeconds;
            state.patternSpecific['time_in_transit'] = seconds < 0
                ? 0
                : seconds;
          }
        }
        state.unsupportedPatternSpecific
          ..add('items_dispatched')
          ..add('items_received')
          ..add('discrepancy_summary');
      case 'ongoing_resolution/v1':
        if (shapeRole == 'interaction' && event.type == 'capture') {
          state.patternSpecific['last_interaction_date'] =
              _formatProjectionTimestamp(DateTime.parse(event.timestamp));
          state.patternSpecific['interaction_count'] =
              (state.patternSpecific['interaction_count'] as int? ?? 0) + 1;
        }
        if (shapeRole == 'referral' && event.type == 'capture') {
          state.patternSpecific['referral_count'] =
              (state.patternSpecific['referral_count'] as int? ?? 0) + 1;
        }
        if (shapeRole == 'reopening' && event.type == 'capture') {
          state.patternSpecific['reopen_count'] =
              (state.patternSpecific['reopen_count'] as int? ?? 0) + 1;
        }
        if (shapeRole == 'transfer' &&
            event.shapeRef == 'assignment_created/v1') {
          final targetActor = event.payload['target_actor'];
          if (targetActor is Map) {
            _setCurrentAssignee(state, Map<String, dynamic>.from(targetActor));
          }
        }
    }
  }

  void _applyInitialAssignment(
    _StateInstance state,
    String subjectId,
    _Binding binding,
    Map<String, _AssignmentFact> latestAssignmentsBySubjectActivity,
  ) {
    if (binding.ref != 'ongoing_resolution/v1') return;
    final fact =
        latestAssignmentsBySubjectActivity[_subjectActivityKey(
          subjectId,
          binding.activityRef,
        )];
    if (fact != null) _setCurrentAssignee(state, fact.targetActor);
  }

  void _setCurrentAssignee(
    _StateInstance state,
    Map<String, dynamic> actorRef,
  ) {
    state.patternSpecific['current_assignee'] = Map<String, dynamic>.from(
      actorRef,
    );
  }

  void _clearCurrentAssigneeIfMatches(
    _StateInstance state,
    _AssignmentFact fact,
  ) {
    final current = state.patternSpecific['current_assignee'];
    final currentId = current is Map ? current['id'] : null;
    final endedId = fact.targetActor['id'];
    if (currentId is String && currentId == endedId) {
      state.patternSpecific['current_assignee'] = null;
    }
  }

  int _compareEvents(Event a, Event b) {
    final watermarkA = a.syncWatermark ?? 1 << 62;
    final watermarkB = b.syncWatermark ?? 1 << 62;
    final watermarkCompare = watermarkA.compareTo(watermarkB);
    if (watermarkCompare != 0) return watermarkCompare;
    return DateTime.parse(a.timestamp).compareTo(DateTime.parse(b.timestamp));
  }

  bool _isProjectionMetadata(Event event) =>
      event.shapeRef.startsWith('conflict_detected/') ||
      event.shapeRef.startsWith('conflict_resolved/') ||
      event.shapeRef.startsWith('subjects_merged/') ||
      event.shapeRef.startsWith('subject_split/');

  bool _isIntegrityFlag(Event event) =>
      event.shapeRef.startsWith('conflict_detected/');

  bool _isIntegrityResolution(Event event) =>
      event.shapeRef.startsWith('conflict_resolved/');

  String _subjectKey(Map<String, dynamic> subjectRef, _Binding binding) =>
      'subject|${subjectRef['type']}|${subjectRef['id']}|'
      '${binding.activityRef}|${binding.ref}';

  String _eventKey(String sourceEventId, _Binding binding) =>
      'event|$sourceEventId|${binding.ref}';

  String _canonicalSubjectId(
    Map<String, dynamic> subjectRef,
    Map<String, String> subjectAliases,
  ) {
    final subjectId = subjectRef['id'];
    return subjectId is String ? subjectAliases[subjectId] ?? subjectId : '';
  }

  Map<String, dynamic> _canonicalSubjectRef(
    Map<String, dynamic> subjectRef,
    String canonicalSubjectId,
  ) {
    final copy = Map<String, dynamic>.from(subjectRef);
    copy['id'] = canonicalSubjectId;
    return copy;
  }

  String _subjectActivityKey(String subjectId, String activityRef) =>
      '$subjectId|$activityRef';

  String? _sourceEventId(Map<String, dynamic> payload) {
    final source = payload['source_event_id'];
    if (source is String) return source;
    final sourceRef = payload['source_event_ref'];
    if (sourceRef is String) return sourceRef;
    if (sourceRef is Map && sourceRef['id'] is String) {
      return sourceRef['id'] as String;
    }
    return null;
  }
}

class _Binding {
  final String activityRef;
  final String ref;
  final String composition;
  final Map<String, dynamic> definition;
  final Map<String, String> shapeRolesByRef;
  final Map<String, String> activationRolesByRef;
  final Map<String, dynamic> parameters;

  const _Binding({
    required this.activityRef,
    required this.ref,
    required this.composition,
    required this.definition,
    required this.shapeRolesByRef,
    required this.activationRolesByRef,
    required this.parameters,
  });

  String? shapeRole(String shapeRef) => shapeRolesByRef[shapeRef];

  String? activationRole(String shapeRef) => activationRolesByRef[shapeRef];
}

class _AssignmentFact {
  final String assignmentId;
  final Map<String, dynamic> targetActor;
  final List<String> subjectIds;
  final Set<String>? activityRefs;

  const _AssignmentFact({
    required this.assignmentId,
    required this.targetActor,
    required this.subjectIds,
    required this.activityRefs,
  });

  bool appliesToActivity(String activityRef) =>
      activityRefs == null || activityRefs!.contains(activityRef);
}

class _StateInstance {
  final String key;
  final String composition;
  final String? sourceEventId;
  final Map<String, dynamic> subjectRef;
  final _Binding binding;
  String? currentState;
  DateTime? pendingSince;
  final Map<String, Object?> attributes = {};
  final Map<String, dynamic> patternSpecific = {};
  final Set<String> unsupportedPatternSpecific = {};

  _StateInstance._({
    required this.key,
    required this.composition,
    required this.sourceEventId,
    required this.subjectRef,
    required this.binding,
  });

  factory _StateInstance.subject({
    required String key,
    required Map<String, dynamic> subjectRef,
    required _Binding binding,
  }) => _StateInstance._(
    key: key,
    composition: 'subject',
    sourceEventId: null,
    subjectRef: subjectRef,
    binding: binding,
  );

  factory _StateInstance.event({
    required String key,
    required String sourceEventId,
    required Map<String, dynamic> subjectRef,
    required _Binding binding,
  }) => _StateInstance._(
    key: key,
    composition: 'event',
    sourceEventId: sourceEventId,
    subjectRef: subjectRef,
    binding: binding,
  );

  int intAttribute(String name, int fallback) {
    final value = attributes[name];
    return value is num ? value.toInt() : fallback;
  }

  PatternState toPatternState(DateTime asOf) {
    final pending = pendingSince!.toUtc();
    final specific = <String, dynamic>{};
    for (final entry in patternSpecific.entries) {
      if (entry.key == 'time_at_current_level' && entry.value is bool) {
        if (entry.value == true) {
          specific[entry.key] = _nonNegativeSeconds(
            asOf.toUtc().difference(pending),
          );
        }
      } else {
        specific[entry.key] = entry.value;
      }
    }
    return PatternState(
      composition: composition,
      stateKey: composition == 'subject'
          ? {
              'subject_ref': subjectRef,
              'activity_ref': binding.activityRef,
              'binding_ref': binding.ref,
            }
          : {'source_event_id': sourceEventId, 'binding_ref': binding.ref},
      currentState: currentState!,
      pendingSince: _formatProjectionTimestamp(pending),
      timeInState: _nonNegativeSeconds(asOf.toUtc().difference(pending)),
      patternSpecific: specific,
      unsupportedPatternSpecificFields: unsupportedPatternSpecific.toList()
        ..sort(),
    );
  }

  int _nonNegativeSeconds(Duration duration) =>
      duration.inSeconds < 0 ? 0 : duration.inSeconds;
}
