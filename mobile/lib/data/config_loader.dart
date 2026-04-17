import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:datarun_mobile/domain/shape.dart';

class ConfigLoader {
  final Map<String, ShapeDefinition> _cache = {};

  Future<ShapeDefinition> loadShape(String shapeRef) async {
    if (_cache.containsKey(shapeRef)) return _cache[shapeRef]!;

    // Convert shape_ref "basic_capture/v1" → asset path "basic_capture_v1.json"
    final fileName = shapeRef.replaceAll('/', '_');
    final jsonStr =
        await rootBundle.loadString('assets/shapes/$fileName.json');
    final json = jsonDecode(jsonStr) as Map<String, dynamic>;
    final shape = ShapeDefinition.fromJson(json);
    _cache[shapeRef] = shape;
    return shape;
  }
}
