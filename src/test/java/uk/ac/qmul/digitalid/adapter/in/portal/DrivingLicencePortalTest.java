package uk.ac.qmul.digitalid.adapter.in.portal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DrivingLicencePortalTest {

    // 15.1 — DrivingLicenceResponse must expose only the fields the authority is authorised to receive

    @Test
    void shouldExposeOnlyAllowedFields_inDrivingLicenceResponse() {
        Set<String> fieldNames = Arrays.stream(DrivingLicenceResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "eligible", "minimumAgeMet", "restrictionBlock", "reasonCodes");
    }

    @Test
    void shouldNotExposeRestrictionTextWelfareTaxOrImmigrationData_inDrivingLicenceResponse() {
        Set<String> fieldNames = Arrays.stream(DrivingLicenceResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "name", "dateOfBirth", "nationality", "welfareBand",
                "restrictionText", "restrictionReason", "history", "status");
    }
}
