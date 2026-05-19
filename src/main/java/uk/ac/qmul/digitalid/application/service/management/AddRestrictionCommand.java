package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.Restriction;

import java.util.Objects;

public final class AddRestrictionCommand {

    private final DigitalIdNumber digitalIdNumber;
    private final Restriction restriction;
    private final Organisation requestedBy;

    public AddRestrictionCommand(DigitalIdNumber digitalIdNumber, Restriction restriction, Organisation requestedBy) {
        Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        Objects.requireNonNull(restriction, "restriction is required");
        Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.digitalIdNumber = digitalIdNumber;
        this.restriction = restriction;
        this.requestedBy = requestedBy;
    }

    public DigitalIdNumber digitalIdNumber() { return digitalIdNumber; }
    public Restriction restriction()         { return restriction; }
    public Organisation requestedBy()        { return requestedBy; }
}