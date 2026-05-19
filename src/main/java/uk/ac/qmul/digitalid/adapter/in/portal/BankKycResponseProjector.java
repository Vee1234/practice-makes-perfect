package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.domain.IdentityStatus;

import java.util.ArrayList;
import java.util.List;

final class BankKycResponseProjector {

    BankKycResponse project(VerificationDecision decision, boolean nameMatched,
                            boolean dobMatched, boolean statusValid) {
        List<String> reasonCodes = buildReasonCodes(decision, nameMatched, dobMatched, statusValid);
        KycDecision kycDecision = reasonCodes.isEmpty() ? KycDecision.PASS : KycDecision.FAIL;

        return new BankKycResponse(statusValid, nameMatched, dobMatched, kycDecision, reasonCodes);
    }

    private List<String> buildReasonCodes(VerificationDecision decision, boolean nameMatched,
                                          boolean dobMatched, boolean statusValid) {
        List<String> reasons = new ArrayList<>();

        if (decision == VerificationDecision.NOT_FOUND) {
            reasons.add("NOT_FOUND");
            return reasons;
        }
        if (!statusValid)   reasons.add(IdentityStatus.SUSPENDED.name());
        if (!nameMatched)   reasons.add("NAME_MISMATCH");
        if (!dobMatched)    reasons.add("DOB_MISMATCH");

        return reasons;
    }
}
