package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.adapter.in.portal.WelfareEligibilityResponse;
import uk.ac.qmul.digitalid.adapter.in.portal.WelfarePortal;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class WelfareConsoleCommand implements ConsoleCommand {

    private final WelfarePortal portal;

    public WelfareConsoleCommand(WelfarePortal portal) {
        this.portal = portal;
    }

    @Override
    public String getDescription() { return "Check benefit eligibility"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        WelfareEligibilityResponse response = portal.checkBenefitEligibility(id.get());
        if (response.getReasonCodes().contains("NOT_FOUND")) { out.println("Identity not found."); return; }
        out.println("Eligible:       " + response.isEligible());
        out.println("Name:           " + response.getName());
        out.println("Date of birth:  " + response.getDateOfBirth());
        out.println("Band category:  " + response.getBandCategory());
        out.println("Reason codes:   " + response.getReasonCodes());
    }
}
