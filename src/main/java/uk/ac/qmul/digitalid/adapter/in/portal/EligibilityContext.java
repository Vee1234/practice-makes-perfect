package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.DigitalId;

import java.time.LocalDate;
import java.util.Objects;

public final class EligibilityContext {

    private final DigitalId identity;
    private final LocalDate checkDate;

    public EligibilityContext(DigitalId identity, LocalDate checkDate) {
        this.identity  = Objects.requireNonNull(identity,  "identity is required");
        this.checkDate = Objects.requireNonNull(checkDate, "checkDate is required");
    }

    public DigitalId getIdentity()   { return identity; }
    public LocalDate getCheckDate()  { return checkDate; }
}
