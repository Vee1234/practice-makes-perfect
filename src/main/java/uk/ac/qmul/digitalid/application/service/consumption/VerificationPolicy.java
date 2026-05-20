package uk.ac.qmul.digitalid.application.service.consumption;

import uk.ac.qmul.digitalid.domain.DigitalId;

@FunctionalInterface
public interface VerificationPolicy {
    EvaluationOutcome evaluate(DigitalId identity);
}
