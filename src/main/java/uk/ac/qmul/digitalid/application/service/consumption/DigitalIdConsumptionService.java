package uk.ac.qmul.digitalid.application.service.consumption;

import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.port.out.DigitalIdRepository;
import uk.ac.qmul.digitalid.domain.DigitalId;

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
    public VerificationDecision verify(VerificationRequest request) {
        Objects.requireNonNull(request, "request is required");

        Optional<DigitalId> found = repository.findById(request.getDigitalIdNumber());

        VerificationDecision decision = found
                .map(identity -> request.getPolicy().evaluate(identity))
                .orElse(VerificationDecision.NOT_FOUND);

        audit(request, decision);
        return decision;
    }

    private void audit(VerificationRequest request, VerificationDecision decision) {
        boolean success = decision == VerificationDecision.VERIFIED;
        String reasonCode = success ? null : decision.name();
        auditPublisher.notifyObservers(new AuditEvent(
                "VERIFY_IDENTITY",
                request.getRequestedBy().getOrganisationId(),
                clock.instant(),
                success,
                reasonCode
        ));
    }
}
