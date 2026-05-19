package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DigitalIdTest {
    @Test
    void creationSetsActiveStatus() {
        DigitalIdNumber id = DigitalIdNumber.of("DID-000001");
        LegalName name = new LegalName("Alice Example");
        LocalDate dob = LocalDate.of(2000, 1, 1);
        Instant now = Instant.parse("2026-05-18T00:00:00Z");
        DigitalId digitalId = DigitalId.create(id, name, dob, now);
        assertEquals(IdentityStatus.ACTIVE, digitalId.status());
    }

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

    @Test
    void updateLegalNameCreatesNewInstance() {
        DigitalIdNumber id = DigitalIdNumber.of("DID-000003");
        LegalName name = new LegalName("Carol Example");
        LocalDate dob = LocalDate.of(1985, 3, 3);
        Instant created = Instant.parse("2026-05-18T00:00:00Z");
        DigitalId digitalId = DigitalId.create(id, name, dob, created);
        Instant updated = Instant.parse("2026-05-19T00:00:00Z");
        digitalId = digitalId.updateLegalName(new LegalName("Carol Smith"), updated);
        assertEquals("Carol Smith", digitalId.currentLegalName().value());
        assertEquals(updated, digitalId.updatedAt());
    }
}
