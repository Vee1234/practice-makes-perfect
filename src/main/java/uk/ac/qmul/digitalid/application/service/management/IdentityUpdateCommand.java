package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.util.Objects;

public final class IdentityUpdateCommand extends ManagementCommand {

    private final LegalName newLegalName;

    public IdentityUpdateCommand(DigitalIdNumber digitalIdNumber, LegalName newLegalName, Organisation requestedBy) {
        super(digitalIdNumber, requestedBy);
        this.newLegalName = Objects.requireNonNull(newLegalName, "newLegalName is required");
    }

    public LegalName getNewLegalName() { return newLegalName; }

    @Override
    public String getEventType() { return "UPDATE_IDENTITY"; }
}
