package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityStatusTransitionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant LATER = Instant.parse("2026-06-01T00:00:00Z");

    private DigitalId activeId() {
        return DigitalId.create(
                DigitalIdNumber.of("DID-000001"),
                new LegalName("Jane Doe"),
                LocalDate.of(1990, 1, 1),
                NOW);
    }

    @Test
    void shouldAllowTransition_whenActiveToSuspended() {
        OperationResult<DigitalId> result = activeId().changeStatus(IdentityStatus.SUSPENDED, LATER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.SUSPENDED);
    }

    @Test
    void shouldAllowTransition_whenSuspendedToActive() {
        DigitalId suspended = activeId().changeStatus(IdentityStatus.SUSPENDED, LATER).getPayload();

        OperationResult<DigitalId> result = suspended.changeStatus(IdentityStatus.ACTIVE, LATER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.ACTIVE);
    }

    @Test
    void shouldAllowTransition_whenActiveToRevoked() {
        OperationResult<DigitalId> result = activeId().changeStatus(IdentityStatus.REVOKED, LATER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.REVOKED);
    }

    @Test
    void shouldAllowTransition_whenActiveToExpired() {
        OperationResult<DigitalId> result = activeId().changeStatus(IdentityStatus.EXPIRED, LATER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.EXPIRED);
    }

    @Test
    void shouldRejectTransition_whenRevokedToActive() {
        DigitalId revoked = activeId().changeStatus(IdentityStatus.REVOKED, LATER).getPayload();

        OperationResult<DigitalId> result = revoked.changeStatus(IdentityStatus.ACTIVE, LATER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void shouldRejectTransition_whenExpiredToSuspended() {
        DigitalId expired = activeId().changeStatus(IdentityStatus.EXPIRED, LATER).getPayload();

        OperationResult<DigitalId> result = expired.changeStatus(IdentityStatus.SUSPENDED, LATER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void shouldRejectTransition_whenRevokedToRevokedRepeatedly() {
        DigitalId revoked = activeId().changeStatus(IdentityStatus.REVOKED, LATER).getPayload();

        OperationResult<DigitalId> first = revoked.changeStatus(IdentityStatus.REVOKED, LATER);
        OperationResult<DigitalId> second = revoked.changeStatus(IdentityStatus.REVOKED, LATER);

        assertThat(first.isSuccess()).isFalse();
        assertThat(first.getError().code()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
        assertThat(second.isSuccess()).isFalse();
        assertThat(second.getError().code()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void shouldRejectTransition_whenExpiredToExpiredRepeatedly() {
        DigitalId expired = activeId().changeStatus(IdentityStatus.EXPIRED, LATER).getPayload();

        OperationResult<DigitalId> result = expired.changeStatus(IdentityStatus.EXPIRED, LATER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }
}
