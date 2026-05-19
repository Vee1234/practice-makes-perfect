package uk.ac.qmul.digitalid.application.auth;

import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.ErrorCode;
import uk.ac.qmul.digitalid.domain.OperationResult;

public final class AuthorisationService {

    public OperationResult<Void> authoriseManagement(Organisation organisation) {
        if (organisation.role() == OrganisationRole.CENTRAL_AUTHORITY) {
            return OperationResult.success(null);
        }
        return OperationResult.failure(new DomainError(
                ErrorCode.UNAUTHORISED_OPERATION,
                "Unauthorised operation"));
    }
}