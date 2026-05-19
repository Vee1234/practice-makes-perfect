package uk.ac.qmul.digitalid.adapter.in.portal;

import java.util.List;
import java.util.Objects;

public final class BankKycResponse {

    private final boolean validNow;
    private final boolean nameMatched;
    private final boolean dobMatched;
    private final KycDecision kycDecision;
    private final List<String> reasonCodes;

    public BankKycResponse(boolean validNow, boolean nameMatched, boolean dobMatched,
                           KycDecision kycDecision, List<String> reasonCodes) {
        this.validNow = validNow;
        this.nameMatched = nameMatched;
        this.dobMatched = dobMatched;
        this.kycDecision = Objects.requireNonNull(kycDecision, "kycDecision is required");
        this.reasonCodes = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes is required"));
    }

    public boolean isValidNow()        { return validNow; }
    public boolean isNameMatched()     { return nameMatched; }
    public boolean isDobMatched()      { return dobMatched; }
    public KycDecision getKycDecision(){ return kycDecision; }
    public List<String> getReasonCodes(){ return reasonCodes; }
}
