package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.service.management.AddRestrictionCommand;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface AddRestrictionPort {
    OperationResult<DigitalId> addRestriction(AddRestrictionCommand command);
}
