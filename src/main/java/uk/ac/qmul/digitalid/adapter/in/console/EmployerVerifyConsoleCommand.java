package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.adapter.in.portal.EmployerPortal;
import uk.ac.qmul.digitalid.adapter.in.portal.RightToWorkResponse;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class EmployerVerifyConsoleCommand implements ConsoleCommand {

    private final EmployerPortal portal;

    public EmployerVerifyConsoleCommand(EmployerPortal portal) {
        this.portal = portal;
    }

    @Override
    public String getDescription() { return "Verify right to work"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        RightToWorkResponse response = portal.verifyRightToWork(id.get());
        if ("NOT_FOUND".equals(response.getReasonCode())) { out.println("Identity not found."); return; }
        out.println("Valid now:     " + response.isValidNow());
        out.println("Name:          " + response.getName());
        out.println("Date of birth: " + response.getDateOfBirth());
        out.println("Reason code:   " + response.getReasonCode());
        out.println("Checked at:    " + response.getCheckedAt());
    }
}
