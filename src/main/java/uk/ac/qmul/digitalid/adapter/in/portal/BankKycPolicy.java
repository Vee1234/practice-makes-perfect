package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationPolicy;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Per-request KYC strategy. Captures submitted credentials and the check date, evaluates them
 * against the identity, and exposes individual match results so the portal can build a
 * detailed response without accessing the domain aggregate directly.
 */
public final class BankKycPolicy implements VerificationPolicy {

    private final LegalName submittedName;
    private final LocalDate submittedDateOfBirth;
    private final LocalDate checkDate;

    private boolean statusValid;
    private boolean nameMatched;
    private boolean dobMatched;
    private boolean underFinancialReview;

    public BankKycPolicy(LegalName submittedName, LocalDate submittedDateOfBirth, LocalDate checkDate) {
        this.submittedName        = Objects.requireNonNull(submittedName,        "submittedName is required");
        this.submittedDateOfBirth = Objects.requireNonNull(submittedDateOfBirth, "submittedDateOfBirth is required");
        this.checkDate            = Objects.requireNonNull(checkDate,            "checkDate is required");
    }

    @Override
    public VerificationDecision evaluate(DigitalId identity) {
        statusValid           = identity.getStatus() == IdentityStatus.ACTIVE;
        nameMatched           = identity.getCurrentLegalName().equals(submittedName);
        dobMatched            = identity.getDateOfBirth().equals(submittedDateOfBirth);
        underFinancialReview  = hasActiveFinancialReview(identity);

        return (statusValid && nameMatched && dobMatched && !underFinancialReview)
                ? VerificationDecision.VERIFIED
                : VerificationDecision.REJECTED;
    }

    private boolean hasActiveFinancialReview(DigitalId identity) {
        return identity.getRestrictions().stream()
                .anyMatch(r -> r.getType() == RestrictionType.FINANCIAL_REVIEW
                        && r.isActiveOn(checkDate));
    }

    public boolean isStatusValid()          { return statusValid; }
    public boolean isNameMatched()          { return nameMatched; }
    public boolean isDobMatched()           { return dobMatched; }
    public boolean isUnderFinancialReview() { return underFinancialReview; }
}
