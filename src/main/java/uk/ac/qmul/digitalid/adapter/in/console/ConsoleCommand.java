package uk.ac.qmul.digitalid.adapter.in.console;

import java.io.PrintStream;
import java.util.Scanner;

public interface ConsoleCommand {
    String getDescription();
    void execute(Scanner in, PrintStream out);
}
