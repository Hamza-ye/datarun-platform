DELETE FROM expression_rules
WHERE shape_ref IN (
    'assignment_created/v1',
    'assignment_ended/v1',
    'conflict_detected/v1',
    'conflict_resolved/v1',
    'subjects_merged/v1',
    'subject_split/v1'
);

DELETE FROM shapes
WHERE name IN (
    'assignment_created',
    'assignment_ended',
    'conflict_detected',
    'conflict_resolved',
    'subjects_merged',
    'subject_split'
);
