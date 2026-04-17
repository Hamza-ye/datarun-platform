class ShapeField {
  final String key;
  final String label;
  final String type; // text, number, date, select, boolean
  final bool required;
  final List<ShapeOption>? options; // for select type

  ShapeField({
    required this.key,
    required this.label,
    required this.type,
    required this.required,
    this.options,
  });

  factory ShapeField.fromJson(Map<String, dynamic> json) => ShapeField(
        key: json['key'] as String,
        label: json['label'] as String,
        type: json['type'] as String,
        required: json['required'] as bool? ?? false,
        options: (json['options'] as List?)
            ?.map((o) => ShapeOption.fromJson(o as Map<String, dynamic>))
            .toList(),
      );
}

class ShapeOption {
  final String value;
  final String label;

  ShapeOption({required this.value, required this.label});

  factory ShapeOption.fromJson(Map<String, dynamic> json) => ShapeOption(
        value: json['value'] as String,
        label: json['label'] as String,
      );
}

class ShapeDefinition {
  final String shapeRef;
  final String displayName;
  final List<ShapeField> fields;

  ShapeDefinition({
    required this.shapeRef,
    required this.displayName,
    required this.fields,
  });

  factory ShapeDefinition.fromJson(Map<String, dynamic> json) =>
      ShapeDefinition(
        shapeRef: json['shape_ref'] as String,
        displayName: json['display_name'] as String,
        fields: (json['fields'] as List)
            .map((f) => ShapeField.fromJson(f as Map<String, dynamic>))
            .toList(),
      );
}
