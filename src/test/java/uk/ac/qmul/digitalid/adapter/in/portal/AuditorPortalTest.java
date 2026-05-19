package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuditorPortalTest {

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
}