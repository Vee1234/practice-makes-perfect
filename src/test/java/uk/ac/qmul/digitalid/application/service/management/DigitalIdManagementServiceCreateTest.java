package uk.ac.qmul.digitalid.application.service.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.adapter.out.persistence.InMemoryDigitalIdRepository;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.domain.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalIdManagementServiceCreateTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Organisation CENTRAL = new Organisation("CA-001", OrganisationRole.CENTRAL_AUTHORITY);
    private static final Organisation EMPLOYER = new Organisation("EMP-001", OrganisationRole.EMPLOYER);

    private InMemoryDigitalIdRepository repository;
    private InMemoryAuditSink auditSink;
    private DigitalIdManagementService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        auditSink = new InMemoryAuditSink();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(auditSink);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new DigitalIdManagementService(repository, auditPublisher, new AuthorisationService(), clock);
    }

    @Test
    void shouldCreateActiveDigitalId_whenCentralAuthorityRequests() {
        IdentityCreateCommand command = new IdentityCreateCommand(
                DigitalIdNumber.of("DID-000001"), new LegalName("Jane Doe"),
                LocalDate.of(1990, 1, 1), CENTRAL);

        OperationResult<DigitalId> result = service.createIdentity(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.ACTIVE);
        assertThat(repository.exists(DigitalIdNumber.of("DID-000001"))).isTrue();
    }

    @Test
    void shouldRejectCreation_whenDuplicateId() {
        IdentityCreateCommand command = new IdentityCreateCommand(
                DigitalIdNumber.of("DID-000001"), new LegalName("Jane Doe"),
                LocalDate.of(1990, 1, 1), CENTRAL);
        service.createIdentity(command);

        OperationResult<DigitalId> result = service.createIdentity(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.DUPLICATE_ID);
    }

    @Test
    void shouldRejectCreation_whenNotCentralAuthority() {
        IdentityCreateCommand command = new IdentityCreateCommand(
                DigitalIdNumber.of("DID-000001"), new LegalName("Jane Doe"),
                LocalDate.of(1990, 1, 1), EMPLOYER);

        OperationResult<DigitalId> result = service.createIdentity(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldAuditSuccess_whenIdentityCreated() {
        IdentityCreateCommand command = new IdentityCreateCommand(
                DigitalIdNumber.of("DID-000001"), new LegalName("Jane Doe"),
                LocalDate.of(1990, 1, 1), CENTRAL);

        service.createIdentity(command);

        assertThat(auditSink.events()).hasSize(1);
        assertThat(auditSink.events().get(0).isSuccess()).isTrue();
    }

    @Test
    void shouldAuditFailure_whenUnauthorised() {
        IdentityCreateCommand command = new IdentityCreateCommand(
                DigitalIdNumber.of("DID-000001"), new LegalName("Jane Doe"),
                LocalDate.of(1990, 1, 1), EMPLOYER);

        service.createIdentity(command);

        assertThat(auditSink.events()).hasSize(1);
        assertThat(auditSink.events().get(0).isSuccess()).isFalse();
    }
}