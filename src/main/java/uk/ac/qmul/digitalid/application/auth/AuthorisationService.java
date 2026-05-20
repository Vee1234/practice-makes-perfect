package uk.ac.qmul.digitalid.application.auth;

import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.ErrorCode;

import java.util.Optional;

public final class AuthorisationService {

    public Optional<DomainError> authoriseManagement(Organisation organisation) {
        return authorise(organisation, OrganisationRole.CENTRAL_AUTHORITY);
    }

    public Optional<DomainError> authoriseAuditQuery(Organisation organisation) {
        return authorise(organisation, OrganisationRole.AUDITOR);
    }

    private Optional<DomainError> authorise(Organisation organisation, OrganisationRole requiredRole) {
        if (organisation.getRole() == requiredRole) return Optional.empty();
        return Optional.of(new DomainError(ErrorCode.UNAUTHORISED_OPERATION, "Unauthorised operation"));
    }
}
