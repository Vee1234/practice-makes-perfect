package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationPolicy;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Per-request KYC strategy. Captures submitted credentials, evaluates them against the identity,
 * and exposes individual match results so the portal can build a detailed response.
 */
public final class BankKycPolicy implements VerificationPolicy {

    private final LegalName submittedName;
    private final LocalDate submittedDateOfBirth;

    private boolean statusValid;
    private boolean nameMatched;
    private boolean dobMatched;

    public BankKycPolicy(LegalName submittedName, LocalDate submittedDateOfBirth) {
        this.submittedName        = Objects.requireNonNull(submittedName, "submittedName is required");
        this.submittedDateOfBirth = Objects.requireNonNull(submittedDateOfBirth, "submittedDateOfBirth is required");
    }

    @Override
    public VerificationDecision evaluate(DigitalId identity) {
        statusValid = identity.getStatus() == IdentityStatus.ACTIVE;
        nameMatched = identity.getCurrentLegalName().equals(submittedName);
        dobMatched  = identity.getDateOfBirth().equals(submittedDateOfBirth);

        return (statusValid && nameMatched && dobMatched)
                ? VerificationDecision.VERIFIED
                : VerificationDecision.REJECTED;
    }

    public boolean isStatusValid() { return statusValid; }
    public boolean isNameMatched() { return nameMatched; }
    public boolean isDobMatched()  { return dobMatched; }
}
