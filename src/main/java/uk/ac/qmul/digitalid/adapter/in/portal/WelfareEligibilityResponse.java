package uk.ac.qmul.digitalid.adapter.in.portal;

import java.util.List;
import java.util.Objects;

public final class WelfareEligibilityResponse {

    private final boolean eligible;
    private final BandCategory bandCategory;
    private final List<String> reasonCodes;

    public WelfareEligibilityResponse(boolean eligible, BandCategory bandCategory, List<String> reasonCodes) {
        this.eligible     = eligible;
        this.bandCategory = Objects.requireNonNull(bandCategory, "bandCategory is required");
        this.reasonCodes  = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes is required"));
    }

    public boolean isEligible()          { return eligible; }
    public BandCategory getBandCategory(){ return bandCategory; }
    public List<String> getReasonCodes() { return reasonCodes; }
}
