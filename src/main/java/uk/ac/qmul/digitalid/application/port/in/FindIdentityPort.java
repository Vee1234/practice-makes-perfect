package uk.ac.qmul.digitalid.application.port.in;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.OperationResult;

public interface FindIdentityPort {
    OperationResult<DigitalId> findById(Organisation requestedBy, DigitalIdNumber id);
}