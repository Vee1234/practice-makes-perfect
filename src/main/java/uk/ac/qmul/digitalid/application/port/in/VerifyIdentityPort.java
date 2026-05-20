package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationOutcome;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationRequest;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface VerifyIdentityPort {
    OperationResult<VerificationOutcome> verify(VerificationRequest request);
}
