package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.time.LocalDate;
import java.util.Objects;

public final class IdentityCreateCommand {

    private final DigitalIdNumber digitalIdNumber;
    private final LegalName legalName;
    private final LocalDate dateOfBirth;
    private final Organisation requestedBy;

    public IdentityCreateCommand(DigitalIdNumber digitalIdNumber, LegalName legalName, LocalDate dateOfBirth, Organisation requestedBy) {
        Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        Objects.requireNonNull(legalName, "legalName is required");
        Objects.requireNonNull(dateOfBirth, "dateOfBirth is required");
        Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.digitalIdNumber = digitalIdNumber;
        this.legalName = legalName;
        this.dateOfBirth = dateOfBirth;
        this.requestedBy = requestedBy;
    }

    public DigitalIdNumber digitalIdNumber() { return digitalIdNumber; }
    public LegalName legalName()             { return legalName; }
    public LocalDate dateOfBirth()           { return dateOfBirth; }
    public Organisation requestedBy()        { return requestedBy; }
}