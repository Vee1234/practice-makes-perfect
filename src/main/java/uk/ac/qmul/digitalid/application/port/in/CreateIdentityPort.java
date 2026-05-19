package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.management.IdentityCreateCommand;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface CreateIdentityPort {
    OperationResult<DigitalId> createIdentity(IdentityCreateCommand command);
}
