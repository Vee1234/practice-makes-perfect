package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationOutcome;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.util.List;

final class DigitalIdToWelfareEligibilityResponseMapper {

    WelfareEligibilityResponse project(OperationResult<VerificationOutcome> result) {
        if (!result.isSuccess())
            return new WelfareEligibilityResponse(false, null, null, BandCategory.UNKNOWN, List.of("NOT_FOUND"));
        DigitalId identity = result.getPayload().identity();
        List<String> failingCodes = result.getPayload().failingReasonCodes();
        boolean eligible = failingCodes.isEmpty();
        BandCategory bandCategory = deriveBandCategory(failingCodes);
        return new WelfareEligibilityResponse(eligible, identity.getCurrentLegalName().value(),
                identity.getDateOfBirth(), bandCategory, failingCodes);
    }

    private BandCategory deriveBandCategory(List<String> failingCodes) {
        if (failingCodes.contains("WELFARE_BAND_NOT_SET")) return BandCategory.UNKNOWN;
        if (failingCodes.contains("BAND_NOT_ELIGIBLE"))    return BandCategory.NOT_ELIGIBLE;
        return BandCategory.ELIGIBLE;
    }
}