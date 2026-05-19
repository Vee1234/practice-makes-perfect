package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DigitalIdTest {
    @Test
    void creationSetsActiveStatus() {
        DigitalIdNumber id = DigitalIdNumber.of("DID-000001");
        LegalName name = new LegalName("Alice Example");
        LocalDate dob = LocalDate.of(2000, 1, 1);
        Instant now = Instant.parse("2026-05-18T00:00:00Z");
        DigitalId digitalId = DigitalId.create(id, name, dob, now);
        assertEquals(IdentityStatus.ACTIVE, digitalId.getStatus());
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
    void legalNameUpdateChangesUpdatedAt() {
        DigitalIdNumber id = DigitalIdNumber.of("DID-000003");
        LegalName name = new LegalName("Carol Example");
        LocalDate dob = LocalDate.of(1985, 3, 3);
        Instant created = Instant.parse("2026-05-18T00:00:00Z");
        DigitalId digitalId = DigitalId.create(id, name, dob, created);
        Instant updated = Instant.parse("2026-05-19T00:00:00Z");
        digitalId = digitalId.updateLegalName(new LegalName("Carol Smith"), updated);
        assertEquals("Carol Smith", digitalId.getCurrentLegalName().value());
        assertEquals(updated, digitalId.getUpdatedAt());
    }

    @Test
    void blankLegalNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> new LegalName(""));
        assertThrows(IllegalArgumentException.class, () -> new LegalName("   "));
    }

    @Test
    void addRestrictionAppendsToRestrictionsSet() {
        DigitalId digitalId = activeIdentity("DID-000010");
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");

        OperationResult<DigitalId> result = digitalId.addRestriction(restriction, LocalDate.of(2026, 1, 1));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getRestrictions()).containsExactly(restriction);
    }

    @Test
    void addRestriction_rejectsOverlappingActiveRestrictionOfSameType() {
        DigitalId digitalId = activeIdentity("DID-000011");
        Restriction first = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "COURT_ORDER");

        DigitalId withFirst = digitalId.addRestriction(first, LocalDate.of(2026, 1, 1)).getPayload();

        Restriction overlap = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 6, 1), null, "SECOND_ORDER");
        OperationResult<DigitalId> result = withFirst.addRestriction(overlap, LocalDate.of(2026, 6, 1));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().code()).isEqualTo(ErrorCode.INVALID_COMMAND);
    }

    @Test
    void addRestriction_allowsDifferentTypeOnSameDate() {
        DigitalId digitalId = activeIdentity("DID-000012");
        Restriction ban = new Restriction(RestrictionType.DRIVING_BAN,
                LocalDate.of(2026, 1, 1), null, "COURT_ORDER");
        Restriction hold = new Restriction(RestrictionType.BORDER_HOLD,
                LocalDate.of(2026, 1, 1), null, "IMMIGRATION");

        DigitalId withBan = digitalId.addRestriction(ban, LocalDate.of(2026, 1, 1)).getPayload();
        OperationResult<DigitalId> result = withBan.addRestriction(hold, LocalDate.of(2026, 1, 1));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload().getRestrictions()).hasSize(2);
    }

    @Test
    void newIdentityHasNoRestrictions() {
        DigitalId digitalId = activeIdentity("DID-000013");
        assertThat(digitalId.getRestrictions()).isEmpty();
    }

    private DigitalId activeIdentity(String id) {
        return DigitalId.create(DigitalIdNumber.of(id), new LegalName("Test User"),
                LocalDate.of(1990, 1, 1), Instant.parse("2026-01-01T00:00:00Z"));
    }
}
