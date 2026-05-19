package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class RightToWorkResponse {

    private final String digitalId;
    private final boolean validNow;
    private final String name;
    private final LocalDate dateOfBirth;
    private final String reasonCode;
    private final Instant checkedAt;

    public RightToWorkResponse(String digitalId, boolean validNow, String name, LocalDate dateOfBirth,
                               String reasonCode, Instant checkedAt) {
        this.digitalId   = Objects.requireNonNull(digitalId, "digitalId is required");
        this.validNow    = validNow;
        this.name        = name;
        this.dateOfBirth = dateOfBirth;
        this.reasonCode  = reasonCode;
        this.checkedAt   = Objects.requireNonNull(checkedAt, "checkedAt is required");
    }

    public String getDigitalId()      { return digitalId; }
    public boolean isValidNow()       { return validNow; }
    public String getName()           { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getReasonCode()     { return reasonCode; }
    public Instant getCheckedAt()     { return checkedAt; }
}
