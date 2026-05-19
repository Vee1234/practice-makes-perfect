package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DigitalIdTest {
    @Test
    void idAndDateOfBirthAreImmutable() {
        DigitalIdNumber id = DigitalIdNumber.of("DID-000002");
        LegalName name = new LegalName("Bob Example");
        LocalDate dob = LocalDate.of(1990, 2, 2);
        Instant now = Instant.parse("2026-05-18T00:00:00Z");
        DigitalId digitalId = DigitalId.create(id, name, dob, now);
        assertThrows(UnsupportedOperationException.class, () -> digitalId.changeId(DigitalIdNumber.of("DID-999999")));
        assertThrows(UnsupportedOperationException.class, () -> digitalId.changeDateOfBirth(LocalDate.of(1980, 1, 1)));
    }
}
