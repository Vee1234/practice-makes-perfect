package uk.ac.qmul.digitalid.application.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.domain.ErrorCode;
import uk.ac.qmul.digitalid.domain.OperationResult;

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

        OperationResult<Void> result = authorisationService.authoriseManagement(organisation);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldReject_whenOrganisationIsEmployer() {
        Organisation organisation = new Organisation("EMP-001", OrganisationRole.EMPLOYER);

        OperationResult<Void> result = authorisationService.authoriseManagement(organisation);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReject_whenOrganisationIsBank() {
        Organisation organisation = new Organisation("BANK-001", OrganisationRole.BANK);

        OperationResult<Void> result = authorisationService.authoriseManagement(organisation);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReject_whenOrganisationIsAuditor() {
        Organisation organisation = new Organisation("AUD-001", OrganisationRole.AUDITOR);

        OperationResult<Void> result = authorisationService.authoriseManagement(organisation);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }
}