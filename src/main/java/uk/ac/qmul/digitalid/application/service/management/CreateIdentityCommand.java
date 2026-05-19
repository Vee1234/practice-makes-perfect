package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;

import java.time.LocalDate;
import java.util.Objects;

public final class CreateIdentityCommand {

    private final String digitalIdNumber;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final Organisation requestedBy;

    public CreateIdentityCommand(String digitalIdNumber, String fullName, LocalDate dateOfBirth, Organisation requestedBy) {
        Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        Objects.requireNonNull(fullName, "fullName is required");
        Objects.requireNonNull(dateOfBirth, "dateOfBirth is required");
        Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.digitalIdNumber = digitalIdNumber;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.requestedBy = requestedBy;
    }

    public String digitalIdNumber()    { return digitalIdNumber; }
    public String fullName()           { return fullName; }
    public LocalDate dateOfBirth()     { return dateOfBirth; }
    public Organisation requestedBy()  { return requestedBy; }
}