package uk.ac.qmul.digitalid.adapter.out.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.application.audit.AuditEvent;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAuditSinkTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private InMemoryAuditSink auditSink;

    @BeforeEach
    void setUp() {
        auditSink = new InMemoryAuditSink();
    }

    @Test
    void shouldReturnEmptyList_whenNoEventsRecorded() {
        assertThat(auditSink.findAll()).isEmpty();
    }

    @Test
    void shouldRecordEvent_whenEventIsSubmitted() {
        AuditEvent event = new AuditEvent("CREATE", "CENTRAL_AUTHORITY", NOW, true, null);

        auditSink.record(event);

        assertThat(auditSink.findAll()).containsExactly(event);
    }

    @Test
    void shouldRecordEventsInOrder_whenMultipleEventsSubmitted() {
        AuditEvent first  = new AuditEvent("CREATE",  "CENTRAL_AUTHORITY", NOW, true, null);
        AuditEvent second = new AuditEvent("SUSPEND", "CENTRAL_AUTHORITY", NOW, true, "FRAUD");

        auditSink.record(first);
        auditSink.record(second);

        List<AuditEvent> events = auditSink.findAll();
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isEqualTo(first);
        assertThat(events.get(1)).isEqualTo(second);
    }
}
