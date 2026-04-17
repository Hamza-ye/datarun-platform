import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/presentation/widgets/widget_mapper.dart';

/// S3: Form — shape-driven event creation.
class FormScreen extends StatefulWidget {
  final String? subjectId; // null = new subject
  final String shapeRef;

  const FormScreen({
    super.key,
    required this.subjectId,
    required this.shapeRef,
  });

  @override
  State<FormScreen> createState() => _FormScreenState();
}

class _FormScreenState extends State<FormScreen> {
  final _formKey = GlobalKey<FormState>();
  ShapeDefinition? _shape;
  final Map<String, dynamic> _values = {};
  bool _loading = true;
  bool _dirty = false;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _loadShape();
  }

  Future<void> _loadShape() async {
    final state = context.read<AppState>();
    final shape = await state.configLoader.loadShape(widget.shapeRef);
    setState(() {
      _shape = shape;
      _loading = false;
    });
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
          title: Text(_shape?.displayName ?? 'Loading...'),
          actions: [
            TextButton(
              onPressed: _saving ? null : _save,
              child: _saving
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2))
                  : const Text('Save'),
            ),
          ],
        ),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : Form(
                key: _formKey,
                child: ListView(
                  children: _shape!.fields.map((field) {
                    return WidgetMapper.build(
                      field,
                      _values[field.key],
                      (value) {
                        setState(() {
                          _values[field.key] = value;
                          _dirty = true;
                        });
                      },
                    );
                  }).toList(),
                ),
              ),
      ),
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _saving = true);

    final state = context.read<AppState>();
    // Clean nulls from payload
    final payload = Map<String, dynamic>.from(_values)
      ..removeWhere((_, v) => v == null);

    await state.eventAssembler.assemble(
      subjectId: widget.subjectId,
      shapeRef: widget.shapeRef,
      payload: payload,
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
