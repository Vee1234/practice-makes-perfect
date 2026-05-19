package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;

import java.util.Objects;

public final class ChangeStatusCommand {

    private final DigitalIdNumber digitalIdNumber;
    private final IdentityStatus targetStatus;
    private final Organisation requestedBy;

    public ChangeStatusCommand(DigitalIdNumber digitalIdNumber, IdentityStatus targetStatus, Organisation requestedBy) {
        Objects.requireNonNull(digitalIdNumber, "digitalIdNumber is required");
        Objects.requireNonNull(targetStatus, "targetStatus is required");
        Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.digitalIdNumber = digitalIdNumber;
        this.targetStatus = targetStatus;
        this.requestedBy = requestedBy;
    }

    public DigitalIdNumber digitalIdNumber() { return digitalIdNumber; }
    public IdentityStatus targetStatus()     { return targetStatus; }
    public Organisation requestedBy()        { return requestedBy; }
}