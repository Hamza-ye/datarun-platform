package dev.datarun.server.integrity;

import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Stateless sweep job — re-evaluates subjects with multi-device events.
 * Catches flags missed by Tx2 failures and asymmetric-flagging race conditions.
 * Deterministic flag IDs prevent duplicate flags (idempotent).
 * Runs every 5 minutes. No tracking tables.
 */
@Component
public class ConflictSweepJob {

    private static final Logger log = LoggerFactory.getLogger(ConflictSweepJob.class);

    private final ConflictDetector conflictDetector;
    private final EventRepository eventRepository;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbc;

    public ConflictSweepJob(ConflictDetector conflictDetector,
                            EventRepository eventRepository,
                            TransactionTemplate transactionTemplate,
                            JdbcTemplate jdbc) {
        this.conflictDetector = conflictDetector;
        this.eventRepository = eventRepository;
        this.transactionTemplate = transactionTemplate;
        this.jdbc = jdbc;
    }

    @Scheduled(fixedDelay = 300_000) // 5 minutes
    public void sweep() {
        // Trailing window: look back 1 hour (720 watermark slots at ~5/min is generous)
        Long maxWatermark = jdbc.queryForObject(
                "SELECT COALESCE(MAX(sync_watermark), 0) FROM events", Long.class);
        if (maxWatermark == null || maxWatermark == 0) {
            return;
        }

        // Use a watermark trailing window — sweep all events from the last hour-equivalent
        // For simplicity, use watermark 0 on small datasets, otherwise trail by a configurable window
        long trailingFrom = Math.max(0, maxWatermark - 1000);

        try {
            List<Event> flagEvents = conflictDetector.sweep(trailingFrom);
            if (!flagEvents.isEmpty()) {
                int persisted = persistFlagEvents(flagEvents);
                log.info("Sweep: raised {} new flags", persisted);
            }
        } catch (Exception e) {
            log.warn("Sweep failed (will retry next cycle): {}", e.getMessage());
        }
    }

    private int persistFlagEvents(List<Event> flagEvents) {
        Integer result = transactionTemplate.execute(status -> {
            int count = 0;
            for (Event flag : flagEvents) {
                if (eventRepository.insert(flag)) {
                    count++;
                }
            }
            return count;
        });
        return result != null ? result : 0;
    }
}
