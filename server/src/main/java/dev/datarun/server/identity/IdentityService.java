package dev.datarun.server.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Identity Resolver: merge/split operations on subjects.
 * Online-only, server-validated (F9).
 * Merge uses the full DD-3 procedure with row-level locking.
 * subject_lifecycle is a rebuildable projection cache of identity lifecycle events,
 * used only to serialize merge/split precondition checks.
 */
@Service
public class IdentityService {

    private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final AliasCache aliasCache;
    private final ObjectMapper objectMapper;

    public IdentityService(JdbcTemplate jdbc,
                           TransactionTemplate transactionTemplate,
                           EventRepository eventRepository,
                           ServerIdentity serverIdentity,
                           AliasCache aliasCache,
                           ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.transactionTemplate = transactionTemplate;
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.aliasCache = aliasCache;
        this.objectMapper = objectMapper;
    }

    /**
     * Merge two subjects: retired_id is absorbed into surviving_id.
     * Full DD-3 procedure with row-level locking (Database Optimizer validated).
     *
     * @param retiredId  subject to retire (absorbed)
     * @param survivingId subject that survives (absorbs)
     * @param actorId    coordinator performing the merge
     * @param reason     optional reason
     * @return the subjects_merged event
     * @throws IllegalArgumentException if preconditions fail
     */
    public Event merge(UUID retiredId, UUID survivingId, UUID actorId, String reason) {
        if (retiredId.equals(survivingId)) {
            throw new IllegalArgumentException("Cannot merge a subject with itself");
        }

        Event mergeEvent = transactionTemplate.execute(status -> {
            // Step 0: Acquire locks — prevents concurrent merge race condition.
            // Eager-insert lifecycle rows (active is the default, changes no semantics)
            // then lock in consistent order to prevent deadlocks.
            jdbc.update("""
                INSERT INTO subject_lifecycle (subject_id, state)
                VALUES (?::uuid, 'active') ON CONFLICT (subject_id) DO NOTHING
                """, retiredId.toString());
            jdbc.update("""
                INSERT INTO subject_lifecycle (subject_id, state)
                VALUES (?::uuid, 'active') ON CONFLICT (subject_id) DO NOTHING
                """, survivingId.toString());

            // Lock in consistent order (by subject_id) to prevent deadlocks
            UUID first = retiredId.compareTo(survivingId) < 0 ? retiredId : survivingId;
            UUID second = retiredId.compareTo(survivingId) < 0 ? survivingId : retiredId;

            String firstState = jdbc.queryForObject(
                    "SELECT state FROM subject_lifecycle WHERE subject_id = ?::uuid FOR UPDATE",
                    String.class, first.toString());
            String secondState = jdbc.queryForObject(
                    "SELECT state FROM subject_lifecycle WHERE subject_id = ?::uuid FOR UPDATE",
                    String.class, second.toString());

            // Application checks: both must be 'active'. If not → rollback.
            String retiredState = retiredId.equals(first) ? firstState : secondState;
            String survivingState = survivingId.equals(first) ? firstState : secondState;

            if (!"active".equals(retiredState)) {
                throw new IllegalArgumentException(
                        "Subject " + retiredId + " is not active (state: " + retiredState + ")");
            }
            if (!"active".equals(survivingState)) {
                throw new IllegalArgumentException(
                        "Subject " + survivingId + " is not active (state: " + survivingState + ")");
            }

            // Step 1: Cascade existing aliases pointing to retired → surviving
            jdbc.update("""
                UPDATE subject_aliases SET surviving_id = ?::uuid
                WHERE surviving_id = ?::uuid
                """, survivingId.toString(), retiredId.toString());

            // Step 2: Record new alias (idempotent for replay safety)
            jdbc.update("""
                INSERT INTO subject_aliases (retired_id, surviving_id, merged_at)
                VALUES (?::uuid, ?::uuid, NOW())
                ON CONFLICT (retired_id) DO NOTHING
                """, retiredId.toString(), survivingId.toString());

            Event event = buildMergeEvent(retiredId, survivingId, actorId, reason);

            // Step 3: Archive retired subject in the cache using event time so the row is rebuildable.
            jdbc.update("""
                UPDATE subject_lifecycle SET state = 'archived', archived_at = ?::timestamptz
                WHERE subject_id = ?::uuid
                """, event.timestamp().toString(), retiredId.toString());

            // Step 4: Insert subjects_merged event
            eventRepository.insert(event);

            return event;
        });

        // Step 5 (application): refresh alias cache
        aliasCache.refresh();

        log.info("Merged subject {} into {} (event: {})", retiredId, survivingId,
                mergeEvent != null ? mergeEvent.id() : "null");
        return mergeEvent;
    }

    /**
     * Split a subject: source is archived, a new successor is created.
     * Historical events remain attributed to the archived source.
     *
     * @param sourceId  subject to split (archived)
     * @param actorId   coordinator performing the split
     * @param reason    optional reason
     * @return the subject_split event
     * @throws IllegalArgumentException if preconditions fail
     */
    public Event split(UUID sourceId, UUID actorId, String reason) {
        UUID successorId = UUID.randomUUID();

        Event splitEvent = transactionTemplate.execute(status -> {
            // Ensure lifecycle row exists
            jdbc.update("""
                INSERT INTO subject_lifecycle (subject_id, state)
                VALUES (?::uuid, 'active') ON CONFLICT (subject_id) DO NOTHING
                """, sourceId.toString());

            // Lock and check precondition
            String state = jdbc.queryForObject(
                    "SELECT state FROM subject_lifecycle WHERE subject_id = ?::uuid FOR UPDATE",
                    String.class, sourceId.toString());

            if (!"active".equals(state)) {
                throw new IllegalArgumentException(
                        "Subject " + sourceId + " is not active (state: " + state + ")");
            }

            Event event = buildSplitEvent(sourceId, successorId, actorId, reason);

            // Archive source with successor reference using event time so the row is rebuildable.
            jdbc.update("""
                UPDATE subject_lifecycle SET state = 'archived', archived_at = ?::timestamptz, successor_id = ?::uuid
                WHERE subject_id = ?::uuid
                """, event.timestamp().toString(), successorId.toString(), sourceId.toString());

            // Insert subject_split event
            eventRepository.insert(event);

            return event;
        });

        log.info("Split subject {} → successor {} (event: {})", sourceId, successorId,
                splitEvent != null ? splitEvent.id() : "null");
        return splitEvent;
    }

    /**
     * Rebuild the subject_lifecycle projection from identity lifecycle events.
     * The event stream is the source of truth; this cache may be discarded and
     * recomputed for repair, migration, or drift checks.
     */
    public void rebuildSubjectLifecycleFromEvents() {
        transactionTemplate.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM subject_lifecycle");
            jdbc.update("""
                WITH touched_subjects AS (
                    SELECT DISTINCT subject_id
                    FROM (
                        SELECT payload->>'retired_id' AS subject_id
                        FROM events
                        WHERE shape_ref LIKE 'subjects_merged/%'
                        UNION ALL
                        SELECT payload->>'surviving_id' AS subject_id
                        FROM events
                        WHERE shape_ref LIKE 'subjects_merged/%'
                        UNION ALL
                        SELECT payload->>'source_id' AS subject_id
                        FROM events
                        WHERE shape_ref LIKE 'subject_split/%'
                    ) touched
                    WHERE subject_id IS NOT NULL
                ),
                archived_subjects AS (
                    SELECT DISTINCT ON (subject_id)
                           subject_id,
                           archived_at,
                           successor_id
                    FROM (
                        SELECT payload->>'retired_id' AS subject_id,
                               timestamp AS archived_at,
                               NULL::text AS successor_id,
                               sync_watermark
                        FROM events
                        WHERE shape_ref LIKE 'subjects_merged/%'
                        UNION ALL
                        SELECT payload->>'source_id' AS subject_id,
                               timestamp AS archived_at,
                               payload->>'successor_id' AS successor_id,
                               sync_watermark
                        FROM events
                        WHERE shape_ref LIKE 'subject_split/%'
                    ) archived
                    WHERE subject_id IS NOT NULL
                    ORDER BY subject_id, sync_watermark DESC
                )
                INSERT INTO subject_lifecycle (subject_id, state, archived_at, successor_id)
                SELECT touched.subject_id::uuid,
                       CASE WHEN archived.subject_id IS NULL THEN 'active' ELSE 'archived' END,
                       archived.archived_at,
                       archived.successor_id::uuid
                FROM touched_subjects touched
                LEFT JOIN archived_subjects archived ON archived.subject_id = touched.subject_id
                """);
        });
    }

    private Event buildMergeEvent(UUID retiredId, UUID survivingId, UUID actorId, String reason) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", survivingId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId.toString());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("surviving_id", survivingId.toString());
        payload.put("retired_id", retiredId.toString());
        if (reason != null) {
            payload.put("reason", reason);
        }

        return new Event(
                UUID.randomUUID(),
                "capture",
                "subjects_merged/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null, // sync_watermark assigned on insert
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );
    }

    private Event buildSplitEvent(UUID sourceId, UUID successorId, UUID actorId, String reason) {
        ObjectNode subjectRef = objectMapper.createObjectNode();
        subjectRef.put("type", "subject");
        subjectRef.put("id", sourceId.toString());

        ObjectNode actorRef = objectMapper.createObjectNode();
        actorRef.put("type", "actor");
        actorRef.put("id", actorId.toString());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_id", sourceId.toString());
        payload.put("successor_id", successorId.toString());
        if (reason != null) {
            payload.put("reason", reason);
        }

        return new Event(
                UUID.randomUUID(),
                "capture",
                "subject_split/v1",
                null,
                subjectRef,
                actorRef,
                serverIdentity.getDeviceId(),
                (int) serverIdentity.nextDeviceSeq(),
                null, // sync_watermark assigned on insert
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );
    }
}
