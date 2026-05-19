package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.port.in.CreateIdentityPort;
import uk.ac.qmul.digitalid.application.port.in.UpdateIdentityPort;
import uk.ac.qmul.digitalid.application.port.out.DigitalIdRepository;
import uk.ac.qmul.digitalid.domain.*;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

public final class DigitalIdManagementService implements CreateIdentityPort, UpdateIdentityPort {

    private final DigitalIdRepository repository;
    private final AuditEventPublisher auditPublisher;
    private final AuthorisationService authorisationService;
    private final Clock clock;

    public DigitalIdManagementService(DigitalIdRepository repository, AuditEventPublisher auditPublisher,
                                      AuthorisationService authorisationService, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.auditPublisher = Objects.requireNonNull(auditPublisher);
        this.authorisationService = Objects.requireNonNull(authorisationService);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public OperationResult<DigitalId> createIdentity(IdentityCreateCommand command) {
        Optional<DomainError> auth = authorisationService.authoriseManagement(command.requestedBy());
        if (auth.isPresent()) {
            audit("CREATE_IDENTITY", command.requestedBy().organisationId(), false, auth.get().code().name());
            return OperationResult.failure(auth.get());
        }

        if (repository.exists(command.digitalIdNumber())) {
            DomainError error = new DomainError(ErrorCode.DUPLICATE_ID, "Identity already exists");
            audit("CREATE_IDENTITY", command.requestedBy().organisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        DigitalId digitalId = DigitalId.create(command.digitalIdNumber(), command.legalName(),
                command.dateOfBirth(), clock.instant());
        repository.save(digitalId);
        audit("CREATE_IDENTITY", command.requestedBy().organisationId(), true, null);

        return OperationResult.success(digitalId);
    }

    @Override
    public OperationResult<DigitalId> updateMutableAttributes(IdentityUpdateCommand command) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void audit(String eventType, String actor, boolean success, String reasonCode) {
        auditPublisher.notifyObservers(new AuditEvent(eventType, actor, clock.instant(), success, reasonCode));
    }
}
