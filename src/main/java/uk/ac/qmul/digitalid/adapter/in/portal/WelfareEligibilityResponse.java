package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class WelfareEligibilityResponse {

    private final boolean eligible;
    private final String name;
    private final LocalDate dateOfBirth;
    private final BandCategory bandCategory;
    private final List<String> reasonCodes;

    public WelfareEligibilityResponse(boolean eligible, String name, LocalDate dateOfBirth,
                                      BandCategory bandCategory, List<String> reasonCodes) {
        this.eligible     = eligible;
        this.name         = name;
        this.dateOfBirth  = dateOfBirth;
        this.bandCategory = Objects.requireNonNull(bandCategory, "bandCategory is required");
        this.reasonCodes  = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes is required"));
    }

    public boolean isEligible()          { return eligible; }
    public String getName()              { return name; }
    public LocalDate getDateOfBirth()    { return dateOfBirth; }
    public BandCategory getBandCategory(){ return bandCategory; }
    public List<String> getReasonCodes() { return reasonCodes; }
}
