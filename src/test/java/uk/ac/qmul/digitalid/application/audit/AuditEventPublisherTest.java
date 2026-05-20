package uk.ac.qmul.digitalid.application.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventPublisherTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private AuditEventPublisher publisher;
    private InMemoryAuditSink sink;

    @BeforeEach
    void setUp() {
        publisher = new AuditEventPublisher();
        sink = new InMemoryAuditSink();
    }

    @Test
    void shouldNotifyObserver_whenEventPublished() {
        AuditEvent event = new AuditEvent("CREATE", "CENTRAL_AUTHORITY", NOW, true, null);
        publisher.attach(sink);

        publisher.notifyObservers(event);

        assertThat(sink.findAll()).containsExactly(event);
    }

    @Test
    void shouldNotifyAllObservers_whenMultipleObserversAttached() {
        AuditEvent event = new AuditEvent("SUSPEND", "CENTRAL_AUTHORITY", NOW, true, "FRAUD");
        InMemoryAuditSink secondSink = new InMemoryAuditSink();
        publisher.attach(sink);
        publisher.attach(secondSink);

        publisher.notifyObservers(event);

        assertThat(sink.findAll()).containsExactly(event);
        assertThat(secondSink.findAll()).containsExactly(event);
    }

    @Test
    void shouldNotNotifyDetachedObserver_whenEventPublished() {
        AuditEvent event = new AuditEvent("REVOKE", "CENTRAL_AUTHORITY", NOW, false, "EXPIRED");
        publisher.attach(sink);
        publisher.detach(sink);

        publisher.notifyObservers(event);

        assertThat(sink.findAll()).isEmpty();
    }

    @Test
    void shouldNotifyNoObservers_whenNoneAttached() {
        AuditEvent event = new AuditEvent("CREATE", "CENTRAL_AUTHORITY", NOW, true, null);

        publisher.notifyObservers(event);

        assertThat(sink.findAll()).isEmpty();
    }
}
