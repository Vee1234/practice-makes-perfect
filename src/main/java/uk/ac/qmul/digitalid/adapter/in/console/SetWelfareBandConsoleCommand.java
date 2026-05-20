package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.in.SetWelfareBandPort;
import uk.ac.qmul.digitalid.application.service.management.SetWelfareBandCommand;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.OperationResult;
import uk.ac.qmul.digitalid.domain.WelfareBand;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class SetWelfareBandConsoleCommand implements ConsoleCommand {

    private final SetWelfareBandPort port;
    private final Organisation requestedBy;

    public SetWelfareBandConsoleCommand(SetWelfareBandPort port, Organisation requestedBy) {
        this.port = port;
        this.requestedBy = requestedBy;
    }

    @Override
    public String getDescription() { return "Set / update welfare band for a Digital ID"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        Optional<WelfareBand> band = ConsoleInput.readEnumChoice(in, out, "Welfare band", WelfareBand.values());
        if (band.isEmpty()) return;

        OperationResult<?> result = port.setWelfareBand(new SetWelfareBandCommand(id.get(), band.get(), requestedBy));
        if (result.isSuccess()) out.println("Welfare band set to " + band.get() + " for: " + id.get().value());
        else                   out.println("Failed: " + result.getError().message());
    }
}
