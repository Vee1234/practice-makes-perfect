package uk.ac.qmul.digitalid.application.port.out;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;

import java.util.List;

public interface AuditQueryPort {
    List<AuditEvent> findAll();
}
