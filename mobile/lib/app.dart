import 'package:flutter/material.dart';

import 'data/db.dart';
import 'data/prefs.dart';
import 'net/sync_client.dart';
import 'screens/home_screen.dart';
import 'screens/setup_screen.dart';

class DatarunApp extends StatelessWidget {
  final Prefs prefs;
  final LocalDb db;
  const DatarunApp({super.key, required this.prefs, required this.db});

  @override
  Widget build(BuildContext context) {
    final client = SyncClient(prefs, db);
    return MaterialApp(
      title: 'Datarun Mobile',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: prefs.isProvisioned
          ? HomeScreen(prefs: prefs, db: db, client: client)
          : SetupScreen(prefs: prefs, db: db, client: client),
    );
  }
}
