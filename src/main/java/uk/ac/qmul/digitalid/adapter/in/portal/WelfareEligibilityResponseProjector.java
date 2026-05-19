package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;

import java.util.List;

final class WelfareEligibilityResponseProjector {

    WelfareEligibilityResponse project(VerificationDecision decision, List<String> failingReasonCodes) {
        if (decision == VerificationDecision.NOT_FOUND) {
            return new WelfareEligibilityResponse(false, BandCategory.UNKNOWN, List.of("NOT_FOUND"));
        }

        boolean eligible = failingReasonCodes.isEmpty();
        BandCategory bandCategory = deriveBandCategory(failingReasonCodes);

        return new WelfareEligibilityResponse(eligible, bandCategory, failingReasonCodes);
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
