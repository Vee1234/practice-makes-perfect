package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationOutcome;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.time.Instant;

final class DigitalIdToRightToWorkResponseMapper {

    RightToWorkResponse project(String requestedId, OperationResult<VerificationOutcome> result, Instant checkedAt) {
        if (!result.isSuccess())
            return new RightToWorkResponse(requestedId, false, null, null, "NOT_FOUND", checkedAt);
        DigitalId identity = result.getPayload().identity();
        boolean validNow = identity.getStatus() == IdentityStatus.ACTIVE;
        String reasonCode = validNow ? null : "INACTIVE_STATUS";
        return new RightToWorkResponse(identity.getId().value(), validNow,
                identity.getCurrentLegalName().value(), identity.getDateOfBirth(), reasonCode, checkedAt);
    }
}