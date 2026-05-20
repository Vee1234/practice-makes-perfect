package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.adapter.in.portal.DrivingLicencePortal;
import uk.ac.qmul.digitalid.adapter.in.portal.DrivingLicenceResponse;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class DrivingLicenceConsoleCommand implements ConsoleCommand {

    private final DrivingLicencePortal portal;

    public DrivingLicenceConsoleCommand(DrivingLicencePortal portal) {
        this.portal = portal;
    }

    @Override
    public String getDescription() { return "Check driving licence eligibility"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        DrivingLicenceResponse response = portal.checkLicenceEligibility(id.get());
        if (response.getReasonCodes().contains("NOT_FOUND")) { out.println("Identity not found."); return; }
        out.println("Eligible:          " + response.isEligible());
        out.println("Name:              " + response.getName());
        out.println("Date of birth:     " + response.getDateOfBirth());
        out.println("Minimum age met:   " + response.isMinimumAgeMet());
        out.println("Restriction block: " + response.isRestrictionBlock());
        out.println("Reason codes:      " + response.getReasonCodes());
    }
}
