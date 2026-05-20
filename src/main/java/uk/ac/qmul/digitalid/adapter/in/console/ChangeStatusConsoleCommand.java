package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.in.ChangeStatusPort;
import uk.ac.qmul.digitalid.application.service.management.ChangeStatusCommand;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class ChangeStatusConsoleCommand implements ConsoleCommand {

    private final ChangeStatusPort port;
    private final IdentityStatus targetStatus;
    private final String description;
    private final String successVerb;
    private final Organisation requestedBy;

    public ChangeStatusConsoleCommand(ChangeStatusPort port, IdentityStatus targetStatus,
                                      String description, String successVerb, Organisation requestedBy) {
        this.port = port;
        this.targetStatus = targetStatus;
        this.description = description;
        this.successVerb = successVerb;
        this.requestedBy = requestedBy;
    }

    @Override
    public String getDescription() { return description; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        OperationResult<?> result = port.changeStatus(new ChangeStatusCommand(id.get(), targetStatus, requestedBy));
        if (result.isSuccess()) out.println("Identity " + successVerb + ": " + id.get().value());
        else                   out.println("Failed: " + result.getError().message());
    }
}
