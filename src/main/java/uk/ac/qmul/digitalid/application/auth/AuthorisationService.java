package uk.ac.qmul.digitalid.application.auth;

import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.ErrorCode;
import uk.ac.qmul.digitalid.domain.OperationResult;

public final class AuthorisationService {

    public OperationResult<Void> authoriseManagement(ActorContext actor) {
        if (actor.role() == ActorRole.CENTRAL_AUTHORITY) {
            return OperationResult.success(null);
        }
        return OperationResult.failure(new DomainError(
                ErrorCode.UNAUTHORISED_OPERATION,
                "Unauthorised operation."));
    }
}
