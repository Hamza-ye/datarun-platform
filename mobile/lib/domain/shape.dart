/// IDR-017 shape field definition.
class ShapeField {
  final String name;
  final String type; // text, integer, decimal, boolean, date, select, multi_select, location, subject_ref, narrative
  final bool required;
  final String? description; // display label
  final int displayOrder;
  final String? group;
  final bool deprecated;
  final List<String>? options; // for select/multi_select only
  // validation: reserved for Phase 3b

  ShapeField({
    required this.name,
    required this.type,
    required this.required,
    this.description,
    this.displayOrder = 0,
    this.group,
    this.deprecated = false,
    this.options,
  });

  /// Display label: description if available, otherwise name.
  String get label => description ?? name;

  factory ShapeField.fromJson(Map<String, dynamic> json) => ShapeField(
        name: json['name'] as String,
        type: json['type'] as String,
        required: json['required'] as bool? ?? false,
        description: json['description'] as String?,
        displayOrder: json['display_order'] as int? ?? 0,
        group: json['group'] as String?,
        deprecated: json['deprecated'] as bool? ?? false,
        options: (json['options'] as List?)?.cast<String>(),
      );
}

/// IDR-017 shape definition.
class ShapeDefinition {
  final String shapeRef; // e.g. "household_visit/v1"
  final String name;
  final int version;
  final String status; // active, deprecated
  final String sensitivity;
  final List<ShapeField> fields;

  ShapeDefinition({
    required this.shapeRef,
    required this.name,
    required this.version,
    required this.status,
    required this.sensitivity,
    required this.fields,
  });

  /// Active (non-deprecated) fields sorted by display_order.
  List<ShapeField> get activeFields =>
      fields.where((f) => !f.deprecated).toList()
        ..sort((a, b) => a.displayOrder.compareTo(b.displayOrder));

  /// Parse from config package shapes map entry.
  /// [shapeRef] is the key in the shapes map (e.g. "household_visit/v1").
  /// [json] is the value object with name, version, status, sensitivity, fields.
  factory ShapeDefinition.fromConfigJson(String shapeRef, Map<String, dynamic> json) =>
      ShapeDefinition(
        shapeRef: shapeRef,
        name: json['name'] as String,
        version: json['version'] as int,
        status: json['status'] as String? ?? 'active',
        sensitivity: json['sensitivity'] as String? ?? 'standard',
        fields: (json['fields'] as List?)
                ?.map((f) => ShapeField.fromJson(f as Map<String, dynamic>))
                .toList() ??
            [],
      );
}
