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
    void shouldAuthorise_whenActorIsCentralAuthority() {
        ActorContext actor = new ActorContext("CA-001", ActorRole.CENTRAL_AUTHORITY);

        OperationResult<Void> result = authorisationService.authoriseManagement(actor);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldReject_whenActorIsEmployer() {
        ActorContext actor = new ActorContext("EMP-001", ActorRole.EMPLOYER);

        OperationResult<Void> result = authorisationService.authoriseManagement(actor);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReject_whenActorIsBank() {
        ActorContext actor = new ActorContext("BANK-001", ActorRole.BANK);

        OperationResult<Void> result = authorisationService.authoriseManagement(actor);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReject_whenActorIsAuditor() {
        ActorContext actor = new ActorContext("AUD-001", ActorRole.AUDITOR);

        OperationResult<Void> result = authorisationService.authoriseManagement(actor);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }
}