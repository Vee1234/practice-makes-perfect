package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationRequest;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.time.Clock;
import java.util.Objects;

public final class EmployerPortal {

    private static final Organisation EMPLOYER = new Organisation("EMPLOYER", OrganisationRole.EMPLOYER);

    private final VerifyIdentityPort verifyPort;
    private final Clock clock;
    private final EmployerVerificationPolicy policy;
    private final RightToWorkResponseProjector projector;

    public EmployerPortal(VerifyIdentityPort verifyPort, Clock clock) {
        this.verifyPort = Objects.requireNonNull(verifyPort, "verifyPort is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
        this.policy = new EmployerVerificationPolicy();
        this.projector = new RightToWorkResponseProjector();
    }

    public RightToWorkResponse verifyRightToWork(DigitalIdNumber id) {
        Objects.requireNonNull(id, "id is required");
        VerificationDecision decision = verifyPort.verify(new VerificationRequest(id, EMPLOYER, policy));
        return projector.project(id, decision, clock.instant());
    }
}
