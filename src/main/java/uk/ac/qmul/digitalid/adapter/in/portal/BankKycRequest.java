package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.time.LocalDate;
import java.util.Objects;

public final class BankKycRequest {

    private final DigitalIdNumber digitalIdNumber;
    private final LegalName submittedName;
    private final LocalDate submittedDateOfBirth;

    public BankKycRequest(DigitalIdNumber digitalIdNumber, LegalName submittedName, LocalDate submittedDateOfBirth) {
        this.digitalIdNumber    = Objects.requireNonNull(digitalIdNumber,    "digitalIdNumber is required");
        this.submittedName      = Objects.requireNonNull(submittedName,      "submittedName is required");
        this.submittedDateOfBirth = Objects.requireNonNull(submittedDateOfBirth, "submittedDateOfBirth is required");
    }

    public DigitalIdNumber getDigitalIdNumber()    { return digitalIdNumber; }
    public LegalName getSubmittedName()            { return submittedName; }
    public LocalDate getSubmittedDateOfBirth()     { return submittedDateOfBirth; }
}
