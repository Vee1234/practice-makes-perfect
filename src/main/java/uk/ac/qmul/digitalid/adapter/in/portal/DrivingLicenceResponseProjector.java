package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.util.List;

final class DrivingLicenceResponseProjector {

    DrivingLicenceResponse project(OperationResult<DigitalId> result, List<String> failingReasonCodes) {
        if (!result.isSuccess()) {
            return new DrivingLicenceResponse(false, false, false, null, null, List.of("NOT_FOUND"));
        }
        DigitalId identity = result.getPayload();
        boolean eligible         = failingReasonCodes.isEmpty();
        boolean minimumAgeMet    = !failingReasonCodes.contains("MINIMUM_AGE_NOT_MET");
        boolean restrictionBlock = failingReasonCodes.contains("DRIVING_BAN_ACTIVE");
        return new DrivingLicenceResponse(eligible, minimumAgeMet, restrictionBlock,
                identity.getCurrentLegalName().value(), identity.getDateOfBirth(), failingReasonCodes);
    }
}
