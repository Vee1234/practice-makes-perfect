package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.management.SetWelfareBandCommand;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface SetWelfareBandPort {
    OperationResult<DigitalId> setWelfareBand(SetWelfareBandCommand command);
}
