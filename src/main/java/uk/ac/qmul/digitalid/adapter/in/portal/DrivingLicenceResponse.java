package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class DrivingLicenceResponse {

    private final boolean eligible;
    private final boolean minimumAgeMet;
    private final boolean restrictionBlock;
    private final String name;
    private final LocalDate dateOfBirth;
    private final List<String> reasonCodes;

    public DrivingLicenceResponse(boolean eligible, boolean minimumAgeMet, boolean restrictionBlock,
                                  String name, LocalDate dateOfBirth, List<String> reasonCodes) {
        this.eligible         = eligible;
        this.minimumAgeMet    = minimumAgeMet;
        this.restrictionBlock = restrictionBlock;
        this.name             = name;
        this.dateOfBirth      = dateOfBirth;
        this.reasonCodes      = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes is required"));
    }

    public boolean isEligible()          { return eligible; }
    public boolean isMinimumAgeMet()     { return minimumAgeMet; }
    public boolean isRestrictionBlock()  { return restrictionBlock; }
    public String getName()              { return name; }
    public LocalDate getDateOfBirth()    { return dateOfBirth; }
    public List<String> getReasonCodes() { return reasonCodes; }
}
