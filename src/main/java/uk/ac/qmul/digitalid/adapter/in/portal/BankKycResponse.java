package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class BankKycResponse {

    private final boolean validNow;
    private final String name;
    private final LocalDate dateOfBirth;
    private final KycDecision kycDecision;
    private final List<String> reasonCodes;

    public BankKycResponse(boolean validNow, String name, LocalDate dateOfBirth,
                           KycDecision kycDecision, List<String> reasonCodes) {
        this.validNow    = validNow;
        this.name        = name;
        this.dateOfBirth = dateOfBirth;
        this.kycDecision = Objects.requireNonNull(kycDecision, "kycDecision is required");
        this.reasonCodes = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes is required"));
    }

    public boolean isValidNow()         { return validNow; }
    public String getName()             { return name; }
    public LocalDate getDateOfBirth()   { return dateOfBirth; }
    public KycDecision getKycDecision() { return kycDecision; }
    public List<String> getReasonCodes(){ return reasonCodes; }
}
