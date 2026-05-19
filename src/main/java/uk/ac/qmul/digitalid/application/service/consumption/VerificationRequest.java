package uk.ac.qmul.digitalid.application.service.consumption;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.util.Objects;

public final class VerificationRequest {

    private final DigitalIdNumber digitalIdNumber;
    private final Organisation requestedBy;
    private final VerificationPolicy policy;

    public VerificationRequest(DigitalIdNumber digitalIdNumber, Organisation requestedBy, VerificationPolicy policy) {
        this.digitalIdNumber = Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.policy = Objects.requireNonNull(policy, "policy is required");
    }

    public DigitalIdNumber getDigitalIdNumber() { return digitalIdNumber; }
    public Organisation getRequestedBy()        { return requestedBy; }
    public VerificationPolicy getPolicy()       { return policy; }
}
