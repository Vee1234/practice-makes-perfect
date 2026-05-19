package uk.ac.qmul.digitalid.application.audit;

import java.time.Instant;
import java.util.Objects;

public final class AuditEvent {

    private final String eventType;
    private final String actor;
    private final Instant timestamp;
    private final boolean success;
    private final String reasonCode;

    public AuditEvent(String eventType, String actor, Instant timestamp, boolean success, String reasonCode) {
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(actor, "actor is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        this.eventType = eventType;
        this.actor = actor;
        this.timestamp = timestamp;
        this.success = success;
        this.reasonCode = reasonCode;
    }

    public String getEventType()  { return eventType; }
    public String getActor()      { return actor; }
    public Instant getTimestamp() { return timestamp; }
    public boolean isSuccess()    { return success; }
    public String getReasonCode() { return reasonCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditEvent e)) return false;
        return success == e.success
                && eventType.equals(e.eventType)
                && actor.equals(e.actor)
                && timestamp.equals(e.timestamp)
                && Objects.equals(reasonCode, e.reasonCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, actor, timestamp, success, reasonCode);
    }
}