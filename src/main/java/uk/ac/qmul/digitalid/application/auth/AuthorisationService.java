package uk.ac.qmul.digitalid.application.auth;

import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.ErrorCode;

import java.util.Optional;

public final class AuthorisationService {

    public Optional<DomainError> authoriseManagement(Organisation organisation) {
        if (organisation.role() == OrganisationRole.CENTRAL_AUTHORITY) {
            return Optional.empty();
        }
        return Optional.of(new DomainError(ErrorCode.UNAUTHORISED_OPERATION, "Unauthorised operation"));
    }
}