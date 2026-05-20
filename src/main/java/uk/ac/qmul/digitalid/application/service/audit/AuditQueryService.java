package uk.ac.qmul.digitalid.application.service.audit;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.out.AuditQueryPort;
import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AuditQueryService {

    private final AuditQueryPort auditQueryPort;
    private final AuthorisationService authorisationService;

    public AuditQueryService(AuditQueryPort auditQueryPort, AuthorisationService authorisationService) {
        this.auditQueryPort       = Objects.requireNonNull(auditQueryPort,       "auditQueryPort is required");
        this.authorisationService = Objects.requireNonNull(authorisationService, "authorisationService is required");
    }

    public OperationResult<List<AuditEvent>> query(Organisation requestedBy) {
        Objects.requireNonNull(requestedBy, "requestedBy is required");

        Optional<DomainError> authError = authorisationService.authoriseAuditQuery(requestedBy);
        if (authError.isPresent()) {
            return OperationResult.failure(authError.get());
        }

        return OperationResult.success(auditQueryPort.findAll());
    }
}
