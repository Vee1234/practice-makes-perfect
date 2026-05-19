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

class BankPortalTest {

    private static final Instant NOW           = Instant.parse("2026-01-01T00:00:00Z");
    private static final LocalDate CHECK_DATE  = LocalDate.ofInstant(NOW, ZoneOffset.UTC);
    private static final DigitalIdNumber ID    = DigitalIdNumber.of("DID-000001");
    private static final LegalName NAME        = new LegalName("Jane Doe");
    private static final LocalDate DOB         = LocalDate.of(1990, 6, 15);

    private InMemoryDigitalIdRepository repository;
    private BankPortal portal;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(new InMemoryAuditSink());
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        VerifyIdentityPort consumptionService = new DigitalIdConsumptionService(repository, auditPublisher, clock);
        portal = new BankPortal(consumptionService, clock);
    }

    // 13.1 — projection field-scope

    @Test
    void shouldExposeOnlyAllowedFields_inBankKycResponse() {
        Set<String> fieldNames = Arrays.stream(BankKycResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "validNow", "name", "dateOfBirth", "kycDecision", "reasonCodes");
    }

    @Test
    void shouldNotExposeRawProfileWelfareOrImmigrationData_inBankKycResponse() {
        Set<String> fieldNames = Arrays.stream(BankKycResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "digitalId", "history", "nationality",
                "welfareBand", "restrictions", "residentialRegion",
                "nameMatched", "dobMatched");
    }

    // 13.3 — BankPortal facade behaviour

    @Test
    void shouldPassKyc_whenIdentityIsActive() {
        repository.save(DigitalId.create(ID, NAME, DOB, NOW));

        BankKycResponse response = portal.checkBasicKyc(ID);

        assertThat(response.isValidNow()).isTrue();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.PASS);
        assertThat(response.getReasonCodes()).isEmpty();
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getDateOfBirth()).isEqualTo(DOB);
    }

    @Test
    void shouldFailKyc_whenIdentityIsSuspended() {
        repository.save(DigitalId.create(ID, NAME, DOB, NOW)
                .changeStatus(IdentityStatus.SUSPENDED, NOW).getPayload());

        BankKycResponse response = portal.checkBasicKyc(ID);

        assertThat(response.isValidNow()).isFalse();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("INACTIVE_STATUS");
    }

    @Test
    void shouldFailKyc_whenIdentityDoesNotExist() {
        BankKycResponse response = portal.checkBasicKyc(ID);

        assertThat(response.isValidNow()).isFalse();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("NOT_FOUND");
        assertThat(response.getName()).isNull();
        assertThat(response.getDateOfBirth()).isNull();
    }

    @Test
    void shouldFailKyc_whenActiveFinancialReviewRestrictionExists() {
        Restriction activeReview = new Restriction(
                RestrictionType.FINANCIAL_REVIEW, CHECK_DATE.minusDays(30), null, "UNDER_REVIEW");
        DigitalId identity = DigitalId.create(ID, NAME, DOB, NOW)
                .addRestriction(activeReview, CHECK_DATE).getPayload();
        repository.save(identity);

        BankKycResponse response = portal.checkBasicKyc(ID);

        assertThat(response.isValidNow()).isTrue();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("FINANCIAL_REVIEW_ACTIVE");
    }
}
