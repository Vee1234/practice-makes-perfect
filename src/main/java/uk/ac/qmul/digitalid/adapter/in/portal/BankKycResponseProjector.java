package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;

import java.util.ArrayList;
import java.util.List;

final class BankKycResponseProjector {

    BankKycResponse project(VerificationDecision decision,
                            boolean statusValid, boolean nameMatched, boolean dobMatched) {
        List<String> reasonCodes = buildReasonCodes(decision, statusValid, nameMatched, dobMatched);
        KycDecision kycDecision = reasonCodes.isEmpty() ? KycDecision.PASS : KycDecision.FAIL;
        return new BankKycResponse(statusValid, nameMatched, dobMatched, kycDecision, reasonCodes);
    }

    private List<String> buildReasonCodes(VerificationDecision decision,
                                          boolean statusValid, boolean nameMatched, boolean dobMatched) {
        if (decision == VerificationDecision.NOT_FOUND) {
            return List.of("NOT_FOUND");
        }
        List<String> reasons = new ArrayList<>();
        if (!statusValid) reasons.add("INVALID_STATUS");
        if (!nameMatched) reasons.add("NAME_MISMATCH");
        if (!dobMatched)  reasons.add("DOB_MISMATCH");
        return reasons;
    }
}
