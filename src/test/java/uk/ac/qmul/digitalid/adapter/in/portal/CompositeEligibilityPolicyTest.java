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
        VerificationDecision decision = drivingPolicy.evaluate(activeAdultIdentity());

        assertThat(decision).isEqualTo(VerificationDecision.VERIFIED);
        assertThat(drivingPolicy.getFailingReasonCodes()).isEmpty();
    }

    @Test
    void shouldReturnRejected_whenIdentityIsSuspended() {
        DigitalId suspended = activeAdultIdentity().changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();

        VerificationDecision decision = drivingPolicy.evaluate(suspended);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(drivingPolicy.getFailingReasonCodes()).contains("INACTIVE_STATUS");
    }

    @Test
    void shouldReturnRejected_whenApplicantIsBelowMinimumAge() {
        DigitalId underage = DigitalId.create(ID, NAME, CHECK_DATE.minusYears(15), NOW);

        VerificationDecision decision = drivingPolicy.evaluate(underage);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(drivingPolicy.getFailingReasonCodes()).contains("MINIMUM_AGE_NOT_MET");
    }

    @Test
    void shouldReturnRejected_whenActiveDrivingBanExists() {
        DigitalId banned = identityWithActiveBan();

        VerificationDecision decision = drivingPolicy.evaluate(banned);

        assertThat(decision).isEqualTo(VerificationDecision.REJECTED);
        assertThat(drivingPolicy.getFailingReasonCodes()).contains("DRIVING_BAN_ACTIVE");
    }

    @Test
    void shouldReturnVerified_whenDrivingBanHasExpired() {
        DigitalId expiredBan = identityWithExpiredBan();

        VerificationDecision decision = drivingPolicy.evaluate(expiredBan);

        assertThat(decision).isEqualTo(VerificationDecision.VERIFIED);
        assertThat(drivingPolicy.getFailingReasonCodes()).isEmpty();
    }

    @Test
    void shouldCollectAllFailingReasons_whenMultipleRulesFail() {
        DigitalId underageSuspended = DigitalId.create(ID, NAME, CHECK_DATE.minusYears(15), NOW)
                .changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();

        drivingPolicy.evaluate(underageSuspended);

        assertThat(drivingPolicy.getFailingReasonCodes())
                .contains("INACTIVE_STATUS", "MINIMUM_AGE_NOT_MET");
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
