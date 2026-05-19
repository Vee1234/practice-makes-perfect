package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.util.List;

final class WelfareEligibilityResponseProjector {

    WelfareEligibilityResponse project(OperationResult<DigitalId> result, List<String> failingReasonCodes) {
        if (!result.isSuccess()) {
            return new WelfareEligibilityResponse(false, null, null, BandCategory.UNKNOWN, List.of("NOT_FOUND"));
        }
        DigitalId identity = result.getPayload();
        boolean eligible = failingReasonCodes.isEmpty();
        BandCategory bandCategory = deriveBandCategory(failingReasonCodes);
        return new WelfareEligibilityResponse(eligible, identity.getCurrentLegalName().value(),
                identity.getDateOfBirth(), bandCategory, failingReasonCodes);
    }

    private BandCategory deriveBandCategory(List<String> failingReasonCodes) {
        if (failingReasonCodes.contains("WELFARE_BAND_NOT_SET")) {
            return BandCategory.UNKNOWN;
        }
        if (failingReasonCodes.contains("BAND_NOT_ELIGIBLE")) {
            return BandCategory.NOT_ELIGIBLE;
        }
        return BandCategory.ELIGIBLE;
    }
}
