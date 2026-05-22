import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/domain/activity_role_actions.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/domain/expression_evaluator.dart';
import 'package:datarun_mobile/presentation/widgets/widget_mapper.dart';

/// S3: Form — shape-driven event creation.
class FormScreen extends StatefulWidget {
  final String? subjectId; // null = new subject
  final String shapeRef;
  final String? activityRef; // null = no expression evaluation

  const FormScreen({
    super.key,
    required this.subjectId,
    required this.shapeRef,
    this.activityRef,
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
  String? _actionWarning;
  bool _loading = true;
  bool _dirty = false;
  bool _saving = false;
  bool _captureAllowed = true;

  @override
  void initState() {
    super.initState();
    _loadShape();
  }

  void _loadShape() async {
    final state = context.read<AppState>();
    // Promote pending config at form-open (IDR-019 two-slot model)
    await state.configStore.promotePending();
    final shape = state.configStore.getShape(widget.shapeRef);
    // Pre-resolve context.* properties per IDR-018 rule 4
    final ctx = await state.contextResolver.resolve(
      subjectId: widget.subjectId,
      activityRef: widget.activityRef,
    );
    final decision = await _currentCaptureDecision(state);
    _context
      ..clear()
      ..addAll(ctx);
    if (!mounted) return;
    setState(() {
      _shape = shape;
      _captureAllowed = decision.allowed;
      _actionWarning = decision.warning;
      _loading = false;
    });
    if (shape != null) {
      _applyDefaults();
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
          title: Text(_shape?.name ?? 'Loading...'),
          actions: [
            TextButton(
              onPressed: _saving || !_captureAllowed ? null : _save,
              child: _saving
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('Save'),
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
                        .where((field) => !_hiddenFields.contains(field.name))
                        .map((field) {
                          return WidgetMapper.build(
                            field,
                            _values[field.name],
                            (value) {
                              setState(() {
                                _values[field.name] = value;
                                _dirty = true;
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

    setState(() => _saving = true);

    final state = context.read<AppState>();
    final decision = await _currentCaptureDecision(state);
    if (!decision.allowed) {
      if (!mounted) return;
      setState(() {
        _saving = false;
        _captureAllowed = false;
        _actionWarning = decision.warning;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(decision.warning ?? 'Capture unavailable')),
      );
      return;
    }

    // Clean nulls from payload
    final payload = Map<String, dynamic>.from(_values)
      ..removeWhere((_, v) => v == null);

    await state.eventAssembler.assemble(
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
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Saved. Will sync when online.')),
      );
      Navigator.pop(context);
    }
  }

  Future<ActivityActionDecision> _currentCaptureDecision(AppState state) async {
    final activityRef = widget.activityRef;
    if (activityRef == null) {
      return const ActivityActionDecision.allowed();
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
