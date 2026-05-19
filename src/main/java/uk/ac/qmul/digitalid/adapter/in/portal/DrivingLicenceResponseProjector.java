package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;

import java.util.List;

final class DrivingLicenceResponseProjector {

    DrivingLicenceResponse project(VerificationDecision decision, List<String> failingReasonCodes) {
        if (decision == VerificationDecision.NOT_FOUND) {
            return new DrivingLicenceResponse(false, false, false, List.of("NOT_FOUND"));
        }

        boolean eligible        = failingReasonCodes.isEmpty();
        boolean minimumAgeMet   = !failingReasonCodes.contains("MINIMUM_AGE_NOT_MET");
        boolean restrictionBlock = failingReasonCodes.contains("DRIVING_BAN_ACTIVE");

        return new DrivingLicenceResponse(eligible, minimumAgeMet, restrictionBlock, failingReasonCodes);
    }
}
