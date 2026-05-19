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

class EmployerPortalTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final DigitalIdNumber ID = DigitalIdNumber.of("DID-000001");
    private static final LocalDate DOB = LocalDate.of(1990, 1, 1);

    private InMemoryDigitalIdRepository repository;
    private EmployerPortal portal;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(auditSink);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        VerifyIdentityPort consumptionService = new DigitalIdConsumptionService(repository, auditPublisher, clock);
        portal = new EmployerPortal(consumptionService, clock);
    }

    // 12.1 — projection field-scope

    @Test
    void shouldExposeOnlyAllowedFields_inRightToWorkResponse() {
        Set<String> fieldNames = Arrays.stream(RightToWorkResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "digitalId", "validNow", "name", "dateOfBirth", "reasonCode", "checkedAt");
    }

    @Test
    void shouldNotExposeRestrictionsHistoryOrImmigrationData_inResponse() {
        Set<String> fieldNames = Arrays.stream(RightToWorkResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain("restrictions", "history",
                "nationality", "welfareBand", "currentLegalName");
    }

    // 12.3 — EmployerPortal facade behaviour

    @Test
    void shouldReturnValidNow_whenIdentityIsActive() {
        repository.save(identity(IdentityStatus.ACTIVE));

        assertThat(portal.verifyRightToWork(ID).isValidNow()).isTrue();
    }

    @Test
    void shouldReturnInvalid_whenIdentityIsSuspended() {
        repository.save(identity(IdentityStatus.SUSPENDED));

        assertThat(portal.verifyRightToWork(ID).isValidNow()).isFalse();
    }

    @Test
    void shouldReturnInvalid_whenIdentityIsRevoked() {
        repository.save(identity(IdentityStatus.REVOKED));

        assertThat(portal.verifyRightToWork(ID).isValidNow()).isFalse();
    }

    @Test
    void shouldReturnInvalid_whenIdentityDoesNotExist() {
        assertThat(portal.verifyRightToWork(ID).isValidNow()).isFalse();
    }

    @Test
    void shouldPopulateDigitalIdCheckedAtNameAndDob_inResponse() {
        repository.save(identity(IdentityStatus.ACTIVE));

        RightToWorkResponse response = portal.verifyRightToWork(ID);

        assertThat(response.getDigitalId()).isEqualTo("DID-000001");
        assertThat(response.getCheckedAt()).isEqualTo(NOW);
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getDateOfBirth()).isEqualTo(DOB);
    }

    @Test
    void shouldIncludeReasonCode_whenIdentityIsInvalid() {
        assertThat(portal.verifyRightToWork(ID).getReasonCode()).isNotNull();
    }

    @Test
    void shouldReturnNullNameAndDob_whenIdentityDoesNotExist() {
        RightToWorkResponse response = portal.verifyRightToWork(ID);

        assertThat(response.getName()).isNull();
        assertThat(response.getDateOfBirth()).isNull();
    }

    // helper

    private DigitalId identity(IdentityStatus target) {
        DigitalId base = DigitalId.create(ID, new LegalName("Jane Doe"), DOB, NOW);
        if (target == IdentityStatus.ACTIVE) return base;
        return base.changeStatus(target, NOW).getPayload();
    }
}
