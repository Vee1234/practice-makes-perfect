package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationDecision;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.Restriction;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BankKycPolicyTest {

    private static final Instant NOW           = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate CHECK_DATE  = LocalDate.of(2026, 1, 1);
    private static final DigitalIdNumber ID    = DigitalIdNumber.of("DID-000001");
    private static final LegalName CORRECT_NAME = new LegalName("Jane Doe");
    private static final LocalDate CORRECT_DOB  = LocalDate.of(1990, 6, 15);

    @Test
    void shouldReturnVerified_whenStatusIsActiveAndCredentialsMatchAndNoFinancialReview() {
        BankKycPolicy policy = policyWithCorrectCredentials();

        VerificationDecision decision = policy.evaluate(activeIdentity(CORRECT_NAME, CORRECT_DOB));

        assertThat(decision).isEqualTo(VerificationDecision.VERIFIED);
        assertThat(policy.isStatusValid()).isTrue();
        assertThat(policy.isNameMatched()).isTrue();
        assertThat(policy.isDobMatched()).isTrue();
        assertThat(policy.isUnderFinancialReview()).isFalse();
    }

    @Test
    void shouldReturnRejected_whenNameDoesNotMatch() {
        BankKycPolicy policy = new BankKycPolicy(new LegalName("Wrong Name"), CORRECT_DOB, CHECK_DATE);

        VerificationDecision decision = policy.evaluate(activeIdentity(CORRECT_NAME, CORRECT_DOB));

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isNameMatched()).isFalse();
        assertThat(policy.isDobMatched()).isTrue();
    }

    @Test
    void shouldReturnRejected_whenDobDoesNotMatch() {
        BankKycPolicy policy = new BankKycPolicy(CORRECT_NAME, LocalDate.of(1999, 1, 1), CHECK_DATE);

        VerificationDecision decision = policy.evaluate(activeIdentity(CORRECT_NAME, CORRECT_DOB));

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isNameMatched()).isTrue();
        assertThat(policy.isDobMatched()).isFalse();
    }

    @Test
    void shouldReturnRejected_whenIdentityIsSuspended_evenIfCredentialsMatch() {
        BankKycPolicy policy = policyWithCorrectCredentials();
        DigitalId suspended = activeIdentity(CORRECT_NAME, CORRECT_DOB)
                .changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();

        VerificationDecision decision = policy.evaluate(suspended);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isStatusValid()).isFalse();
    }

    @Test
    void shouldReturnRejected_whenActiveFinancialReviewRestrictionExists() {
        BankKycPolicy policy = policyWithCorrectCredentials();
        DigitalId identityUnderReview = identityWithActiveFinancialReview();

        VerificationDecision decision = policy.evaluate(identityUnderReview);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(policy.isUnderFinancialReview()).isTrue();
        assertThat(policy.isStatusValid()).isTrue();
        assertThat(policy.isNameMatched()).isTrue();
        assertThat(policy.isDobMatched()).isTrue();
    }

    @Test
    void shouldReturnVerified_whenFinancialReviewRestrictionHasExpired() {
        BankKycPolicy policy = policyWithCorrectCredentials();
        DigitalId identityWithExpiredReview = identityWithExpiredFinancialReview();

        VerificationDecision decision = policy.evaluate(identityWithExpiredReview);

        assertThat(decision).isEqualTo(VerificationDecision.VERIFIED);
        assertThat(policy.isUnderFinancialReview()).isFalse();
    }

    // helpers

    private BankKycPolicy policyWithCorrectCredentials() {
        return new BankKycPolicy(CORRECT_NAME, CORRECT_DOB, CHECK_DATE);
    }

    private DigitalId activeIdentity(LegalName name, LocalDate dob) {
        return DigitalId.create(ID, name, dob, NOW);
    }

    private DigitalId identityWithActiveFinancialReview() {
        DigitalId base = activeIdentity(CORRECT_NAME, CORRECT_DOB);
        Restriction activeReview = new Restriction(
                RestrictionType.FINANCIAL_REVIEW,
                CHECK_DATE.minusDays(30),
                null,
                "UNDER_REVIEW");
        return base.addRestriction(activeReview, CHECK_DATE).getPayload();
    }

    private DigitalId identityWithExpiredFinancialReview() {
        DigitalId base = activeIdentity(CORRECT_NAME, CORRECT_DOB);
        Restriction expiredReview = new Restriction(
                RestrictionType.FINANCIAL_REVIEW,
                CHECK_DATE.minusDays(60),
                CHECK_DATE.minusDays(1),
                "RESOLVED");
        return base.addRestriction(expiredReview, CHECK_DATE).getPayload();
    }
}
