package uk.ac.qmul.digitalid.domain;

import java.time.LocalDate;
import java.util.Objects;

public record Restriction(RestrictionType type, LocalDate startsOn, LocalDate endsOn) {

    public Restriction {
        Objects.requireNonNull(type);
        Objects.requireNonNull(startsOn);
        if (endsOn != null && endsOn.isBefore(startsOn)) {
            throw new IllegalArgumentException("endsOn must not be before startsOn");
        }
    }
}