package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestrictionTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END   = LocalDate.of(2026, 6, 1);

    @Test
    void shouldCreateRestriction_whenDateRangeIsValid() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN, START, END, "BANNED");

        assertThat(restriction.type()).isEqualTo(RestrictionType.DRIVING_BAN);
        assertThat(restriction.startsOn()).isEqualTo(START);
        assertThat(restriction.endsOn()).isEqualTo(END);
        assertThat(restriction.reasonCode()).isEqualTo("BANNED");
    }

    @Test
    void shouldCreateRestriction_whenEndsOnIsAbsent() {
        Restriction restriction = new Restriction(RestrictionType.BORDER_HOLD, START, null, "HOLD");

        assertThat(restriction.endsOn()).isNull();
    }

    @Test
    void shouldRejectRestriction_whenEndIsBeforeStart() {
        assertThatThrownBy(() -> new Restriction(RestrictionType.DRIVING_BAN, END, START, "BANNED"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeActive_whenDateFallsWithinRange() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN, START, END, "BANNED");

        assertThat(restriction.isActiveOn(LocalDate.of(2026, 3, 1))).isTrue();
    }

    @Test
    void shouldBeActive_whenOpenEndedAndDateIsAfterStart() {
        Restriction restriction = new Restriction(RestrictionType.BORDER_HOLD, START, null, "HOLD");

        assertThat(restriction.isActiveOn(LocalDate.of(2030, 1, 1))).isTrue();
    }

    @Test
    void shouldBeInactive_whenDateIsAfterEnd() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN, START, END, "BANNED");

        assertThat(restriction.isActiveOn(LocalDate.of(2026, 12, 1))).isFalse();
    }

    @Test
    void shouldBeInactive_whenDateIsBeforeStart() {
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN, START, END, "BANNED");

        assertThat(restriction.isActiveOn(LocalDate.of(2025, 1, 1))).isFalse();
    }
}