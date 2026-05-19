package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.application.audit.AuditEvent;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.service.audit.AuditQueryService;
import uk.ac.qmul.digitalid.domain.ErrorCode;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuditorPortalTest {

    private static final Instant NOW     = Instant.parse("2026-01-01T00:00:00Z");
    private static final Organisation EMPLOYER = new Organisation("EMP-001", OrganisationRole.EMPLOYER);

    private InMemoryAuditSink sink;
    private AuditorPortal portal;
    private AuditQueryService auditQueryService;

    @BeforeEach
    void setUp() {
        sink              = new InMemoryAuditSink();
        auditQueryService = new AuditQueryService(sink, new AuthorisationService());
        portal            = new AuditorPortal(auditQueryService);
    }

    // 21.1 — AuditEventSummary must expose only audit metadata, never identity attributes

    @Test
    void shouldExposeOnlyAllowedFields_inAuditEventSummary() {
        Set<String> fieldNames = Arrays.stream(AuditEventSummary.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "eventType", "actor", "timestamp", "success", "reasonCode");
    }

    @Test
    void shouldNotExposeIdentityAttributes_inAuditEventSummary() {
        Set<String> fieldNames = Arrays.stream(AuditEventSummary.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "name", "dateOfBirth", "nationality", "welfareBand",
                "restrictions", "residentialRegion", "status", "digitalIdNumber");
    }

    // 21.4 — AuditorPortal facade behaviour

    @Test
    void shouldReturnEmptySummaries_whenNoEventsHaveBeenRecorded() {
        List<AuditEventSummary> summaries = portal.getEventSummaries();

        assertThat(summaries).isEmpty();
    }

    @Test
    void shouldReturnProjectedSummary_whenOneEventRecorded() {
        sink.record(new AuditEvent("VERIFY_IDENTITY", "EMP-001", NOW, true, null));

        List<AuditEventSummary> summaries = portal.getEventSummaries();

        assertThat(summaries).hasSize(1);
        AuditEventSummary summary = summaries.get(0);
        assertThat(summary.getEventType()).isEqualTo("VERIFY_IDENTITY");
        assertThat(summary.getActor()).isEqualTo("EMP-001");
        assertThat(summary.getTimestamp()).isEqualTo(NOW);
        assertThat(summary.isSuccess()).isTrue();
        assertThat(summary.getReasonCode()).isNull();
    }

    @Test
    void shouldReturnAllSummaries_inRecordedOrder() {
        sink.record(new AuditEvent("VERIFY_IDENTITY", "EMP-001",  NOW, true,  null));
        sink.record(new AuditEvent("VERIFY_IDENTITY", "BANK-001", NOW, false, "REJECTED"));

        List<AuditEventSummary> summaries = portal.getEventSummaries();

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).getActor()).isEqualTo("EMP-001");
        assertThat(summaries.get(1).getActor()).isEqualTo("BANK-001");
        assertThat(summaries.get(1).getReasonCode()).isEqualTo("REJECTED");
    }

    @Test
    void shouldProjectReasonCode_whenEventWasUnsuccessful() {
        sink.record(new AuditEvent("VERIFY_IDENTITY", "EMP-001", NOW, false, "NOT_FOUND"));

        AuditEventSummary summary = portal.getEventSummaries().get(0);

        assertThat(summary.isSuccess()).isFalse();
        assertThat(summary.getReasonCode()).isEqualTo("NOT_FOUND");
    }

    // 21.3 — non-auditor roles are rejected at the service level

    @Test
    void shouldRejectQuery_whenOrganisationIsNotAuditor() {
        sink.record(new AuditEvent("VERIFY_IDENTITY", "EMP-001", NOW, true, null));

        OperationResult<?> result = auditQueryService.query(EMPLOYER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.UNAUTHORISED_OPERATION);
    }
}
