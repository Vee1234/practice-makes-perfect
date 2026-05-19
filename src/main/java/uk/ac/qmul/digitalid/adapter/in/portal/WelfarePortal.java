package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationRequest;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.RestrictionType;
import uk.ac.qmul.digitalid.domain.WelfareBand;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class WelfarePortal {

    private static final Organisation WELFARE_AUTHORITY =
            new Organisation("DWP", OrganisationRole.WELFARE);

    private static final Set<WelfareBand> ELIGIBLE_BANDS = Set.of(WelfareBand.BAND_A, WelfareBand.BAND_B);

    private final VerifyIdentityPort verifyPort;
    private final Clock clock;
    private final WelfareEligibilityResponseProjector projector;

    public WelfarePortal(VerifyIdentityPort verifyPort, Clock clock) {
        this.verifyPort = Objects.requireNonNull(verifyPort, "verifyPort is required");
        this.clock      = Objects.requireNonNull(clock,      "clock is required");
        this.projector  = new WelfareEligibilityResponseProjector();
    }

    public WelfareEligibilityResponse checkBenefitEligibility(DigitalIdNumber id,
                                                               String submittedRegion,
                                                               LocalDate checkDate) {
        Objects.requireNonNull(id,             "id is required");
        Objects.requireNonNull(submittedRegion, "submittedRegion is required");
        Objects.requireNonNull(checkDate,      "checkDate is required");

        CompositeEligibilityPolicy policy = new CompositeEligibilityPolicy(
                checkDate,
                List.of(
                        new ActiveStatusRule(),
                        new NoActiveRestrictionRule(RestrictionType.WELFARE_REVIEW),
                        new RegionMatchRule(submittedRegion),
                        new WelfareBandEligibilityRule(ELIGIBLE_BANDS)
                )
        );

        VerificationDecision decision = verifyPort.verify(new VerificationRequest(id, WELFARE_AUTHORITY, policy));
        return projector.project(decision, policy.getFailingReasonCodes());
    }
}