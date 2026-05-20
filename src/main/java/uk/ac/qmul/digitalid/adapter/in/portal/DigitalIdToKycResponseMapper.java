package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationOutcome;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.util.List;

final class DigitalIdToKycResponseMapper {

    BankKycResponse project(OperationResult<VerificationOutcome> result) {
        if (!result.isSuccess())
            return new BankKycResponse(false, null, null, KycDecision.FAIL, List.of("NOT_FOUND"));
        DigitalId identity = result.getPayload().identity();
        List<String> failingCodes = result.getPayload().failingReasonCodes();
        boolean validNow = !failingCodes.contains("INACTIVE_STATUS");
        KycDecision kycDecision = failingCodes.isEmpty() ? KycDecision.PASS : KycDecision.FAIL;
        return new BankKycResponse(validNow, identity.getCurrentLegalName().value(),
                identity.getDateOfBirth(), kycDecision, failingCodes);
    }
}
