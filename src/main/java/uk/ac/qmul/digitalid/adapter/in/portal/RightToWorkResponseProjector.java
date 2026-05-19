package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.time.Instant;

final class RightToWorkResponseProjector {

    RightToWorkResponse project(DigitalIdNumber id, VerificationDecision decision, Instant checkedAt) {
        boolean validNow = decision == VerificationDecision.VERIFIED;
        String reasonCode = validNow ? null : decision.name();
        return new RightToWorkResponse(id.value(), validNow, reasonCode, checkedAt);
    }
}
