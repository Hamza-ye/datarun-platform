import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:datarun_mobile/domain/shape.dart';

/// Registry-based widget mapper. Maps field type → widget builder.
/// Phase 0: text, number, date, select, boolean.
class WidgetMapper {
  static final Map<String, Widget Function(ShapeField, dynamic, ValueChanged)>
      _registry = {
    'text': _buildTextField,
    'number': _buildNumberField,
    'date': _buildDateField,
    'select': _buildSelectField,
    'boolean': _buildBooleanField,
  };

  static Widget build(
      ShapeField field, dynamic value, ValueChanged<dynamic> onChanged) {
    final builder = _registry[field.type];
    if (builder == null) {
      return ListTile(
        title: Text(field.label),
        subtitle: Text('Unsupported field type: ${field.type}'),
      );
    }
    return builder(field, value, onChanged);
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

  static Widget _buildNumberField(
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
                    DropdownMenuItem(value: o.value, child: Text(o.label)))
                .toList() ??
            [],
        onChanged: (v) => onChanged(v),
        validator: field.required
            ? (v) => v == null ? '${field.label} is required' : null
            : null,
      ),
    );
  }

  static Widget _buildBooleanField(
      ShapeField field, dynamic value, ValueChanged onChanged) {
    return SwitchListTile(
      title: Text(field.label),
      value: value as bool? ?? false,
      onChanged: (v) => onChanged(v),
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
