package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.Instant;
import java.util.Objects;

public final class AuditEventSummary {

    private final String eventType;
    private final String actor;
    private final Instant timestamp;
    private final boolean success;
    private final String reasonCode;

    public AuditEventSummary(String eventType, String actor, Instant timestamp,
                             boolean success, String reasonCode) {
        this.eventType = Objects.requireNonNull(eventType, "eventType is required");
        this.actor     = Objects.requireNonNull(actor,     "actor is required");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp is required");
        this.success   = success;
        this.reasonCode = reasonCode;
    }

    public String getEventType()  { return eventType; }
    public String getActor()      { return actor; }
    public Instant getTimestamp() { return timestamp; }
    public boolean isSuccess()    { return success; }
    public String getReasonCode() { return reasonCode; }
}
