package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.management.ChangeStatusCommand;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface ChangeStatusPort {
    OperationResult<DigitalId> changeStatus(ChangeStatusCommand command);
}
