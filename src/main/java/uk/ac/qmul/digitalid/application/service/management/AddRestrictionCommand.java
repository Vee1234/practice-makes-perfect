package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.Restriction;

import java.util.Objects;

public final class AddRestrictionCommand extends ManagementCommand {

    private final Restriction restriction;

    public AddRestrictionCommand(DigitalIdNumber digitalIdNumber, Restriction restriction, Organisation requestedBy) {
        super(digitalIdNumber, requestedBy);
        this.restriction = Objects.requireNonNull(restriction, "restriction is required");
    }

    public Restriction getRestriction() { return restriction; }

    @Override
    public String getEventType() { return "ADD_RESTRICTION"; }
}
