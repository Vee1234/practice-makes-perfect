package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.Restriction;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityRuleTest {

    private static final Instant NOW           = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate CHECK_DATE  = LocalDate.of(2026, 1, 1);
    private static final DigitalIdNumber ID    = DigitalIdNumber.of("DID-000001");
    private static final LegalName NAME        = new LegalName("Jane Doe");

    // --- ActiveStatusRule ---

    @Test
    void activeStatusRule_shouldPass_whenIdentityIsActive() {
        EligibilityContext context = contextWithStatus(IdentityStatus.ACTIVE);

        Optional<String> result = new ActiveStatusRule().evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void activeStatusRule_shouldFail_whenIdentityIsSuspended() {
        EligibilityContext context = contextWithStatus(IdentityStatus.SUSPENDED);

        Optional<String> result = new ActiveStatusRule().evaluate(context);

        assertThat(result).contains("INACTIVE_STATUS");
    }

    @Test
    void activeStatusRule_shouldFail_whenIdentityIsRevoked() {
        EligibilityContext context = contextWithStatus(IdentityStatus.REVOKED);

        Optional<String> result = new ActiveStatusRule().evaluate(context);

        assertThat(result).contains("INACTIVE_STATUS");
    }

    // --- MinimumAgeRule ---

    @Test
    void minimumAgeRule_shouldPass_whenApplicantIsOldEnough() {
        LocalDate dobAboveMinimum = CHECK_DATE.minusYears(20);
        EligibilityContext context = new EligibilityContext(activeIdentity(dobAboveMinimum), CHECK_DATE);

        Optional<String> result = new MinimumAgeRule().evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void minimumAgeRule_shouldFail_whenApplicantIsBelowMinimumAge() {
        LocalDate dobBelowMinimum = CHECK_DATE.minusYears(16);
        EligibilityContext context = new EligibilityContext(activeIdentity(dobBelowMinimum), CHECK_DATE);

        Optional<String> result = new MinimumAgeRule().evaluate(context);

        assertThat(result).contains("MINIMUM_AGE_NOT_MET");
    }

    @Test
    void minimumAgeRule_shouldPass_whenApplicantIsExactlyMinimumAge() {
        LocalDate dobExactlyMinimum = CHECK_DATE.minusYears(MinimumAgeRule.MINIMUM_AGE_YEARS);
        EligibilityContext context = new EligibilityContext(activeIdentity(dobExactlyMinimum), CHECK_DATE);

        Optional<String> result = new MinimumAgeRule().evaluate(context);

        assertThat(result).isEmpty();
    }

    // --- NoActiveRestrictionRule ---

    @Test
    void noActiveRestrictionRule_shouldPass_whenNoDrivingBanExists() {
        EligibilityContext context = contextWithStatus(IdentityStatus.ACTIVE);

        Optional<String> result = new NoActiveRestrictionRule(RestrictionType.DRIVING_BAN).evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void noActiveRestrictionRule_shouldFail_whenActiveDrivingBanExists() {
        DigitalId identityWithBan = identityWithActiveRestriction(RestrictionType.DRIVING_BAN);
        EligibilityContext context = new EligibilityContext(identityWithBan, CHECK_DATE);

        Optional<String> result = new NoActiveRestrictionRule(RestrictionType.DRIVING_BAN).evaluate(context);

        assertThat(result).contains("DRIVING_BAN_ACTIVE");
    }

    @Test
    void noActiveRestrictionRule_shouldPass_whenDrivingBanHasExpired() {
        DigitalId identityWithExpiredBan = identityWithExpiredRestriction(RestrictionType.DRIVING_BAN);
        EligibilityContext context = new EligibilityContext(identityWithExpiredBan, CHECK_DATE);

        Optional<String> result = new NoActiveRestrictionRule(RestrictionType.DRIVING_BAN).evaluate(context);

        assertThat(result).isEmpty();
    }

    // --- helpers ---

    private EligibilityContext contextWithStatus(IdentityStatus target) {
        DigitalId base = activeIdentity(LocalDate.of(1990, 1, 1));
        DigitalId identity = target == IdentityStatus.ACTIVE
                ? base
                : base.changeStatus(target, NOW).getPayload();
        return new EligibilityContext(identity, CHECK_DATE);
    }

    private DigitalId activeIdentity(LocalDate dob) {
        return DigitalId.create(ID, NAME, dob, NOW);
    }

    private DigitalId identityWithActiveRestriction(RestrictionType type) {
        DigitalId base = activeIdentity(LocalDate.of(1990, 1, 1));
        Restriction active = new Restriction(type, CHECK_DATE.minusDays(10), null, "TEST");
        return base.addRestriction(active, CHECK_DATE).getPayload();
    }

    private DigitalId identityWithExpiredRestriction(RestrictionType type) {
        DigitalId base = activeIdentity(LocalDate.of(1990, 1, 1));
        Restriction expired = new Restriction(type, CHECK_DATE.minusDays(30), CHECK_DATE.minusDays(1), "TEST");
        return base.addRestriction(expired, CHECK_DATE).getPayload();
    }
}
