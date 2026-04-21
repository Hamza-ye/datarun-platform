import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:datarun_mobile/presentation/app_state.dart';
import 'package:datarun_mobile/presentation/screens/form_screen.dart';
import 'package:datarun_mobile/domain/event.dart';
import 'package:datarun_mobile/domain/shape.dart';

/// S2: Subject Detail — hub for a single subject.
class SubjectDetailScreen extends StatefulWidget {
  final String subjectId;

  const SubjectDetailScreen({super.key, required this.subjectId});

  @override
  State<SubjectDetailScreen> createState() => _SubjectDetailScreenState();
}

class _SubjectDetailScreenState extends State<SubjectDetailScreen> {
  List<Event> _events = [];
  Set<String> _flaggedIds = {};
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadEvents();
  }

  Future<void> _loadEvents() async {
    final state = context.read<AppState>();
    final events =
        await state.projectionEngine.getSubjectDetail(widget.subjectId);
    final flaggedIds = await state.projectionEngine.getFlaggedEventIds();
    setState(() {
      _events = events;
      _flaggedIds = flaggedIds;
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final name = _events.isNotEmpty
        ? (_events.last.payload['name'] as String? ?? 'Unnamed subject')
        : 'Subject';

    return Scaffold(
      appBar: AppBar(title: Text(name)),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _events.isEmpty
              ? const Center(child: Text('No events'))
              : ListView.builder(
                  itemCount: _events.length,
                  itemBuilder: (context, index) {
                    final e = _events[index];
                    return _EventTile(
                        event: e, isFlagged: _flaggedIds.contains(e.id));
                  },
                ),
      // Action bar — Phase 0: single "Capture" action
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.all(16),
        child: FilledButton.icon(
          onPressed: () => _capture(context),
          icon: const Icon(Icons.add),
          label: const Text('Capture'),
        ),
      ),
    );
  }

  void _capture(BuildContext context) {
    final state = context.read<AppState>();
    final configStore = state.configStore;
    final activeActivities = configStore.getActiveActivities();

    // Collect all shapes across active activities, tracking the activity for each shape
    final allShapes = <ShapeDefinition>[];
    final shapeToActivity = <String, String>{};
    for (final actName in activeActivities) {
      for (final shape in configStore.getShapesForActivity(actName)) {
        allShapes.add(shape);
        shapeToActivity[shape.shapeRef] = actName;
      }
    }

    if (allShapes.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No activities configured')),
      );
      return;
    }

    void navigateToForm(String shapeRef) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => FormScreen(
            subjectId: widget.subjectId,
            shapeRef: shapeRef,
            activityRef: shapeToActivity[shapeRef],
          ),
        ),
      ).then((_) {
        if (!mounted) return;
        _loadEvents();
        context.read<AppState>().refresh();
      });
    }

    if (allShapes.length == 1) {
      navigateToForm(allShapes.first.shapeRef);
      return;
    }

    showDialog<ShapeDefinition>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Select form'),
        children: allShapes.map((shape) {
          return SimpleDialogOption(
            onPressed: () => Navigator.pop(ctx, shape),
            child: Text(shape.name),
          );
        }).toList(),
      ),
    ).then((selected) {
      if (selected != null) {
        navigateToForm(selected.shapeRef);
      }
    });
  }
}

class _EventTile extends StatelessWidget {
  final Event event;
  final bool isFlagged;

  const _EventTile({required this.event, this.isFlagged = false});

  @override
  Widget build(BuildContext context) {
    final dt = DateTime.tryParse(event.timestamp);
    final timeStr = dt != null
        ? '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')} ${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}'
        : event.timestamp;

    return ExpansionTile(
      leading: Icon(
        _iconForType(event.type),
        color: isFlagged ? Colors.red : null,
      ),
      title: Row(
        children: [
          Expanded(child: Text('${event.type} · ${event.shapeRef}')),
          if (isFlagged)
            Container(
              padding:
                  const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: Colors.red.shade100,
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                'FLAGGED',
                style: TextStyle(
                    fontSize: 10,
                    color: Colors.red.shade900,
                    fontWeight: FontWeight.bold),
              ),
            ),
        ],
      ),
      subtitle: Text(timeStr),
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: event.payload.entries.map((e) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text('${e.key}: ${e.value ?? '—'}',
                    style: Theme.of(context).textTheme.bodyMedium),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  IconData _iconForType(String type) {
    switch (type) {
      case 'capture':
        return Icons.edit_note;
      case 'review':
        return Icons.rate_review;
      default:
        return Icons.event;
    }
  }
}
