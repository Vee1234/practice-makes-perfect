package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationOutcome;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.util.List;

final class DigitalIdToDrivingLicenceResponseMapper {

    DrivingLicenceResponse project(OperationResult<VerificationOutcome> result) {
        if (!result.isSuccess())
            return new DrivingLicenceResponse(false, false, false, null, null, List.of("NOT_FOUND"));
        DigitalId identity = result.getPayload().identity();
        List<String> failingCodes = result.getPayload().failingReasonCodes();
        boolean eligible         = failingCodes.isEmpty();
        boolean minimumAgeMet    = !failingCodes.contains("MINIMUM_AGE_NOT_MET");
        boolean restrictionBlock = failingCodes.contains(RestrictionType.DRIVING_BAN.activeReasonCode());
        return new DrivingLicenceResponse(eligible, minimumAgeMet, restrictionBlock,
                identity.getCurrentLegalName().value(), identity.getDateOfBirth(), failingCodes);
    }
}