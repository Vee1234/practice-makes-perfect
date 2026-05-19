package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;

final class AuditEventSummaryProjector {

    AuditEventSummary project(AuditEvent event) {
        return new AuditEventSummary(
                event.getEventType(),
                event.getActor(),
                event.getTimestamp(),
                event.isSuccess(),
                event.getReasonCode()
        );
    }
}
