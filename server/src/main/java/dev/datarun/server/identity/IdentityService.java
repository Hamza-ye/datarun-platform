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
import java.util.Arrays;
import java.util.UUID;

/**
 * Identity Resolver: merge/split operations on subjects.
 * Online-only, server-validated (F9).
 * Merge/split preconditions project identity lifecycle from events and serialize
 * irreversible lineage writes with transaction-scoped advisory locks.
 */
@Service
public class IdentityService {

    private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private final EventRepository eventRepository;
    private final ServerIdentity serverIdentity;
    private final SubjectAliasProjection subjectAliasProjection;
    private final IdentityLifecycleProjection lifecycleProjection;
    private final ObjectMapper objectMapper;

    public IdentityService(JdbcTemplate jdbc,
                           TransactionTemplate transactionTemplate,
                           EventRepository eventRepository,
                           ServerIdentity serverIdentity,
                           SubjectAliasProjection subjectAliasProjection,
                           IdentityLifecycleProjection lifecycleProjection,
                           ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.transactionTemplate = transactionTemplate;
        this.eventRepository = eventRepository;
        this.serverIdentity = serverIdentity;
        this.subjectAliasProjection = subjectAliasProjection;
        this.lifecycleProjection = lifecycleProjection;
        this.objectMapper = objectMapper;
    }

    /**
     * Merge two subjects: retired_id is absorbed into surviving_id.
     * Current DD-3 procedure with subject-scoped advisory locking.
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
            // Step 0: Serialize irreversible lineage writes for both subject IDs.
            lockSubjects(retiredId, survivingId);

            String retiredState = lifecycleProjection.stateOf(retiredId);
            String survivingState = lifecycleProjection.stateOf(survivingId);

            if (!"active".equals(retiredState)) {
                throw new IllegalArgumentException(
                        "Subject " + retiredId + " is not active (state: " + retiredState + ")");
            }
            if (!"active".equals(survivingState)) {
                throw new IllegalArgumentException(
                        "Subject " + survivingId + " is not active (state: " + survivingState + ")");
            }

            Event event = buildMergeEvent(retiredId, survivingId, actorId, reason);

            // Step 1: Update the rebuildable alias projection with eager transitive closure.
            subjectAliasProjection.upsertAlias(retiredId, survivingId, event.timestamp());

            // Step 3: Insert subjects_merged event. Lifecycle is derived from this event.
            eventRepository.insert(event);

            return event;
        });

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
            lockSubjects(sourceId);

            String state = lifecycleProjection.stateOf(sourceId);

            if (!"active".equals(state)) {
                throw new IllegalArgumentException(
                        "Subject " + sourceId + " is not active (state: " + state + ")");
            }

            Event event = buildSplitEvent(sourceId, successorId, actorId, reason);

            // Insert subject_split event. Lifecycle is derived from this event.
            eventRepository.insert(event);

            return event;
        });

        log.info("Split subject {} → successor {} (event: {})", sourceId, successorId,
                splitEvent != null ? splitEvent.id() : "null");
        return splitEvent;
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

    private void lockSubjects(UUID... subjectIds) {
        Arrays.stream(subjectIds)
                .distinct()
                .sorted()
                .forEach(subjectId -> jdbc.query(
                        "SELECT pg_advisory_xact_lock(?)",
                        rs -> { },
                        advisoryLockKey(subjectId)));
    }

    private long advisoryLockKey(UUID subjectId) {
        return subjectId.getMostSignificantBits() ^ subjectId.getLeastSignificantBits();
    }
}
