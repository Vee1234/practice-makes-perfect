package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;

import java.util.List;

final class BankKycResponseProjector {

    BankKycResponse project(VerificationDecision decision, List<String> failingReasonCodes) {
        if (decision == VerificationDecision.NOT_FOUND) {
            return new BankKycResponse(false, true, true, KycDecision.FAIL, List.of("NOT_FOUND"));
        }
        boolean validNow = !failingReasonCodes.contains("INACTIVE_STATUS");
        KycDecision kycDecision = failingReasonCodes.isEmpty() ? KycDecision.PASS : KycDecision.FAIL;
        return new BankKycResponse(validNow, true, true, kycDecision, failingReasonCodes);
    }
}
