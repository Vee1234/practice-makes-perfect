package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;

import java.util.Objects;

public final class ChangeStatusCommand extends ManagementCommand {

    private final IdentityStatus targetStatus;

    public ChangeStatusCommand(DigitalIdNumber digitalIdNumber, IdentityStatus targetStatus, Organisation requestedBy) {
        super(digitalIdNumber, requestedBy);
        this.targetStatus = Objects.requireNonNull(targetStatus, "targetStatus is required");
    }

    public IdentityStatus getTargetStatus() { return targetStatus; }

    @Override
    public String getEventType() { return "CHANGE_STATUS"; }
}