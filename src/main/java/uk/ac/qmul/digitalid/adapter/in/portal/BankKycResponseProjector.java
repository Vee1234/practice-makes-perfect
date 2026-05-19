package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.util.List;

final class BankKycResponseProjector {

    BankKycResponse project(OperationResult<DigitalId> result, List<String> failingReasonCodes) {
        if (!result.isSuccess()) {
            return new BankKycResponse(false, null, null, KycDecision.FAIL, List.of("NOT_FOUND"));
        }
        DigitalId identity = result.getPayload();
        boolean validNow = !failingReasonCodes.contains("INACTIVE_STATUS");
        KycDecision kycDecision = failingReasonCodes.isEmpty() ? KycDecision.PASS : KycDecision.FAIL;
        return new BankKycResponse(validNow, identity.getCurrentLegalName().value(),
                identity.getDateOfBirth(), kycDecision, failingReasonCodes);
    }
}
