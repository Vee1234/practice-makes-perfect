package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.adapter.out.persistence.InMemoryDigitalIdRepository;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.port.in.VerifyIdentityPort;
import uk.ac.qmul.digitalid.application.service.consumption.DigitalIdConsumptionService;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.Restriction;
import uk.ac.qmul.digitalid.domain.RestrictionType;
import uk.ac.qmul.digitalid.domain.WelfareBand;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class WelfarePortalTest {

    private static final Instant    NOW          = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate  CHECK_DATE   = LocalDate.of(2026, 1, 1);
    private static final String     REGION       = "London";
    private static final DigitalIdNumber ID      = DigitalIdNumber.of("DID-000001");
    private static final LegalName  NAME         = new LegalName("Jane Doe");
    private static final LocalDate  ADULT_DOB    = CHECK_DATE.minusYears(30);

    private InMemoryDigitalIdRepository repository;
    private WelfarePortal portal;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(new InMemoryAuditSink());
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        VerifyIdentityPort consumptionService = new DigitalIdConsumptionService(repository, auditPublisher, clock);
        portal = new WelfarePortal(consumptionService, clock);
    }

    // 16.1 — WelfareEligibilityResponse must expose only the fields the welfare authority is authorised to receive

    @Test
    void shouldExposeOnlyAllowedFields_inWelfareEligibilityResponse() {
        Set<String> fieldNames = Arrays.stream(WelfareEligibilityResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "eligible", "regionMatched", "bandCategory", "reasonCodes");
    }

    @Test
    void shouldNotExposeTaxHistoryBankFlagsOrFullStatusHistory_inWelfareEligibilityResponse() {
        Set<String> fieldNames = Arrays.stream(WelfareEligibilityResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "name", "dateOfBirth", "welfareBand", "taxHistory",
                "bankFlags", "history", "nationality", "restrictions");
    }

    // 16.3 — WelfarePortal facade behaviour

    @Test
    void shouldBeEligible_whenActiveIdentityMatchesRegionAndHasEligibleBand() {
        repository.save(eligibleIdentity());

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.isRegionMatched()).isTrue();
        assertThat(response.getBandCategory()).isEqualTo(BandCategory.ELIGIBLE);
        assertThat(response.getReasonCodes()).isEmpty();
    }

    @Test
    void shouldBeIneligible_withNotFound_whenIdentityDoesNotExist() {
        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getBandCategory()).isEqualTo(BandCategory.UNKNOWN);
        assertThat(response.getReasonCodes()).contains("NOT_FOUND");
    }

    @Test
    void shouldBeIneligible_whenIdentityIsSuspended() {
        DigitalId suspended = baseIdentityWithRegionAndBand()
                .changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload();
        repository.save(suspended);

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getReasonCodes()).contains("INACTIVE_STATUS");
    }

    @Test
    void shouldBeIneligible_withRegionMismatch_whenSubmittedRegionDiffers() {
        repository.save(eligibleIdentity());

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, "Manchester", CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.isRegionMatched()).isFalse();
        assertThat(response.getReasonCodes()).contains("REGION_MISMATCH");
    }

    @Test
    void shouldBeIneligible_withRegionNotSet_whenIdentityHasNoResidentialRegion() {
        DigitalId noRegion = DigitalId.create(ID, NAME, ADULT_DOB, NOW)
                .withWelfareBand(WelfareBand.BAND_A);
        repository.save(noRegion);

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.isRegionMatched()).isFalse();
        assertThat(response.getReasonCodes()).contains("REGION_NOT_SET");
    }

    @Test
    void shouldBeIneligible_withBandNotEligible_whenIdentityHasBandC() {
        DigitalId bandC = DigitalId.create(ID, NAME, ADULT_DOB, NOW)
                .withResidentialRegion(REGION)
                .withWelfareBand(WelfareBand.BAND_C);
        repository.save(bandC);

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getBandCategory()).isEqualTo(BandCategory.NOT_ELIGIBLE);
        assertThat(response.getReasonCodes()).contains("BAND_NOT_ELIGIBLE");
    }

    @Test
    void shouldBeIneligible_withWelfareBandNotSet_whenBandIsNull() {
        DigitalId noBand = DigitalId.create(ID, NAME, ADULT_DOB, NOW)
                .withResidentialRegion(REGION);
        repository.save(noBand);

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getBandCategory()).isEqualTo(BandCategory.UNKNOWN);
        assertThat(response.getReasonCodes()).contains("WELFARE_BAND_NOT_SET");
    }

    @Test
    void shouldBeIneligible_whenActiveWelfareReviewRestrictionExists() {
        Restriction welfareReview = new Restriction(
                RestrictionType.WELFARE_REVIEW, CHECK_DATE.minusDays(5), null, "AUDIT");
        DigitalId restricted = eligibleIdentity().addRestriction(welfareReview, CHECK_DATE).getPayload();
        repository.save(restricted);

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getReasonCodes()).contains("WELFARE_REVIEW_ACTIVE");
    }

    @Test
    void shouldBeEligible_whenWelfareReviewRestrictionHasExpired() {
        Restriction expired = new Restriction(
                RestrictionType.WELFARE_REVIEW, CHECK_DATE.minusDays(30), CHECK_DATE.minusDays(1), "AUDIT");
        DigitalId identity = eligibleIdentity().addRestriction(expired, CHECK_DATE).getPayload();
        repository.save(identity);

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(ID, REGION, CHECK_DATE);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.getReasonCodes()).isEmpty();
    }

    // helpers

    private DigitalId baseIdentityWithRegionAndBand() {
        return DigitalId.create(ID, NAME, ADULT_DOB, NOW)
                .withResidentialRegion(REGION)
                .withWelfareBand(WelfareBand.BAND_A);
    }

    private DigitalId eligibleIdentity() {
        return baseIdentityWithRegionAndBand();
    }
}
