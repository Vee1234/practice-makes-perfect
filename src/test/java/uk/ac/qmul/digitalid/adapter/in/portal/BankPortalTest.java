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

    private static final Instant NOW         = Instant.parse("2026-01-01T00:00:00Z");
    private static final DigitalIdNumber ID  = DigitalIdNumber.of("DID-000001");
    private static final LegalName STORED_NAME = new LegalName("Jane Doe");
    private static final LocalDate STORED_DOB  = LocalDate.of(1990, 6, 15);

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
                "validNow", "nameMatched", "dobMatched", "kycDecision", "reasonCodes");
    }

    @Test
    void shouldNotExposeRawProfileWelfareOrImmigrationData_inBankKycResponse() {
        Set<String> fieldNames = Arrays.stream(BankKycResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "digitalId", "name", "dateOfBirth", "history", "nationality",
                "welfareBand", "restrictions", "residentialRegion");
    }

    // 13.3 — BankPortal facade behaviour

    @Test
    void shouldPassKyc_whenStatusIsActiveAndNameAndDobMatch() {
        repository.save(activeIdentity());
        BankKycRequest request = new BankKycRequest(ID, STORED_NAME, STORED_DOB);

        BankKycResponse response = portal.checkBasicKyc(request);

        assertThat(response.isValidNow()).isTrue();
        assertThat(response.isNameMatched()).isTrue();
        assertThat(response.isDobMatched()).isTrue();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.PASS);
        assertThat(response.getReasonCodes()).isEmpty();
    }

    @Test
    void shouldFailKyc_whenNameDoesNotMatch() {
        repository.save(activeIdentity());
        BankKycRequest request = new BankKycRequest(ID, new LegalName("Wrong Name"), STORED_DOB);

        BankKycResponse response = portal.checkBasicKyc(request);

        assertThat(response.isValidNow()).isTrue();
        assertThat(response.isNameMatched()).isFalse();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("NAME_MISMATCH");
    }

    @Test
    void shouldFailKyc_whenDobDoesNotMatch() {
        repository.save(activeIdentity());
        BankKycRequest request = new BankKycRequest(ID, STORED_NAME, LocalDate.of(1999, 1, 1));

        BankKycResponse response = portal.checkBasicKyc(request);

        assertThat(response.isValidNow()).isTrue();
        assertThat(response.isDobMatched()).isFalse();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("DOB_MISMATCH");
    }

    @Test
    void shouldFailKyc_whenIdentityIsSuspended() {
        repository.save(identity(IdentityStatus.SUSPENDED));
        BankKycRequest request = new BankKycRequest(ID, STORED_NAME, STORED_DOB);

        BankKycResponse response = portal.checkBasicKyc(request);

        assertThat(response.isValidNow()).isFalse();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("INVALID_STATUS");
    }

    @Test
    void shouldFailKyc_whenIdentityDoesNotExist() {
        BankKycRequest request = new BankKycRequest(ID, STORED_NAME, STORED_DOB);

        BankKycResponse response = portal.checkBasicKyc(request);

        assertThat(response.isValidNow()).isFalse();
        assertThat(response.getKycDecision()).isEqualTo(KycDecision.FAIL);
        assertThat(response.getReasonCodes()).contains("NOT_FOUND");
    }

    // helpers

    private DigitalId activeIdentity() {
        return DigitalId.create(ID, STORED_NAME, STORED_DOB, NOW);
    }

    private DigitalId identity(IdentityStatus targetStatus) {
        return activeIdentity().changeStatus(targetStatus, NOW).getPayload();
    }
}
