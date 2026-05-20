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

class DigitalIdManagementServiceStatusRestrictionTest {

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

    // --- changeStatus ---

    @Test
    void shouldSuspendActiveIdentity_whenCentralAuthorityRequests() {
        ChangeStatusCommand command = new ChangeStatusCommand(ID, IdentityStatus.SUSPENDED, CENTRAL);

        OperationResult<DigitalId> result = service.changeStatus(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.SUSPENDED);
    }

    @Test
    void shouldReactivateSuspendedIdentity_whenCentralAuthorityRequests() {
        service.changeStatus(new ChangeStatusCommand(ID, IdentityStatus.SUSPENDED, CENTRAL));
        ChangeStatusCommand command = new ChangeStatusCommand(ID, IdentityStatus.ACTIVE, CENTRAL);

        OperationResult<DigitalId> result = service.changeStatus(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.ACTIVE);
    }

    @Test
    void shouldRevokeIdentity_whenCentralAuthorityRequests() {
        ChangeStatusCommand command = new ChangeStatusCommand(ID, IdentityStatus.REVOKED, CENTRAL);

        OperationResult<DigitalId> result = service.changeStatus(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getStatus()).isEqualTo(IdentityStatus.REVOKED);
    }

    @Test
    void shouldRejectStatusChange_whenTransitionIsInvalid() {
        service.changeStatus(new ChangeStatusCommand(ID, IdentityStatus.REVOKED, CENTRAL));
        ChangeStatusCommand command = new ChangeStatusCommand(ID, IdentityStatus.ACTIVE, CENTRAL);

        OperationResult<DigitalId> result = service.changeStatus(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void shouldRejectStatusChange_whenIdentityNotFound() {
        ChangeStatusCommand command = new ChangeStatusCommand(
                DigitalIdNumber.of("DID-000099"), IdentityStatus.SUSPENDED, CENTRAL);

        OperationResult<DigitalId> result = service.changeStatus(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void shouldRejectStatusChange_whenNotCentralAuthority() {
        ChangeStatusCommand command = new ChangeStatusCommand(ID, IdentityStatus.SUSPENDED, EMPLOYER);

        OperationResult<DigitalId> result = service.changeStatus(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldAuditSuccess_whenStatusChanged() {
        service.changeStatus(new ChangeStatusCommand(ID, IdentityStatus.SUSPENDED, CENTRAL));

        assertThat(auditSink.findAll()).hasSize(1);
        assertThat(auditSink.findAll().get(0).isSuccess()).isTrue();
    }

    @Test
    void shouldAuditFailure_whenStatusChangeUnauthorised() {
        service.changeStatus(new ChangeStatusCommand(ID, IdentityStatus.SUSPENDED, EMPLOYER));

        assertThat(auditSink.findAll()).hasSize(1);
        assertThat(auditSink.findAll().get(0).isSuccess()).isFalse();
    }

    // --- addRestriction ---

    @Test
    void shouldAddRestriction_whenCentralAuthorityRequests() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");
        AddRestrictionCommand command = new AddRestrictionCommand(ID, restriction, CENTRAL);

        OperationResult<DigitalId> result = service.addRestriction(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getRestrictions()).containsExactly(restriction);
    }

    @Test
    void shouldRejectAddRestriction_whenOverlappingActiveRestrictionExists() {
        Restriction first = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");
        service.addRestriction(new AddRestrictionCommand(ID, first, CENTRAL));

        Restriction overlap = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 6, 1), null, "SECOND_ORDER");
        OperationResult<DigitalId> result = service.addRestriction(new AddRestrictionCommand(ID, overlap, CENTRAL));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.INVALID_COMMAND);
    }

    @Test
    void shouldRejectAddRestriction_whenIdentityNotFound() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");
        AddRestrictionCommand command = new AddRestrictionCommand(
                DigitalIdNumber.of("DID-000099"), restriction, CENTRAL);

        OperationResult<DigitalId> result = service.addRestriction(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void shouldRejectAddRestriction_whenNotCentralAuthority() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");
        AddRestrictionCommand command = new AddRestrictionCommand(ID, restriction, EMPLOYER);

        OperationResult<DigitalId> result = service.addRestriction(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldAuditSuccess_whenRestrictionAdded() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");
        service.addRestriction(new AddRestrictionCommand(ID, restriction, CENTRAL));

        assertThat(auditSink.findAll()).hasSize(1);
        assertThat(auditSink.findAll().get(0).isSuccess()).isTrue();
    }
}
