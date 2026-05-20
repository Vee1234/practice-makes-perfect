package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.in.AddRestrictionPort;
import uk.ac.qmul.digitalid.application.port.in.ChangeStatusPort;
import uk.ac.qmul.digitalid.application.port.in.CreateIdentityPort;
import uk.ac.qmul.digitalid.application.port.in.FindIdentityPort;
import uk.ac.qmul.digitalid.application.port.in.SetWelfareBandPort;
import uk.ac.qmul.digitalid.application.port.in.UpdateIdentityPort;
import uk.ac.qmul.digitalid.application.port.out.DigitalIdRepository;
import uk.ac.qmul.digitalid.domain.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class DigitalIdManagementService implements CreateIdentityPort, UpdateIdentityPort, ChangeStatusPort, AddRestrictionPort, SetWelfareBandPort, FindIdentityPort {

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
        return executeManagementOperation(command, () -> {
            if (repository.exists(command.getDigitalIdNumber()))
                return OperationResult.failure(new DomainError(ErrorCode.DUPLICATE_ID, "Identity already exists"));
            DigitalId created = DigitalId.create(command.getDigitalIdNumber(), command.getLegalName(),
                    command.getDateOfBirth(), clock.instant());
            repository.save(created);
            return OperationResult.success(created);
        });
    }

    @Override
    public OperationResult<DigitalId> updateMutableAttributes(IdentityUpdateCommand command) {
        return executeManagementOperation(command, () -> {
            OperationResult<DigitalId> found = loadIdentity(command);
            if (!found.isSuccess()) return found;
            DigitalId existing = found.getPayload();
            if (existing.getStatus() == IdentityStatus.REVOKED || existing.getStatus() == IdentityStatus.EXPIRED)
                return OperationResult.failure(new DomainError(ErrorCode.NOT_MODIFIABLE, "Identity is in a terminal state and cannot be modified"));
            DigitalId updated = existing.updateLegalName(command.getNewLegalName(), clock.instant());
            repository.save(updated);
            return OperationResult.success(updated);
        });
    }

    @Override
    public OperationResult<DigitalId> changeStatus(ChangeStatusCommand command) {
        return executeManagementOperation(command, () -> {
            OperationResult<DigitalId> found = loadIdentity(command);
            if (!found.isSuccess()) return found;
            OperationResult<DigitalId> transition = found.getPayload().changeStatus(command.getTargetStatus(), clock.instant());
            if (transition.isSuccess()) repository.save(transition.getPayload());
            return transition;
        });
    }

    @Override
    public OperationResult<DigitalId> addRestriction(AddRestrictionCommand command) {
        return executeManagementOperation(command, () -> {
            OperationResult<DigitalId> found = loadIdentity(command);
            if (!found.isSuccess()) return found;
            OperationResult<DigitalId> result = found.getPayload().addRestriction(command.getRestriction(), LocalDate.now(clock));
            if (result.isSuccess()) repository.save(result.getPayload());
            return result;
        });
    }

    @Override
    public OperationResult<DigitalId> setWelfareBand(SetWelfareBandCommand command) {
        return executeManagementOperation(command, () -> {
            OperationResult<DigitalId> found = loadIdentity(command);
            if (!found.isSuccess()) return found;
            DigitalId updated = found.getPayload().withWelfareBand(command.getWelfareBand());
            repository.save(updated);
            return OperationResult.success(updated);
        });
    }

    @Override
    public OperationResult<DigitalId> findById(Organisation requestedBy, DigitalIdNumber id) {
        Optional<DomainError> authError = authorisationService.authoriseManagement(requestedBy);
        if (authError.isPresent()) return OperationResult.failure(authError.get());
        return repository.findById(id)
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure(new DomainError(ErrorCode.NOT_FOUND, "Identity not found")));
    }

    private OperationResult<DigitalId> executeManagementOperation(ManagementCommand cmd,
                                                                   Supplier<OperationResult<DigitalId>> operation) {
        Optional<DomainError> authError = authorisationService.authoriseManagement(cmd.getRequestedBy());
        if (authError.isPresent()) {
            audit(cmd.getEventType(), cmd.getRequestedBy().getOrganisationId(), false, authError.get().code().name());
            return OperationResult.failure(authError.get());
        }
        OperationResult<DigitalId> result = operation.get();
        audit(cmd.getEventType(), cmd.getRequestedBy().getOrganisationId(), result.isSuccess(),
                result.isSuccess() ? null : result.getError().code().name());
        return result;
    }

    private OperationResult<DigitalId> loadIdentity(ManagementCommand cmd) {
        return repository.findById(cmd.getDigitalIdNumber())
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure(new DomainError(ErrorCode.NOT_FOUND, "Identity not found")));
    }

    private void audit(String eventType, String actor, boolean success, String reasonCode) {
        auditPublisher.notifyObservers(new AuditEvent(eventType, actor, clock.instant(), success, reasonCode));
    }
}
