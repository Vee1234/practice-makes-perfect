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

class BankKycPolicyTest {

    private static final Instant NOW        = Instant.parse("2026-01-01T00:00:00Z");
    private static final DigitalIdNumber ID = DigitalIdNumber.of("DID-000001");
    private static final LegalName CORRECT_NAME = new LegalName("Jane Doe");
    private static final LocalDate CORRECT_DOB  = LocalDate.of(1990, 6, 15);

    @Test
    void shouldReturnVerified_whenStatusIsActiveAndNameAndDobMatch() {
        BankKycPolicy policy = new BankKycPolicy(CORRECT_NAME, CORRECT_DOB);
        DigitalId activeIdentity = activeIdentity(CORRECT_NAME, CORRECT_DOB);

        VerificationDecision decision = policy.evaluate(activeIdentity);

        assertThat(decision).isEqualTo(VerificationDecision.VERIFIED);
        assertThat(policy.isNameMatched()).isTrue();
        assertThat(policy.isDobMatched()).isTrue();
    }

    @Test
    void shouldReturnRejected_whenNameDoesNotMatch() {
        BankKycPolicy policy = new BankKycPolicy(new LegalName("Wrong Name"), CORRECT_DOB);
        DigitalId activeIdentity = activeIdentity(CORRECT_NAME, CORRECT_DOB);

        VerificationDecision decision = policy.evaluate(activeIdentity);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isNameMatched()).isFalse();
        assertThat(policy.isDobMatched()).isTrue();
    }

    @Test
    void shouldReturnRejected_whenDobDoesNotMatch() {
        BankKycPolicy policy = new BankKycPolicy(CORRECT_NAME, LocalDate.of(1999, 1, 1));
        DigitalId activeIdentity = activeIdentity(CORRECT_NAME, CORRECT_DOB);

        VerificationDecision decision = policy.evaluate(activeIdentity);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isNameMatched()).isTrue();
        assertThat(policy.isDobMatched()).isFalse();
    }

    @Test
    void shouldReturnRejected_whenIdentityIsSuspended_evenIfNameAndDobMatch() {
        BankKycPolicy policy = new BankKycPolicy(CORRECT_NAME, CORRECT_DOB);
        DigitalId suspendedIdentity = activeIdentity(CORRECT_NAME, CORRECT_DOB)
                .changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();

        VerificationDecision decision = policy.evaluate(suspendedIdentity);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isNameMatched()).isTrue();
        assertThat(policy.isDobMatched()).isTrue();
    }

    private DigitalId activeIdentity(LegalName name, LocalDate dob) {
        return DigitalId.create(ID, name, dob, NOW);
    }
}
