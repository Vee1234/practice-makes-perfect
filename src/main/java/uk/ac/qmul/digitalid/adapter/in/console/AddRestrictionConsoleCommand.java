package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.port.in.AddRestrictionPort;
import uk.ac.qmul.digitalid.application.service.management.AddRestrictionCommand;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.OperationResult;
import uk.ac.qmul.digitalid.domain.Restriction;
import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

public final class AddRestrictionConsoleCommand implements ConsoleCommand {

    private final AddRestrictionPort port;
    private final Organisation requestedBy;

    public AddRestrictionConsoleCommand(AddRestrictionPort port, Organisation requestedBy) {
        this.port = port;
        this.requestedBy = requestedBy;
    }

    @Override
    public String getDescription() { return "Add a restriction to a Digital ID"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        Optional<RestrictionType> type = ConsoleInput.readEnumChoice(in, out, "Restriction type", RestrictionType.values());
        if (type.isEmpty()) return;

        Optional<String> reason = ConsoleInput.readField(in, out, "Reason (short description)", input -> {
            if (input.isBlank()) throw new IllegalArgumentException("Reason cannot be blank");
            return input;
        });
        if (reason.isEmpty()) return;

        Optional<Optional<LocalDate>> endDate = readOptionalFutureDate(in, out);
        if (endDate.isEmpty()) return;

        Restriction restriction = new Restriction(type.get(), LocalDate.now(), endDate.get().orElse(null), reason.get());
        OperationResult<?> result = port.addRestriction(new AddRestrictionCommand(id.get(), restriction, requestedBy));
        if (result.isSuccess()) out.println("Restriction added to: " + id.get().value());
        else                   out.println("Failed: " + result.getError().message());
    }

    // outer empty = go back; inner empty = indefinite; inner present = specific date
    private Optional<Optional<LocalDate>> readOptionalFutureDate(Scanner in, PrintStream out) {
        while (true) {
            out.print("End date (YYYY-MM-DD, 'none' for indefinite, 0 to go back): ");
            if (!in.hasNextLine()) return Optional.empty();
            String input = in.nextLine().trim();
            if (input.equals("0")) return Optional.empty();
            if (input.equalsIgnoreCase("none")) return Optional.of(Optional.empty());
            try {
                LocalDate date = LocalDate.parse(input);
                if (date.isBefore(LocalDate.now())) { out.println("  Invalid: End date cannot be in the past."); continue; }
                return Optional.of(Optional.of(date));
            } catch (Exception e) {
                out.println("  Invalid: " + e.getMessage());
            }
        }
    }
}
