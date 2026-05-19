package uk.ac.qmul.digitalid.adapter.in.portal;

import java.util.List;
import java.util.Objects;

public final class DrivingLicenceResponse {

    private final boolean eligible;
    private final boolean minimumAgeMet;
    private final boolean restrictionBlock;
    private final List<String> reasonCodes;

    public DrivingLicenceResponse(boolean eligible, boolean minimumAgeMet,
                                  boolean restrictionBlock, List<String> reasonCodes) {
        this.eligible         = eligible;
        this.minimumAgeMet    = minimumAgeMet;
        this.restrictionBlock = restrictionBlock;
        this.reasonCodes      = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes is required"));
    }

    public boolean isEligible()          { return eligible; }
    public boolean isMinimumAgeMet()     { return minimumAgeMet; }
    public boolean isRestrictionBlock()  { return restrictionBlock; }
    public List<String> getReasonCodes() { return reasonCodes; }
}
