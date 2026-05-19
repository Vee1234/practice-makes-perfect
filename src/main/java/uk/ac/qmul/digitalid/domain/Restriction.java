package uk.ac.qmul.digitalid.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class Restriction {

    private final RestrictionType type;
    private final LocalDate startsOn;
    private final LocalDate endsOn;
    private final String reasonCode;

    public Restriction(RestrictionType type, LocalDate startsOn, LocalDate endsOn, String reasonCode) {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(startsOn, "startsOn is required");
        Objects.requireNonNull(reasonCode, "reasonCode is required");
        if (endsOn != null && endsOn.isBefore(startsOn)) {
            throw new IllegalArgumentException("endsOn must not be before startsOn");
        }
        this.type = type;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.reasonCode = reasonCode;
    }

    public RestrictionType getType()    { return type; }
    public LocalDate getStartsOn()      { return startsOn; }
    public LocalDate getEndsOn()        { return endsOn; }
    public String getReasonCode()       { return reasonCode; }

    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startsOn) && (endsOn == null || !date.isAfter(endsOn));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restriction r)) return false;
        return type == r.type
                && startsOn.equals(r.startsOn)
                && Objects.equals(endsOn, r.endsOn)
                && reasonCode.equals(r.reasonCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, startsOn, endsOn, reasonCode);
    }
}