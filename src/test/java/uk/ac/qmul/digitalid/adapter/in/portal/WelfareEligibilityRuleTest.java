package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.WelfareBand;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WelfareEligibilityRuleTest {

    private static final Instant    NOW        = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate  CHECK_DATE = LocalDate.of(2026, 1, 1);
    private static final DigitalIdNumber ID    = DigitalIdNumber.of("DID-000001");
    private static final LegalName  NAME       = new LegalName("Jane Doe");
    private static final LocalDate  DOB        = LocalDate.of(1990, 6, 15);

    // --- RegionMatchRule ---

    @Test
    void regionMatchRule_shouldPass_whenSubmittedRegionMatchesRegistered() {
        DigitalId identity = baseIdentity().withResidentialRegion("London");
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new RegionMatchRule("London").evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void regionMatchRule_shouldPass_caseInsensitive() {
        DigitalId identity = baseIdentity().withResidentialRegion("London");
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new RegionMatchRule("london").evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void regionMatchRule_shouldFail_withRegionMismatch_whenRegionsDoNotMatch() {
        DigitalId identity = baseIdentity().withResidentialRegion("Manchester");
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new RegionMatchRule("London").evaluate(context);

        assertThat(result).contains("REGION_MISMATCH");
    }

    @Test
    void regionMatchRule_shouldFail_withRegionNotSet_whenResidentialRegionIsNull() {
        DigitalId identity = baseIdentity();
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new RegionMatchRule("London").evaluate(context);

        assertThat(result).contains("REGION_NOT_SET");
    }

    // --- WelfareBandEligibilityRule ---

    @Test
    void welfareBandRule_shouldPass_whenBandIsInEligibleSet() {
        DigitalId identity = baseIdentity().withWelfareBand(WelfareBand.BAND_A);
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new WelfareBandEligibilityRule(Set.of(WelfareBand.BAND_A, WelfareBand.BAND_B))
                .evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    void welfareBandRule_shouldFail_withBandNotEligible_whenBandIsNotInEligibleSet() {
        DigitalId identity = baseIdentity().withWelfareBand(WelfareBand.BAND_C);
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new WelfareBandEligibilityRule(Set.of(WelfareBand.BAND_A, WelfareBand.BAND_B))
                .evaluate(context);

        assertThat(result).contains("BAND_NOT_ELIGIBLE");
    }

    @Test
    void welfareBandRule_shouldFail_withWelfareBandNotSet_whenBandIsNull() {
        DigitalId identity = baseIdentity();
        EligibilityContext context = new EligibilityContext(identity, CHECK_DATE);

        Optional<String> result = new WelfareBandEligibilityRule(Set.of(WelfareBand.BAND_A, WelfareBand.BAND_B))
                .evaluate(context);

        assertThat(result).contains("WELFARE_BAND_NOT_SET");
    }

    // --- helper ---

    private DigitalId baseIdentity() {
        return DigitalId.create(ID, NAME, DOB, NOW);
    }
}
