import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:datarun_mobile/domain/shape.dart';

/// Registry-based widget mapper. Maps field type → widget builder.
/// Phase 0: text, number, date, select, boolean.
/// Phase 3a: integer, decimal, narrative, multi_select, location, subject_ref.
class WidgetMapper {
  static final Map<String, Widget Function(ShapeField, dynamic, ValueChanged)>
      _registry = {
    'text': _buildTextField,
    'number': _buildDecimalField, // backward compat alias
    'integer': _buildIntegerField,
    'decimal': _buildDecimalField,
    'date': _buildDateField,
    'select': _buildSelectField,
    'multi_select': _buildMultiSelectField,
    'boolean': _buildBooleanField,
    'narrative': _buildNarrativeField,
    'location': _buildLocationPlaceholder,
    'subject_ref': _buildSubjectRefPlaceholder,
  };

  static Widget build(
      ShapeField field, dynamic value, ValueChanged<dynamic> onChanged,
      {String? warningMessage}) {
    final builder = _registry[field.type];
    Widget fieldWidget;
    if (builder == null) {
      fieldWidget = ListTile(
        title: Text(field.label),
        subtitle: Text('Unsupported field type: ${field.type}'),
      );
    } else {
      fieldWidget = builder(field, value, onChanged);
    }
    if (warningMessage != null) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          fieldWidget,
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              warningMessage,
              style: const TextStyle(color: Colors.amber, fontSize: 12),
            ),
          ),
        ],
      );
    }
    return fieldWidget;
  }

  static Widget _buildTextField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TextFormField(
        initialValue: value as String? ?? '',
        decoration: InputDecoration(
          labelText: field.label,
          border: const OutlineInputBorder(),
        ),
        onChanged: (v) => onChanged(v.isEmpty ? null : v),
        validator: field.required
            ? (v) => (v == null || v.isEmpty) ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildIntegerField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TextFormField(
        initialValue: value?.toString() ?? '',
        decoration: InputDecoration(
          labelText: field.label,
          border: const OutlineInputBorder(),
        ),
        keyboardType: TextInputType.number,
        onChanged: (v) {
          if (v.isEmpty) {
            onChanged(null);
          } else {
            onChanged(int.tryParse(v));
          }
        },
        validator: field.required
            ? (v) => (v == null || v.isEmpty) ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildDecimalField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TextFormField(
        initialValue: value?.toString() ?? '',
        decoration: InputDecoration(
          labelText: field.label,
          border: const OutlineInputBorder(),
        ),
        keyboardType: const TextInputType.numberWithOptions(decimal: true),
        onChanged: (v) {
          if (v.isEmpty) {
            onChanged(null);
          } else {
            final parsed = num.tryParse(v);
            onChanged(parsed);
          }
        },
        validator: field.required
            ? (v) => (v == null || v.isEmpty) ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildDateField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return _DateFieldWidget(field: field, value: value, onChanged: onChanged);
  }

  static Widget _buildSelectField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      // ignore: deprecated_member_use
      child: DropdownButtonFormField<String>(
        value: value as String?,
        decoration: InputDecoration(
          labelText: field.label,
          border: const OutlineInputBorder(),
        ),
        items: field.options
                ?.map((o) =>
                    DropdownMenuItem(value: o, child: Text(o)))
                .toList() ??
            [],
        onChanged: (v) => onChanged(v),
        validator: field.required
            ? (v) => v == null ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildMultiSelectField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    final selected = (value as List<String>?) ?? [];
    return _MultiSelectChipsWidget(
        field: field, selected: selected, onChanged: onChanged);
  }

  static Widget _buildBooleanField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return SwitchListTile(
      title: Text(field.label),
      value: value as bool? ?? false,
      onChanged: (v) => onChanged(v),
    );
  }

  static Widget _buildNarrativeField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TextFormField(
        initialValue: value as String? ?? '',
        decoration: InputDecoration(
          labelText: field.label,
          border: const OutlineInputBorder(),
        ),
        maxLines: 5,
        onChanged: (v) => onChanged(v.isEmpty ? null : v),
        validator: field.required
            ? (v) => (v == null || v.isEmpty) ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildLocationPlaceholder(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TextFormField(
        initialValue: value as String? ?? '',
        decoration: InputDecoration(
          labelText: '\u{1F4CD} ${field.label}',
          border: const OutlineInputBorder(),
        ),
        onChanged: (v) => onChanged(v.isEmpty ? null : v),
        validator: field.required
            ? (v) => (v == null || v.isEmpty) ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildSubjectRefPlaceholder(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TextFormField(
        initialValue: value as String? ?? '',
        decoration: InputDecoration(
          labelText: '\u{1F517} ${field.label}',
          border: const OutlineInputBorder(),
        ),
        onChanged: (v) => onChanged(v.isEmpty ? null : v),
        validator: field.required
            ? (v) => (v == null || v.isEmpty) ? '${field.label} is required' : null
            : null,
      ),
    );
  }
}

class _MultiSelectChipsWidget extends StatefulWidget {
  final ShapeField field;
  final List<String> selected;
  final ValueChanged onChanged;

  const _MultiSelectChipsWidget({
    required this.field,
    required this.selected,
    required this.onChanged,
  });

  @override
  State<_MultiSelectChipsWidget> createState() => _MultiSelectChipsWidgetState();
}

class _MultiSelectChipsWidgetState extends State<_MultiSelectChipsWidget> {
  late List<String> _selected;

  @override
  void initState() {
    super.initState();
    _selected = List.from(widget.selected);
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: InputDecorator(
        decoration: InputDecoration(
          labelText: widget.field.label,
          border: const OutlineInputBorder(),
        ),
        child: Wrap(
          spacing: 8,
          children: (widget.field.options ?? []).map((option) {
            final isSelected = _selected.contains(option);
            return FilterChip(
              label: Text(option),
              selected: isSelected,
              onSelected: (selected) {
                setState(() {
                  if (selected) {
                    _selected.add(option);
                  } else {
                    _selected.remove(option);
                  }
                });
                widget.onChanged(_selected.isEmpty ? null : List<String>.from(_selected));
              },
            );
          }).toList(),
        ),
      ),
    );
  }
}

class _DateFieldWidget extends StatelessWidget {
  final ShapeField field;
  final dynamic value;
  final ValueChanged onChanged;

  const _DateFieldWidget({
    required this.field,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final dateStr = value as String?;
    final displayText = dateStr ?? 'Select date';

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: InkWell(
        onTap: () async {
          final initial = dateStr != null
              ? DateTime.tryParse(dateStr) ?? DateTime.now()
              : DateTime.now();
          final picked = await showDatePicker(
            context: context,
            initialDate: initial,
            firstDate: DateTime(2000),
            lastDate: DateTime(2100),
          );
          if (picked != null) {
            onChanged(DateFormat('yyyy-MM-dd').format(picked));
          }
        },
        child: InputDecorator(
          decoration: InputDecoration(
            labelText: field.label,
            border: const OutlineInputBorder(),
            suffixIcon: const Icon(Icons.calendar_today),
          ),
          child: Text(displayText),
        ),
      ),
    );
  }
}
