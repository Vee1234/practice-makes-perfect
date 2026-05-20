package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationOutcome;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationRequest;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.OperationResult;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class BankPortal {

    private static final Organisation BANK = new Organisation("BANK", OrganisationRole.BANK);

    private final VerifyIdentityPort verifyPort;
    private final Clock clock;
    private final DigitalIdToKycResponseMapper projector;

    public BankPortal(VerifyIdentityPort verifyPort, Clock clock) {
        this.verifyPort = Objects.requireNonNull(verifyPort, "verifyPort is required");
        this.clock      = Objects.requireNonNull(clock,      "clock is required");
        this.projector  = new DigitalIdToKycResponseMapper();
    }

    public BankKycResponse checkBasicKyc(DigitalIdNumber id) {
        Objects.requireNonNull(id, "id is required");
        CompositeEligibilityPolicy policy = new CompositeEligibilityPolicy(
                LocalDate.now(clock),
                List.of(new ActiveStatusRule(), new NoActiveRestrictionRule(RestrictionType.FINANCIAL_REVIEW))
        );
        OperationResult<VerificationOutcome> result = verifyPort.verify(new VerificationRequest(id, BANK, policy));
        return projector.project(result);
    }
}
