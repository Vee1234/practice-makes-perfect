package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.management.IdentityUpdateCommand;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface UpdateIdentityPort {
    OperationResult<DigitalId> updateMutableAttributes(IdentityUpdateCommand command);
}
