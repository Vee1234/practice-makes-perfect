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

class DigitalIdManagementServiceUpdateTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Organisation CENTRAL = new Organisation("CA-001", OrganisationRole.CENTRAL_AUTHORITY);
    private static final Organisation EMPLOYER = new Organisation("EMP-001", OrganisationRole.EMPLOYER);
    private static final DigitalIdNumber ID = DigitalIdNumber.of("DID-000001");

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

        DigitalId existing = DigitalId.create(ID, new LegalName("Jane Doe"), LocalDate.of(1990, 1, 1), NOW);
        repository.save(existing);
    }

    @Test
    void shouldUpdateLegalName_whenCentralAuthorityRequests() {
        IdentityUpdateCommand command = new IdentityUpdateCommand(ID, new LegalName("Jane Smith"), CENTRAL);

        OperationResult<DigitalId> result = service.updateMutableAttributes(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.payload().currentLegalName()).isEqualTo(new LegalName("Jane Smith"));
    }

    @Test
    void shouldPersistUpdate_whenLegalNameChanged() {
        IdentityUpdateCommand command = new IdentityUpdateCommand(ID, new LegalName("Jane Smith"), CENTRAL);

        service.updateMutableAttributes(command);

        assertThat(repository.findById(ID).get().currentLegalName()).isEqualTo(new LegalName("Jane Smith"));
    }

    @Test
    void shouldRejectUpdate_whenIdentityNotFound() {
        IdentityUpdateCommand command = new IdentityUpdateCommand(
                DigitalIdNumber.of("DID-000099"), new LegalName("Jane Smith"), CENTRAL);

        OperationResult<DigitalId> result = service.updateMutableAttributes(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void shouldRejectUpdate_whenIdentityIsRevoked() {
        repository.save(repository.findById(ID).get().changeStatus(IdentityStatus.REVOKED, NOW).payload());
        IdentityUpdateCommand command = new IdentityUpdateCommand(ID, new LegalName("Jane Smith"), CENTRAL);

        OperationResult<DigitalId> result = service.updateMutableAttributes(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.NOT_MODIFIABLE);
    }

    @Test
    void shouldRejectUpdate_whenIdentityIsExpired() {
        repository.save(repository.findById(ID).get().changeStatus(IdentityStatus.EXPIRED, NOW).payload());
        IdentityUpdateCommand command = new IdentityUpdateCommand(ID, new LegalName("Jane Smith"), CENTRAL);

        OperationResult<DigitalId> result = service.updateMutableAttributes(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.NOT_MODIFIABLE);
    }

    @Test
    void shouldRejectUpdate_whenNotCentralAuthority() {
        IdentityUpdateCommand command = new IdentityUpdateCommand(ID, new LegalName("Jane Smith"), EMPLOYER);

        OperationResult<DigitalId> result = service.updateMutableAttributes(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldAuditSuccess_whenLegalNameUpdated() {
        IdentityUpdateCommand command = new IdentityUpdateCommand(ID, new LegalName("Jane Smith"), CENTRAL);

        service.updateMutableAttributes(command);

        assertThat(auditSink.events()).hasSize(1);
        assertThat(auditSink.events().get(0).success()).isTrue();
    }

    @Test
    void shouldAuditFailure_whenIdentityNotFound() {
        IdentityUpdateCommand command = new IdentityUpdateCommand(
                DigitalIdNumber.of("DID-000099"), new LegalName("Jane Smith"), CENTRAL);

        service.updateMutableAttributes(command);

        assertThat(auditSink.events()).hasSize(1);
        assertThat(auditSink.events().get(0).success()).isFalse();
    }
}