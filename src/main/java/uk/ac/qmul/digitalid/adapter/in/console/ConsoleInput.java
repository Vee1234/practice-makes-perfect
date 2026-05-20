package uk.ac.qmul.digitalid.adapter.in.console;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

final class ConsoleInput {

    private ConsoleInput() {}

    static <T> Optional<T> readField(Scanner in, PrintStream out, String prompt, Function<String, T> parser) {
        while (true) {
            out.print(prompt + " (0 to go back): ");
            if (!in.hasNextLine()) return Optional.empty();
            String input = in.nextLine().trim();
            if (input.equals("0")) return Optional.empty();
            try {
                return Optional.of(parser.apply(input));
            } catch (Exception e) {
                out.println("  Invalid: " + e.getMessage() + ". Please try again.");
            }
        }
    }

    static <E> Optional<E> readEnumChoice(Scanner in, PrintStream out, String prompt, E[] values) {
        out.println(prompt + ":");
        for (int i = 0; i < values.length; i++) {
            out.printf("  %d. %s%n", i + 1, values[i]);
        }
        return readField(in, out, "Select", input -> {
            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= values.length) throw new IllegalArgumentException("Invalid selection");
            return values[index];
        });
    }
}
