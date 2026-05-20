package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.in.CreateIdentityPort;
import uk.ac.qmul.digitalid.application.service.management.IdentityCreateCommand;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

public final class CreateIdentityConsoleCommand implements ConsoleCommand {

    private final CreateIdentityPort port;
    private final Organisation requestedBy;

    public CreateIdentityConsoleCommand(CreateIdentityPort port, Organisation requestedBy) {
        this.port = port;
        this.requestedBy = requestedBy;
    }

    @Override
    public String getDescription() { return "Create a new Digital ID"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number (e.g. DID-123456)", DigitalIdNumber::of);
        if (id.isEmpty()) return;
        Optional<LegalName> name = ConsoleInput.readField(in, out, "Full legal name", LegalName::new);
        if (name.isEmpty()) return;
        Optional<LocalDate> dob = ConsoleInput.readField(in, out, "Date of birth (YYYY-MM-DD)", input -> {
            LocalDate date = LocalDate.parse(input);
            if (date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Date of birth cannot be in the future");
            return date;
        });
        if (dob.isEmpty()) return;

        OperationResult<?> result = port.createIdentity(new IdentityCreateCommand(id.get(), name.get(), dob.get(), requestedBy));
        if (result.isSuccess()) out.println("Identity created: " + id.get().value());
        else                   out.println("Failed: " + result.getError().message());
    }
}
