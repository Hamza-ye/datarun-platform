import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:datarun_mobile/domain/shape.dart';
import 'package:datarun_mobile/presentation/widgets/widget_mapper.dart';

void main() {
  group('WidgetMapper', () {
    testWidgets('renders text field from shape definition', (tester) async {
      final field = ShapeField(
        name: 'name',
        description: 'Name',
        type: 'text',
        required: true,
      );

      dynamic captured;
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: WidgetMapper.build(field, null, (v) => captured = v),
        ),
      ));

      expect(find.text('Name'), findsOneWidget);
      expect(find.byType(TextFormField), findsOneWidget);

      await tester.enterText(find.byType(TextFormField), 'Alice');
      expect(captured, 'Alice');
    });

    testWidgets('renders number field from shape definition', (tester) async {
      final field = ShapeField(
        name: 'value',
        description: 'Numeric Value',
        type: 'number',
        required: false,
      );

      dynamic captured;
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: WidgetMapper.build(field, null, (v) => captured = v),
        ),
      ));

      expect(find.text('Numeric Value'), findsOneWidget);

      await tester.enterText(find.byType(TextFormField), '42');
      expect(captured, 42);
    });

    testWidgets('renders select field with options from shape', (tester) async {
      final field = ShapeField(
        name: 'category',
        description: 'Category',
        type: 'select',
        required: true,
        options: ['urban', 'rural'],
      );

      dynamic captured;
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: WidgetMapper.build(field, null, (v) => captured = v),
        ),
      ));

      expect(find.text('Category'), findsOneWidget);
      // ignore: deprecated_member_use
      expect(find.byType(DropdownButtonFormField<String>), findsOneWidget);
    });

    testWidgets('renders boolean field from shape definition', (tester) async {
      final field = ShapeField(
        name: 'active',
        description: 'Active',
        type: 'boolean',
        required: false,
      );

      dynamic captured;
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: WidgetMapper.build(field, false, (v) => captured = v),
        ),
      ));

      expect(find.text('Active'), findsOneWidget);
      expect(find.byType(SwitchListTile), findsOneWidget);

      await tester.tap(find.byType(Switch));
      expect(captured, true);
    });

    testWidgets('renders date field from shape definition', (tester) async {
      final field = ShapeField(
        name: 'date',
        description: 'Date',
        type: 'date',
        required: true,
      );

      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: WidgetMapper.build(field, null, (_) {}),
        ),
      ));

      expect(find.text('Date'), findsOneWidget);
      expect(find.text('Select date'), findsOneWidget);
      expect(find.byIcon(Icons.calendar_today), findsOneWidget);
    });

    testWidgets('renders all 5 field types from shape', (tester) async {
      // Simulates the full FieldResolver → WidgetMapper pipeline
      final fields = [
        ShapeField(name: 'name', description: 'Name', type: 'text', required: true),
        ShapeField(name: 'date', description: 'Date', type: 'date', required: true),
        ShapeField(
          name: 'category',
          description: 'Category',
          type: 'select',
          required: true,
          options: ['urban'],
        ),
        ShapeField(name: 'notes', description: 'Notes', type: 'text', required: false),
        ShapeField(name: 'value', description: 'Value', type: 'number', required: false),
      ];

      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: ListView(
            children: fields
                .map((f) => WidgetMapper.build(f, null, (_) {}))
                .toList(),
          ),
        ),
      ));

      expect(find.text('Name'), findsOneWidget);
      expect(find.text('Date'), findsOneWidget);
      expect(find.text('Category'), findsOneWidget);
      expect(find.text('Notes'), findsOneWidget);
      expect(find.text('Value'), findsOneWidget);
    });

    testWidgets('required field validation shows error', (tester) async {
      final field = ShapeField(
        name: 'name',
        description: 'Name',
        type: 'text',
        required: true,
      );

      final formKey = GlobalKey<FormState>();
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: Form(
            key: formKey,
            child: WidgetMapper.build(field, null, (_) {}),
          ),
        ),
      ));

      // Trigger validation without entering text
      formKey.currentState!.validate();
      await tester.pump();

      expect(find.text('Name is required'), findsOneWidget);
    });

    test('unsupported field type renders fallback', () {
      final field = ShapeField(
        name: 'unknown',
        description: 'Unknown',
        type: 'gps',
        required: false,
      );

      final widget = WidgetMapper.build(field, null, (_) {});
      expect(widget, isA<Widget>());
    });
  });
}
