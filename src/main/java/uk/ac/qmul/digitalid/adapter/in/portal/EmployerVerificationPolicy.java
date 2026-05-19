package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationPolicy;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.IdentityStatus;

public final class EmployerVerificationPolicy implements VerificationPolicy {

    @Override
    public boolean evaluate(DigitalId identity) {
        return identity.getStatus() == IdentityStatus.ACTIVE;
    }
}
