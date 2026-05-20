package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.EvaluationOutcome;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationPolicy;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.IdentityStatus;

import java.util.List;

public final class EmployerVerificationPolicy implements VerificationPolicy {

    @Override
    public EvaluationOutcome evaluate(DigitalId identity) {
        boolean active = identity.getStatus() == IdentityStatus.ACTIVE;
        return new EvaluationOutcome(active, active ? List.of() : List.of("INACTIVE_STATUS"));
    }
}
