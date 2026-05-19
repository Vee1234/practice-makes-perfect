package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployerPortalTest {

    // 12.1: RightToWorkResponse must expose only the fields the employer is authorised to receive
    @Test
    void shouldExposeOnlyAllowedFields_inRightToWorkResponse() {
        Set<String> fieldNames = Arrays.stream(RightToWorkResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder("digitalId", "validNow", "reasonCode", "checkedAt");
    }

    @Test
    void shouldNotExposeNameDobRestrictionsOrHistory_inResponse() {
        Set<String> fieldNames = Arrays.stream(RightToWorkResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain("name", "dateOfBirth", "restrictions", "history",
                "nationality", "welfareBand", "currentLegalName");
    }
}
