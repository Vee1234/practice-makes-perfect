package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.util.Objects;

public abstract class ManagementCommand {

    private final DigitalIdNumber digitalIdNumber;
    private final Organisation requestedBy;

    protected ManagementCommand(DigitalIdNumber digitalIdNumber, Organisation requestedBy) {
        this.digitalIdNumber = Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy is required");
    }

    public DigitalIdNumber getDigitalIdNumber() { return digitalIdNumber; }
    public Organisation getRequestedBy()        { return requestedBy; }
    public abstract String getEventType();
}
