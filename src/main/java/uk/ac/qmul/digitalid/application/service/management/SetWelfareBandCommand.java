package uk.ac.qmul.digitalid.application.service.management;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.WelfareBand;

import java.util.Objects;

public final class SetWelfareBandCommand extends ManagementCommand {

    private final WelfareBand welfareBand;

    public SetWelfareBandCommand(DigitalIdNumber digitalIdNumber, WelfareBand welfareBand, Organisation requestedBy) {
        super(digitalIdNumber, requestedBy);
        this.welfareBand = Objects.requireNonNull(welfareBand, "welfareBand is required");
    }

    public WelfareBand getWelfareBand() { return welfareBand; }

    @Override
    public String getEventType() { return "SET_WELFARE_BAND"; }
}
