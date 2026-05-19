package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationRequest;

import java.time.Clock;
import java.util.Objects;

public final class BankPortal {

    private static final Organisation BANK = new Organisation("BANK", OrganisationRole.BANK);

    private final VerifyIdentityPort verifyPort;
    private final Clock clock;
    private final BankKycResponseProjector projector;

    public BankPortal(VerifyIdentityPort verifyPort, Clock clock) {
        this.verifyPort = Objects.requireNonNull(verifyPort, "verifyPort is required");
        this.clock      = Objects.requireNonNull(clock, "clock is required");
        this.projector  = new BankKycResponseProjector();
    }

    public BankKycResponse checkBasicKyc(BankKycRequest request) {
        Objects.requireNonNull(request, "request is required");

        BankKycPolicy policy = new BankKycPolicy(request.getSubmittedName(), request.getSubmittedDateOfBirth());
        VerificationDecision decision = verifyPort.verify(
                new VerificationRequest(request.getDigitalIdNumber(), BANK, policy));

        return projector.project(decision, policy.isStatusValid(), policy.isNameMatched(), policy.isDobMatched());
    }
}
