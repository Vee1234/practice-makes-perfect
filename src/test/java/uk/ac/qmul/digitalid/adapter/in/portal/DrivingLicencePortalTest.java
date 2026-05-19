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

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DrivingLicencePortalTest {

    private static final Instant NOW          = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate CHECK_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate ADULT_DOB  = CHECK_DATE.minusYears(25);
    private static final DigitalIdNumber ID   = DigitalIdNumber.of("DID-000001");
    private static final LegalName NAME       = new LegalName("Jane Doe");

    private InMemoryDigitalIdRepository repository;
    private DrivingLicencePortal portal;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(new InMemoryAuditSink());
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        VerifyIdentityPort consumptionService = new DigitalIdConsumptionService(repository, auditPublisher, clock);
        portal = new DrivingLicencePortal(consumptionService, clock);
    }

    // 15.1 — projection field-scope

    @Test
    void shouldExposeOnlyAllowedFields_inDrivingLicenceResponse() {
        Set<String> fieldNames = Arrays.stream(DrivingLicenceResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "eligible", "minimumAgeMet", "restrictionBlock", "reasonCodes");
    }

    @Test
    void shouldNotExposeRestrictionTextWelfareTaxOrImmigrationData() {
        Set<String> fieldNames = Arrays.stream(DrivingLicenceResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "name", "dateOfBirth", "nationality", "welfareBand",
                "restrictionText", "restrictionReason", "history", "status");
    }

    // 15.4 — DrivingLicencePortal facade behaviour

    @Test
    void shouldBeEligible_whenActiveAdultIdentityHasNoBan() {
        repository.save(activeAdultIdentity());

        DrivingLicenceResponse response = portal.checkLicenceEligibility(ID, CHECK_DATE);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.isMinimumAgeMet()).isTrue();
        assertThat(response.isRestrictionBlock()).isFalse();
        assertThat(response.getReasonCodes()).isEmpty();
    }

    @Test
    void shouldBeIneligible_whenActiveDrivingBanExists() {
        repository.save(identityWithActiveBan());

        DrivingLicenceResponse response = portal.checkLicenceEligibility(ID, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.isRestrictionBlock()).isTrue();
        assertThat(response.getReasonCodes()).contains("DRIVING_BAN_ACTIVE");
    }

    @Test
    void shouldBeEligible_whenDrivingBanHasExpired() {
        repository.save(identityWithExpiredBan());

        DrivingLicenceResponse response = portal.checkLicenceEligibility(ID, CHECK_DATE);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.isRestrictionBlock()).isFalse();
        assertThat(response.getReasonCodes()).isEmpty();
    }

    @Test
    void shouldBeIneligible_whenIdentityIsRevoked() {
        repository.save(activeAdultIdentity().changeStatus(IdentityStatus.REVOKED, NOW).getPayload());

        DrivingLicenceResponse response = portal.checkLicenceEligibility(ID, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getReasonCodes()).contains("INACTIVE_STATUS");
    }

    @Test
    void shouldBeIneligible_whenIdentityDoesNotExist() {
        DrivingLicenceResponse response = portal.checkLicenceEligibility(ID, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getReasonCodes()).contains("NOT_FOUND");
    }

    @Test
    void shouldBeIneligible_whenApplicantIsBelowMinimumAge() {
        repository.save(DigitalId.create(ID, NAME, CHECK_DATE.minusYears(15), NOW));

        DrivingLicenceResponse response = portal.checkLicenceEligibility(ID, CHECK_DATE);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.isMinimumAgeMet()).isFalse();
        assertThat(response.getReasonCodes()).contains("MINIMUM_AGE_NOT_MET");
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