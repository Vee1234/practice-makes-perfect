package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.time.Instant;

final class RightToWorkResponseProjector {

    RightToWorkResponse project(String digitalId, OperationResult<DigitalId> result, Instant checkedAt) {
        if (!result.isSuccess()) {
            return new RightToWorkResponse(digitalId, false, null, null, "NOT_FOUND", checkedAt);
        }
        DigitalId identity = result.getPayload();
        boolean validNow = identity.getStatus() == IdentityStatus.ACTIVE;
        String reasonCode = validNow ? null : "INACTIVE_STATUS";
        return new RightToWorkResponse(digitalId, validNow,
                identity.getCurrentLegalName().value(), identity.getDateOfBirth(), reasonCode, checkedAt);
    }
}
