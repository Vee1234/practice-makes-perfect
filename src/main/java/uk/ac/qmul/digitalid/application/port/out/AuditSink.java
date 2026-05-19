package uk.ac.qmul.digitalid.application.port.out;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;

public interface AuditSink {
    void record(AuditEvent event);
}