package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.Instant;
import java.util.Objects;

public final class RightToWorkResponse {

    private final String digitalId;
    private final boolean validNow;
    private final String reasonCode;
    private final Instant checkedAt;

    public RightToWorkResponse(String digitalId, boolean validNow, String reasonCode, Instant checkedAt) {
        this.digitalId = Objects.requireNonNull(digitalId, "digitalId is required");
        this.validNow = validNow;
        this.reasonCode = reasonCode;
        this.checkedAt = Objects.requireNonNull(checkedAt, "checkedAt is required");
    }

    public String getDigitalId()  { return digitalId; }
    public boolean isValidNow()   { return validNow; }
    public String getReasonCode() { return reasonCode; }
    public Instant getCheckedAt() { return checkedAt; }
}
