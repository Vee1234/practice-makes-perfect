package uk.ac.qmul.digitalid.application.service.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.domain.ErrorCode;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private static final Organisation AUDITOR  = new Organisation("AUD-001", OrganisationRole.AUDITOR);
    private static final Organisation EMPLOYER = new Organisation("EMP-001", OrganisationRole.EMPLOYER);

    private InMemoryAuditSink sink;
    private AuditQueryService service;

    @BeforeEach
    void setUp() {
        sink    = new InMemoryAuditSink();
        service = new AuditQueryService(sink, new AuthorisationService());
    }

    @Test
    void shouldReturnAllEvents_whenRequestedByAuditor() {
        AuditEvent event = new AuditEvent("VERIFY_IDENTITY", "EMP-001", NOW, true, null);
        sink.record(event);

        OperationResult<List<AuditEvent>> result = service.query(AUDITOR);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload()).containsExactly(event);
    }

    @Test
    void shouldReturnEmptyList_whenNoEventsRecorded_andRequestedByAuditor() {
        OperationResult<List<AuditEvent>> result = service.query(AUDITOR);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload()).isEmpty();
    }

    @Test
    void shouldFail_withUnauthorisedOperation_whenRequestedByEmployer() {
        OperationResult<List<AuditEvent>> result = service.query(EMPLOYER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }

    @Test
    void shouldReturnMultipleEvents_inRecordedOrder() {
        AuditEvent first  = new AuditEvent("VERIFY_IDENTITY", "EMP-001", NOW, true,  null);
        AuditEvent second = new AuditEvent("VERIFY_IDENTITY", "BANK-001", NOW, false, "REJECTED");
        sink.record(first);
        sink.record(second);

        OperationResult<List<AuditEvent>> result = service.query(AUDITOR);

        assertThat(result.getPayload()).containsExactly(first, second);
    }
}
