package uk.ac.qmul.digitalid.application.service.consumption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.adapter.out.persistence.InMemoryDigitalIdRepository;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.domain.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalIdConsumptionServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Organisation EMPLOYER = new Organisation("EMP-001", OrganisationRole.EMPLOYER);
    private static final DigitalIdNumber ID = DigitalIdNumber.of("DID-000001");

    private InMemoryDigitalIdRepository repository;
    private InMemoryAuditSink auditSink;
    private DigitalIdConsumptionService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        auditSink = new InMemoryAuditSink();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(auditSink);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new DigitalIdConsumptionService(repository, auditPublisher, clock);
    }

    @Test
    void shouldReturnNotFound_whenIdentityDoesNotExist() {
        VerificationRequest request = new VerificationRequest(ID, EMPLOYER, identity -> VerificationDecision.VERIFIED);

        VerificationDecision decision = service.verify(request);

        assertThat(decision).isEqualTo(VerificationDecision.NOT_FOUND);
    }

    @Test
    void shouldDelegateToPolicy_whenIdentityExists() {
        repository.save(DigitalId.create(ID, new LegalName("Jane Doe"), LocalDate.of(1990, 1, 1), NOW));
        VerificationRequest request = new VerificationRequest(ID, EMPLOYER, identity -> VerificationDecision.VERIFIED);

        VerificationDecision decision = service.verify(request);

        assertThat(decision).isEqualTo(VerificationDecision.VERIFIED);
    }

    @Test
    void shouldAudit_onEveryVerification() {
        VerificationRequest request = new VerificationRequest(ID, EMPLOYER, identity -> VerificationDecision.VERIFIED);

        service.verify(request);

        assertThat(auditSink.findAll()).hasSize(1);
        assertThat(auditSink.findAll().get(0).getEventType()).isEqualTo("VERIFY_IDENTITY");
    }
}
