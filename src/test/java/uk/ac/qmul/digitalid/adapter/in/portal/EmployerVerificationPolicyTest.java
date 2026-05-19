package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EmployerVerificationPolicyTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final DigitalIdNumber ID = DigitalIdNumber.of("DID-000001");

    private final EmployerVerificationPolicy policy = new EmployerVerificationPolicy();

    @Test
    void shouldReturnVerified_whenIdentityIsActive() {
        assertThat(policy.evaluate(identityWithStatus(IdentityStatus.ACTIVE)))
                .isEqualTo(VerificationDecision.VERIFIED);
    }

    @Test
    void shouldReturnRejected_whenIdentityIsSuspended() {
        assertThat(policy.evaluate(identityWithStatus(IdentityStatus.SUSPENDED)))
                .isEqualTo(VerificationDecision.REJECTED);
    }

    @Test
    void shouldReturnRejected_whenIdentityIsRevoked() {
        assertThat(policy.evaluate(identityWithStatus(IdentityStatus.REVOKED)))
                .isEqualTo(VerificationDecision.REJECTED);
    }

    @Test
    void shouldReturnRejected_whenIdentityIsExpired() {
        assertThat(policy.evaluate(identityWithStatus(IdentityStatus.EXPIRED)))
                .isEqualTo(VerificationDecision.REJECTED);
    }

    private DigitalId identityWithStatus(IdentityStatus target) {
        DigitalId base = DigitalId.create(ID, new LegalName("Jane Doe"), LocalDate.of(1990, 1, 1), NOW);
        if (target == IdentityStatus.ACTIVE) return base;
        return base.changeStatus(target, NOW).getPayload();
    }
}