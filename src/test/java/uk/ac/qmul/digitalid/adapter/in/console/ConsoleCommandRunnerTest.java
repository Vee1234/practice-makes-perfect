package uk.ac.qmul.digitalid.adapter.in.console;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleCommandRunnerTest {

    private static final ConsoleCommand VERIFY_CMD = command("Verify identity");
    private static final ConsoleCommand KYC_CMD    = command("Check KYC");
    private static final ConsoleCommand AUDIT_CMD  = command("View audit log");

    private static final Map<String, List<ConsoleCommand>> ORG_COMMANDS = Map.of(
            "EMPLOYER", List.of(VERIFY_CMD),
            "BANK",     List.of(KYC_CMD),
            "AUDITOR",  List.of(AUDIT_CMD)
    );

    private final ConsoleCommandRunner runner = new ConsoleCommandRunner(ORG_COMMANDS);

    // commandsFor resolution

    @Test
    void commandsFor_shouldReturnCommands_forKnownOrg() {
        assertThat(runner.commandsFor("EMPLOYER")).containsExactly(VERIFY_CMD);
    }

    @Test
    void commandsFor_shouldBeCaseInsensitive() {
        assertThat(runner.commandsFor("employer")).containsExactly(VERIFY_CMD);
        assertThat(runner.commandsFor("Employer")).containsExactly(VERIFY_CMD);
    }

    @Test
    void commandsFor_shouldReturnEmptyList_forUnknownOrg() {
        assertThat(runner.commandsFor("UNKNOWN_ORG")).isEmpty();
    }

    // run — navigation and execution

    @Test
    void run_shouldExecuteCommand_whenValidOrgAndOperationSelected() {
        StringBuilder executed = new StringBuilder();
        ConsoleCommand trackedCmd = new ConsoleCommand() {
            public String getDescription() { return "Tracked"; }
            public void execute(Scanner in, PrintStream out) { executed.append("executed"); }
        };
        ConsoleCommandRunner singleRunner = new ConsoleCommandRunner(
                Map.of("EMPLOYER", List.of(trackedCmd)));

        simulate(singleRunner, "1\n1\n0\n");

        assertThat(executed.toString()).isEqualTo("executed");
    }

    @Test
    void run_shouldRetryOrgMenu_whenSelectionOutOfRange() {
        String output = simulate(runner, "99\n0\n");
        assertThat(output).contains("Unknown organisation");
    }

    @Test
    void run_shouldReturnToOrgMenu_whenInvalidOperationEntered() {
        // selects org 1, enters invalid op 99, loops back to org menu, then exits
        String output = simulate(runner, "1\n99\n0\n");
        assertThat(output).contains("Invalid selection");
    }

    @Test
    void run_shouldReturnToOrgMenu_whenUserEntersZeroAtOperationMenu() {
        // selects org 1, enters 0 to go back, then exits from org menu
        String output = simulate(runner, "1\n0\n0\n");
        assertThat(output).contains("Digital ID Platform");
    }

    @Test
    void run_shouldExit_whenUserEntersZeroAtOrgMenu() {
        String output = simulate(runner, "0\n");
        assertThat(output).contains("Digital ID Platform");
    }

    @Test
    void run_shouldAcceptOrgByName_caseInsensitive() {
        StringBuilder executed = new StringBuilder();
        ConsoleCommand trackedCmd = new ConsoleCommand() {
            public String getDescription() { return "Tracked"; }
            public void execute(Scanner in, PrintStream out) { executed.append("executed"); }
        };
        ConsoleCommandRunner singleRunner = new ConsoleCommandRunner(
                Map.of("EMPLOYER", List.of(trackedCmd)));

        simulate(singleRunner, "employer\n1\n0\n");

        assertThat(executed.toString()).isEqualTo("executed");
    }

    // helpers

    private static ConsoleCommand command(String description) {
        return new ConsoleCommand() {
            public String getDescription() { return description; }
            public void execute(Scanner in, PrintStream out) {}
        };
    }

    private String simulate(ConsoleCommandRunner r, String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        r.run(new Scanner(input), new PrintStream(bytes));
        return bytes.toString();
    }
}
