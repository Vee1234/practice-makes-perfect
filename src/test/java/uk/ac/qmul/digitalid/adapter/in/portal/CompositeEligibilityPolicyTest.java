package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.application.service.consumption.EvaluationOutcome;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.Restriction;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeEligibilityPolicyTest {

    private static final Instant NOW          = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate CHECK_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate ADULT_DOB  = CHECK_DATE.minusYears(25);
    private static final DigitalIdNumber ID   = DigitalIdNumber.of("DID-000001");
    private static final LegalName NAME       = new LegalName("Jane Doe");

    private final CompositeEligibilityPolicy drivingPolicy = new CompositeEligibilityPolicy(
            CHECK_DATE,
            List.of(
                    new ActiveStatusRule(),
                    new MinimumAgeRule(),
                    new NoActiveRestrictionRule(RestrictionType.DRIVING_BAN)
            )
    );

    @Test
    void shouldReturnVerified_whenAllRulesPass() {
        EvaluationOutcome outcome = drivingPolicy.evaluate(activeAdultIdentity());

        assertThat(outcome.passed()).isTrue();
        assertThat(outcome.failingReasonCodes()).isEmpty();
    }

    @Test
    void shouldReturnRejected_whenIdentityIsSuspended() {
        DigitalId suspended = activeAdultIdentity().changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();

        EvaluationOutcome outcome = drivingPolicy.evaluate(suspended);

        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.failingReasonCodes()).contains("INACTIVE_STATUS");
    }

    @Test
    void shouldReturnRejected_whenApplicantIsBelowMinimumAge() {
        DigitalId underage = DigitalId.create(ID, NAME, CHECK_DATE.minusYears(15), NOW);

        EvaluationOutcome outcome = drivingPolicy.evaluate(underage);

        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.failingReasonCodes()).contains("MINIMUM_AGE_NOT_MET");
    }

    @Test
    void shouldReturnRejected_whenActiveDrivingBanExists() {
        EvaluationOutcome outcome = drivingPolicy.evaluate(identityWithActiveBan());

        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.failingReasonCodes()).contains(RestrictionType.DRIVING_BAN.activeReasonCode());
    }

    @Test
    void shouldReturnVerified_whenDrivingBanHasExpired() {
        EvaluationOutcome outcome = drivingPolicy.evaluate(identityWithExpiredBan());

        assertThat(outcome.passed()).isTrue();
        assertThat(outcome.failingReasonCodes()).isEmpty();
    }

    @Test
    void shouldCollectAllFailingReasons_whenMultipleRulesFail() {
        DigitalId underageSuspended = DigitalId.create(ID, NAME, CHECK_DATE.minusYears(15), NOW)
                .changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();

        EvaluationOutcome outcome = drivingPolicy.evaluate(underageSuspended);

        assertThat(outcome.failingReasonCodes()).contains("INACTIVE_STATUS", "MINIMUM_AGE_NOT_MET");
    }

    // helpers

    private DigitalId activeAdultIdentity() {
        return DigitalId.create(ID, NAME, ADULT_DOB, NOW);
    }

    private DigitalId identityWithActiveBan() {
        Restriction ban = new Restriction(RestrictionType.DRIVING_BAN, CHECK_DATE.minusDays(10), null, "BAN");
        return activeAdultIdentity().addRestriction(ban, CHECK_DATE).getPayload();
    }

    private DigitalId identityWithExpiredBan() {
        Restriction expired = new Restriction(
                RestrictionType.DRIVING_BAN, CHECK_DATE.minusDays(30), CHECK_DATE.minusDays(1), "BAN");
        return activeAdultIdentity().addRestriction(expired, CHECK_DATE).getPayload();
    }
}
