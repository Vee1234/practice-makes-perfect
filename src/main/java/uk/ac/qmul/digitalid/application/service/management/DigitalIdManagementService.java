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
        Optional<DomainError> auth = authorisationService.authoriseManagement(command.getRequestedBy());
        if (auth.isPresent()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, auth.get().code().name());
            return OperationResult.failure(auth.get());
        }

        if (repository.exists(command.getDigitalIdNumber())) {
            DomainError error = new DomainError(ErrorCode.DUPLICATE_ID, "Identity already exists");
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        DigitalId digitalId = DigitalId.create(command.getDigitalIdNumber(), command.getLegalName(),
                command.getDateOfBirth(), clock.instant());
        repository.save(digitalId);
        audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), true, null);

        return OperationResult.success(digitalId);
    }

    @Override
    public OperationResult<DigitalId> updateMutableAttributes(IdentityUpdateCommand command) {
        Optional<DomainError> auth = authorisationService.authoriseManagement(command.getRequestedBy());
        if (auth.isPresent()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, auth.get().code().name());
            return OperationResult.failure(auth.get());
        }

        DigitalId existing = repository.findById(command.getDigitalIdNumber()).orElse(null);
        if (existing == null) {
            DomainError error = new DomainError(ErrorCode.NOT_FOUND, "Identity not found");
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        if (existing.getStatus() == IdentityStatus.REVOKED || existing.getStatus() == IdentityStatus.EXPIRED) {
            DomainError error = new DomainError(ErrorCode.NOT_MODIFIABLE, "Identity is in a terminal state and cannot be modified");
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        DigitalId updated = existing.updateLegalName(command.getNewLegalName(), clock.instant());
        repository.save(updated);
        audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), true, null);

        return OperationResult.success(updated);
    }

    @Override
    public OperationResult<DigitalId> changeStatus(ChangeStatusCommand command) {
        Optional<DomainError> auth = authorisationService.authoriseManagement(command.getRequestedBy());
        if (auth.isPresent()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, auth.get().code().name());
            return OperationResult.failure(auth.get());
        }

        DigitalId existing = repository.findById(command.getDigitalIdNumber()).orElse(null);
        if (existing == null) {
            DomainError error = new DomainError(ErrorCode.NOT_FOUND, "Identity not found");
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        OperationResult<DigitalId> transition = existing.changeStatus(command.getTargetStatus(), clock.instant());
        if (!transition.isSuccess()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, transition.getError().code().name());
            return transition;
        }

        repository.save(transition.getPayload());
        audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), true, null);
        return transition;
    }

    @Override
    public OperationResult<DigitalId> addRestriction(AddRestrictionCommand command) {
        Optional<DomainError> auth = authorisationService.authoriseManagement(command.getRequestedBy());
        if (auth.isPresent()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, auth.get().code().name());
            return OperationResult.failure(auth.get());
        }

        DigitalId existing = repository.findById(command.getDigitalIdNumber()).orElse(null);
        if (existing == null) {
            DomainError error = new DomainError(ErrorCode.NOT_FOUND, "Identity not found");
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        OperationResult<DigitalId> result = existing.addRestriction(command.getRestriction(), LocalDate.now(clock));
        if (!result.isSuccess()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, result.getError().code().name());
            return result;
        }

        repository.save(result.getPayload());
        audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), true, null);
        return result;
    }

    @Override
    public OperationResult<DigitalId> setWelfareBand(SetWelfareBandCommand command) {
        Optional<DomainError> auth = authorisationService.authoriseManagement(command.getRequestedBy());
        if (auth.isPresent()) {
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, auth.get().code().name());
            return OperationResult.failure(auth.get());
        }

        DigitalId existing = repository.findById(command.getDigitalIdNumber()).orElse(null);
        if (existing == null) {
            DomainError error = new DomainError(ErrorCode.NOT_FOUND, "Identity not found");
            audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), false, error.code().name());
            return OperationResult.failure(error);
        }

        DigitalId updated = existing.withWelfareBand(command.getWelfareBand());
        repository.save(updated);
        audit(command.getEventType(), command.getRequestedBy().getOrganisationId(), true, null);
        return OperationResult.success(updated);
    }

    @Override
    public OperationResult<DigitalId> findById(Organisation requestedBy, DigitalIdNumber id) {
        Optional<DomainError> auth = authorisationService.authoriseManagement(requestedBy);
        if (auth.isPresent()) {
            return OperationResult.failure(auth.get());
        }

        return repository.findById(id)
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure(
                        new DomainError(ErrorCode.NOT_FOUND, "Identity not found")));
    }

    private void audit(String eventType, String actor, boolean success, String reasonCode) {
        auditPublisher.notifyObservers(new AuditEvent(eventType, actor, clock.instant(), success, reasonCode));
    }
}
