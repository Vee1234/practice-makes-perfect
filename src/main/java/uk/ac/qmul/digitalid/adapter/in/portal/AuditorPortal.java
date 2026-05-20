package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.service.audit.AuditQueryService;

import java.util.List;
import java.util.Objects;

public final class AuditorPortal {

    private static final Organisation AUDITOR = new Organisation("DTS-AUDIT", OrganisationRole.AUDITOR);

    private final AuditQueryService auditQueryService;
    private final AuditEventToSummaryMapper projector;

    public AuditorPortal(AuditQueryService auditQueryService) {
        this.auditQueryService = Objects.requireNonNull(auditQueryService, "auditQueryService is required");
        this.projector         = new AuditEventToSummaryMapper();
    }

    public List<AuditEventSummary> getEventSummaries() {
        return auditQueryService.query(AUDITOR)
                .getPayload()
                .stream()
                .map(projector::project)
                .toList();
    }
}
