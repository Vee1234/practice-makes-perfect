package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.in.FindIdentityPort;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class ViewIdentityConsoleCommand implements ConsoleCommand {

    private final FindIdentityPort port;
    private final Organisation requestedBy;

    public ViewIdentityConsoleCommand(FindIdentityPort port, Organisation requestedBy) {
        this.port = port;
        this.requestedBy = requestedBy;
    }

    @Override
    public String getDescription() { return "View all information for a Digital ID"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        OperationResult<DigitalId> result = port.findById(requestedBy, id.get());
        if (!result.isSuccess()) { out.println("Identity not found."); return; }

        DigitalId identity = result.getPayload();
        out.println("ID:                 " + identity.getId().value());
        out.println("Name:               " + identity.getCurrentLegalName().value());
        out.println("Date of birth:      " + identity.getDateOfBirth());
        out.println("Status:             " + identity.getStatus());
        out.println("Welfare band:       " + identity.getWelfareBand());
        out.println("Created at:         " + identity.getCreatedAt());
        out.println("Updated at:         " + identity.getUpdatedAt());
        if (identity.getRestrictions().isEmpty()) {
            out.println("Restrictions:       none");
        } else {
            out.println("Restrictions:");
            identity.getRestrictions().forEach(r ->
                    out.printf("  - %-20s from %s to %s (%s)%n",
                            r.getType(), r.getStartsOn(),
                            r.getEndsOn() != null ? r.getEndsOn() : "indefinite",
                            r.getReasonCode()));
        }
    }
}
