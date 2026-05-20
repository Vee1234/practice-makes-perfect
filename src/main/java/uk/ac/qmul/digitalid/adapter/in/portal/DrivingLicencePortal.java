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

public final class DrivingLicencePortal {

    private static final Organisation DVLA = new Organisation("DVLA", OrganisationRole.DRIVING_LICENCE_AUTHORITY);

    private final VerifyIdentityPort verifyPort;
    private final Clock clock;
    private final DigitalIdToDrivingLicenceResponseMapper projector;

    public DrivingLicencePortal(VerifyIdentityPort verifyPort, Clock clock) {
        this.verifyPort = Objects.requireNonNull(verifyPort, "verifyPort is required");
        this.clock      = Objects.requireNonNull(clock,      "clock is required");
        this.projector  = new DigitalIdToDrivingLicenceResponseMapper();
    }

    public DrivingLicenceResponse checkLicenceEligibility(DigitalIdNumber id) {
        Objects.requireNonNull(id, "id is required");
        CompositeEligibilityPolicy policy = new CompositeEligibilityPolicy(
                LocalDate.now(clock),
                List.of(new ActiveStatusRule(), new MinimumAgeRule(), new NoActiveRestrictionRule(RestrictionType.DRIVING_BAN))
        );
        OperationResult<VerificationOutcome> result = verifyPort.verify(new VerificationRequest(id, DVLA, policy));
        return projector.project(result);
    }
}
