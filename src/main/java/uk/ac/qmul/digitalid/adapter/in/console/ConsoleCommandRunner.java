package uk.ac.qmul.digitalid.adapter.in.console;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public final class ConsoleCommandRunner {

    private final Map<String, List<ConsoleCommand>> orgCommands;

    public ConsoleCommandRunner(Map<String, List<ConsoleCommand>> orgCommands) {
        this.orgCommands = Objects.requireNonNull(orgCommands);
    }

    public List<ConsoleCommand> commandsFor(String orgName) {
        return orgCommands.getOrDefault(orgName.toUpperCase(Locale.ROOT), List.of());
    }

    public void run(Scanner in, PrintStream out) {
        List<String> orgs = List.copyOf(orgCommands.keySet());

        out.println("=== Digital ID Platform ===");
        out.println("Select your organisation:");
        for (int i = 0; i < orgs.size(); i++) {
            out.printf("  %d. %s%n", i + 1, orgs.get(i));
        }

        String orgInput = in.nextLine().trim();
        String orgName = resolveSelection(orgInput, orgs);

        if (orgName == null) {
            out.println("Unknown organisation. Exiting.");
            return;
        }

        List<ConsoleCommand> commands = commandsFor(orgName);
        if (commands.isEmpty()) {
            out.println("No operations available for " + orgName + ".");
            return;
        }

        out.println("\nOrganisation: " + orgName);
        out.println("Select an operation:");
        for (int i = 0; i < commands.size(); i++) {
            out.printf("  %d. %s%n", i + 1, commands.get(i).getDescription());
        }

        String opInput = in.nextLine().trim();
        ConsoleCommand command = resolveCommand(opInput, commands);

        if (command == null) {
            out.println("Invalid selection. Exiting.");
            return;
        }

        out.println();
        command.execute(in, out);
    }

    private String resolveSelection(String input, List<String> options) {
        try {
            int index = Integer.parseInt(input) - 1;
            return (index >= 0 && index < options.size()) ? options.get(index) : null;
        } catch (NumberFormatException e) {
            String upper = input.toUpperCase(Locale.ROOT);
            return options.contains(upper) ? upper : null;
        }
    }

    private ConsoleCommand resolveCommand(String input, List<ConsoleCommand> commands) {
        try {
            int index = Integer.parseInt(input) - 1;
            return (index >= 0 && index < commands.size()) ? commands.get(index) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
