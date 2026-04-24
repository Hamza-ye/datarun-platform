import 'package:flutter/material.dart';

import 'app.dart';
import 'data/db.dart';
import 'data/prefs.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final prefs = await Prefs.load();
  final db = await LocalDb.open();
  runApp(DatarunApp(prefs: prefs, db: db));
}
