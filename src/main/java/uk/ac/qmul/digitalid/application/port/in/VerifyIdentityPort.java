package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationRequest;

public interface VerifyIdentityPort {
    VerificationDecision verify(VerificationRequest request);
}
