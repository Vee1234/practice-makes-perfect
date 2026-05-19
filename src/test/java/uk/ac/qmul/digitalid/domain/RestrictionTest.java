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
        Restriction restriction = new Restriction(RestrictionType.DRIVING_BAN, START, END);

        assertThat(restriction.type()).isEqualTo(RestrictionType.DRIVING_BAN);
        assertThat(restriction.startsOn()).isEqualTo(START);
        assertThat(restriction.endsOn()).isEqualTo(END);
    }

    @Test
    void shouldCreateRestriction_whenEndsOnIsAbsent() {
        Restriction restriction = new Restriction(RestrictionType.BORDER_HOLD, START, null);

        assertThat(restriction.endsOn()).isNull();
    }

    @Test
    void shouldRejectRestriction_whenEndIsBeforeStart() {
        assertThatThrownBy(() -> new Restriction(RestrictionType.DRIVING_BAN, END, START))
                .isInstanceOf(IllegalArgumentException.class);
    }
}