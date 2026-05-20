package uk.ac.qmul.digitalid.application.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.domain.DomainError;
import uk.ac.qmul.digitalid.domain.ErrorCode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorisationServiceTest {

    private AuthorisationService authorisationService;

    @BeforeEach
    void setUp() {
        authorisationService = new AuthorisationService();
    }

    @Test
    void shouldAuthorise_whenOrganisationIsCentralAuthority() {
        Organisation organisation = new Organisation("CA-001", OrganisationRole.CENTRAL_AUTHORITY);

        Optional<DomainError> result = authorisationService.authoriseManagement(organisation);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReject_whenOrganisationIsEmployer() {
        Organisation organisation = new Organisation("EMP-001", OrganisationRole.EMPLOYER);

        Optional<DomainError> result = authorisationService.authoriseManagement(organisation);

        assertThat(result).isPresent();
        assertThat(result.get().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReject_whenOrganisationIsBank() {
        Organisation organisation = new Organisation("BANK-001", OrganisationRole.BANK);

        Optional<DomainError> result = authorisationService.authoriseManagement(organisation);

        assertThat(result).isPresent();
        assertThat(result.get().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReject_whenOrganisationIsAuditor() {
        Organisation organisation = new Organisation("AUD-001", OrganisationRole.AUDITOR);

        Optional<DomainError> result = authorisationService.authoriseManagement(organisation);

        assertThat(result).isPresent();
        assertThat(result.get().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    // authoriseAuditQuery

    @Test
    void shouldAuthoriseAuditQuery_whenOrganisationIsAuditor() {
        Organisation organisation = new Organisation("AUD-001", OrganisationRole.AUDITOR);

        Optional<DomainError> result = authorisationService.authoriseAuditQuery(organisation);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectAuditQuery_whenOrganisationIsEmployer() {
        Organisation organisation = new Organisation("EMP-001", OrganisationRole.EMPLOYER);

        Optional<DomainError> result = authorisationService.authoriseAuditQuery(organisation);

        assertThat(result).isPresent();
        assertThat(result.get().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }
}
