package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.time.LocalDate;
import java.util.Objects;

public final class IdentityCreateCommand extends ManagementCommand {

    private final LegalName legalName;
    private final LocalDate dateOfBirth;

    public IdentityCreateCommand(DigitalIdNumber digitalIdNumber, LegalName legalName, LocalDate dateOfBirth, Organisation requestedBy) {
        super(digitalIdNumber, requestedBy);
        this.legalName = Objects.requireNonNull(legalName, "legalName is required");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "dateOfBirth is required");
    }

    public LegalName getLegalName()   { return legalName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    @Override
    public String getEventType() { return "CREATE_IDENTITY"; }
}
