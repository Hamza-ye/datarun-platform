import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/domain/expression_evaluator.dart';
import 'package:datarun_mobile/domain/field_asset_lookup.dart';
import 'package:datarun_mobile/presentation/widgets/widget_mapper.dart';

class CaptureSaveResult {
  final String eventId;
  final String subjectId;

  const CaptureSaveResult({required this.eventId, required this.subjectId});
}

/// S3: Form — shape-driven event creation.
class FormScreen extends StatefulWidget {
  final String? subjectId; // null = new subject
  final String shapeRef;
  final String? activityRef; // null = no expression evaluation
  final Map<String, dynamic> initialValues;
  final bool isCorrection;

  const FormScreen({
    super.key,
    required this.subjectId,
    required this.shapeRef,
    this.activityRef,
    this.initialValues = const {},
    this.isCorrection = false,
  });

  @override
  State<FormScreen> createState() => _FormScreenState();
}

class _FormScreenState extends State<FormScreen> {
  final _formKey = GlobalKey<FormState>();
  ShapeDefinition? _shape;
  final Map<String, dynamic> _values = {};
  final Map<String, dynamic> _context = {};
  final Set<String> _hiddenFields = {};
  final Map<String, String> _warnings = {};
  Map<String, dynamic> _correctionBaseline = const {};
  String? _actionWarning;
  bool _loading = true;
  bool _dirty = false;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _loadShape();
  }

  void _loadShape() async {
    final state = context.read<AppState>();
    // Promote pending config at form-open under the accepted two-slot model.
    await state.configStore.promotePending();
    final shape = state.configStore.getShape(widget.shapeRef);
    // Pre-resolve accepted context.* properties.
    final ctx = await state.contextResolver.resolve(
      subjectId: widget.subjectId,
      activityRef: widget.activityRef,
    );
    final decision = await _currentCaptureDecision(state);
    _values.clear();
    if (shape != null) {
      for (final field in shape.activeFields) {
        if (!widget.initialValues.containsKey(field.name)) continue;
        final value = widget.initialValues[field.name];
        _values[field.name] = field.type == 'multi_select' && value is List
            ? value.whereType<String>().toList()
            : value;
      }
      final candidateEvidence = widget.initialValues[assetCandidateEvidenceKey];
      if (candidateEvidence != null) {
        _values[assetCandidateEvidenceKey] = candidateEvidence;
      }
    }
    _context
      ..clear()
      ..addAll(ctx);
    if (!mounted) return;
    setState(() {
      _shape = shape;
      _actionWarning = decision.warning;
      _loading = false;
    });
    if (shape != null) {
      _applyDefaults();
      if (widget.isCorrection) {
        _correctionBaseline = _effectiveValues(_values);
      }
      _evaluateExpressions();
    }
  }

  /// Build the values map for expression evaluation.
  /// Merges payload.* values with pre-resolved context.* properties.
  Map<String, dynamic> _buildValuesMap() {
    final map = <String, dynamic>{};
    for (final entry in _values.entries) {
      map['payload.${entry.key}'] = entry.value;
    }
    map.addAll(_context);
    return map;
  }

  /// Apply default expressions to fields that have no value yet.
  void _applyDefaults() {
    if (widget.activityRef == null || _shape == null) return;
    final state = context.read<AppState>();
    final valuesMap = _buildValuesMap();

    for (final field in _shape!.activeFields) {
      if (_values[field.name] != null) continue;
      final expr = state.configStore.getDefaultExpression(
        widget.activityRef!,
        widget.shapeRef,
        field.name,
      );
      if (expr == null) continue;
      final value = ExpressionEvaluator.evaluateValue(expr, valuesMap);
      if (value != null) {
        _values[field.name] = value;
      }
    }
  }

  /// Evaluate show_conditions and warnings for all fields.
  void _evaluateExpressions() {
    if (widget.activityRef == null || _shape == null) return;
    final state = context.read<AppState>();
    final valuesMap = _buildValuesMap();
    final hidden = <String>{};
    final warnings = <String, String>{};

    for (final field in _shape!.activeFields) {
      // Show condition
      final showExpr = state.configStore.getShowCondition(
        widget.activityRef!,
        widget.shapeRef,
        field.name,
      );
      if (showExpr != null) {
        final visible = ExpressionEvaluator.evaluateCondition(
          showExpr,
          valuesMap,
        );
        if (!visible) {
          hidden.add(field.name);
        }
      }

      // Warning
      final warnExpr = state.configStore.getWarningExpression(
        widget.activityRef!,
        widget.shapeRef,
        field.name,
      );
      if (warnExpr != null) {
        final triggered = ExpressionEvaluator.evaluateCondition(
          warnExpr,
          valuesMap,
        );
        if (triggered) {
          final msg = state.configStore.getWarningMessage(
            widget.activityRef!,
            widget.shapeRef,
            field.name,
          );
          if (msg != null) {
            warnings[field.name] = msg;
          }
        }
      }
    }

    _hiddenFields
      ..clear()
      ..addAll(hidden);
    _warnings
      ..clear()
      ..addAll(warnings);
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: !_dirty,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop && _dirty) {
          _showDiscardDialog(context);
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            widget.isCorrection
                ? 'Add correction'
                : (_shape?.name ?? 'Loading...'),
          ),
          actions: [
            TextButton(
              onPressed: _saving ? null : _save,
              child: _saving
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : Text(widget.isCorrection ? 'Save correction' : 'Save'),
            ),
          ],
        ),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _shape == null
            ? const Center(child: Text('Shape not found in config'))
            : Form(
                key: _formKey,
                child: ListView(
                  children: [
                    if (widget.isCorrection)
                      const Padding(
                        padding: EdgeInsets.fromLTRB(16, 16, 16, 8),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Icon(Icons.history),
                            SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                'Saving creates a new record. The original stays in history.',
                              ),
                            ),
                          ],
                        ),
                      ),
                    if (_actionWarning != null)
                      Padding(
                        padding: const EdgeInsets.all(16),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Icon(
                              Icons.warning_amber,
                              color: Theme.of(context).colorScheme.error,
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                _actionWarning!,
                                style: TextStyle(
                                  color: Theme.of(context).colorScheme.error,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ..._shape!.activeFields
                        .where(
                          (field) =>
                              !_hiddenFields.contains(field.name) &&
                              field.name != _shape!.subjectBinding,
                        )
                        .map((field) {
                          return WidgetMapper.build(
                            field,
                            _values[field.name],
                            (value) {
                              setState(() {
                                _values[field.name] = value;
                                _dirty = widget.isCorrection
                                    ? _hasCorrectionChanges
                                    : true;
                                _evaluateExpressions();
                              });
                            },
                            warningMessage: _warnings[field.name],
                          );
                        }),
                  ],
                ),
              ),
      ),
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;

    if (widget.isCorrection && !_hasCorrectionChanges) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Change at least one field before saving a correction.',
          ),
        ),
      );
      return;
    }

    setState(() => _saving = true);

    final state = context.read<AppState>();
    final decision = await _currentCaptureDecision(state);
    if (decision.warning != null) {
      if (!mounted) return;
      setState(() {
        _actionWarning = decision.warning;
      });
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(decision.warning!)));
    }

    // Clean nulls from payload
    final payload = Map<String, dynamic>.from(_values)
      ..removeWhere((_, v) => v == null);

    final event = await state.eventAssembler.assemble(
      subjectId: widget.subjectId,
      shapeRef: widget.shapeRef,
      payload: payload,
      activityRef: widget.activityRef,
    );

    if (mounted) {
      setState(() {
        _dirty = false;
        _saving = false;
      });
      Navigator.pop(
        context,
        CaptureSaveResult(
          eventId: event.id,
          subjectId: event.subjectRef['id']!,
        ),
      );
    }
  }

  bool get _hasCorrectionChanges =>
      !_sameValues(_effectiveValues(_values), _correctionBaseline);

  Map<String, dynamic> _effectiveValues(Map<String, dynamic> values) {
    return {
      for (final entry in values.entries)
        if (entry.value != null) entry.key: _snapshotValue(entry.value),
    };
  }

  dynamic _snapshotValue(dynamic value) {
    if (value is List) {
      return value.map(_snapshotValue).toList(growable: false);
    }
    if (value is Map) {
      return {
        for (final entry in value.entries)
          entry.key: _snapshotValue(entry.value),
      };
    }
    return value;
  }

  bool _sameValues(Map<String, dynamic> left, Map<String, dynamic> right) {
    if (left.length != right.length) return false;
    for (final entry in left.entries) {
      if (!right.containsKey(entry.key)) return false;
      if (!_sameValue(entry.value, right[entry.key])) return false;
    }
    return true;
  }

  bool _sameValue(dynamic left, dynamic right) {
    if (left is List && right is List) {
      if (left.length != right.length) return false;
      for (var i = 0; i < left.length; i++) {
        if (!_sameValue(left[i], right[i])) return false;
      }
      return true;
    }
    if (left is Map && right is Map) {
      if (left.length != right.length) return false;
      for (final entry in left.entries) {
        if (!right.containsKey(entry.key)) return false;
        if (!_sameValue(entry.value, right[entry.key])) return false;
      }
      return true;
    }
    return left == right;
  }

  Future<ActivityActionDecision> _currentCaptureDecision(AppState state) async {
    final activityRef = widget.activityRef;
    if (activityRef == null) {
      return const ActivityActionDecision.permitted();
    }
    final assignments = await state.eventStore.getActiveAssignments();
    return state.configStore.evaluateActivityAction(
      activityRef: activityRef,
      action: ActivityAction.capture,
      activeAssignments: assignments,
    );
  }

  void _showDiscardDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Discard changes?'),
        content: const Text('You have unsaved data. Discard it?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Keep editing'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(ctx); // close dialog
              Navigator.pop(context); // pop form
            },
            child: const Text('Discard'),
          ),
        ],
      ),
    );
  }
}
