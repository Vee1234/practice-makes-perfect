package uk.ac.qmul.digitalid.application.service.consumption;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.port.out.DigitalIdRepository;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.ErrorCode;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

public final class DigitalIdConsumptionService implements VerifyIdentityPort {

    private final DigitalIdRepository repository;
    private final AuditEventPublisher auditPublisher;
    private final Clock clock;

    public DigitalIdConsumptionService(DigitalIdRepository repository, AuditEventPublisher auditPublisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.auditPublisher = Objects.requireNonNull(auditPublisher, "auditPublisher is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public OperationResult<VerificationOutcome> verify(VerificationRequest request) {
        Objects.requireNonNull(request, "request is required");

        Optional<DigitalId> stored = repository.findById(request.getDigitalIdNumber());
        if (stored.isEmpty()) {
            audit(request, false, "NOT_FOUND");
            return OperationResult.failure(new DomainError(ErrorCode.NOT_FOUND, "Identity not found"));
        }

        DigitalId identity = stored.get();
        EvaluationOutcome outcome = request.getPolicy().evaluate(identity);
        audit(request, outcome.passed(), outcome.passed() ? null : "REJECTED");
        return OperationResult.success(new VerificationOutcome(identity, outcome.failingReasonCodes()));
    }

    private void audit(VerificationRequest request, boolean success, String reasonCode) {
        auditPublisher.notifyObservers(new AuditEvent(
                "VERIFY_IDENTITY",
                request.getRequestedBy().getOrganisationId(),
                clock.instant(),
                success,
                reasonCode
        ));
    }
}
