package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.util.Objects;

public final class IdentityUpdateCommand {

    private final DigitalIdNumber digitalIdNumber;
    private final LegalName newLegalName;
    private final Organisation requestedBy;

    public IdentityUpdateCommand(DigitalIdNumber digitalIdNumber, LegalName newLegalName, Organisation requestedBy) {
        Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        Objects.requireNonNull(newLegalName, "newLegalName is required");
        Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.digitalIdNumber = digitalIdNumber;
        this.newLegalName = newLegalName;
        this.requestedBy = requestedBy;
    }

    public DigitalIdNumber digitalIdNumber() { return digitalIdNumber; }
    public LegalName newLegalName()          { return newLegalName; }
    public Organisation requestedBy()        { return requestedBy; }
}