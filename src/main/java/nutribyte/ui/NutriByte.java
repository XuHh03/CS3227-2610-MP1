package nutribyte.ui;

import java.util.Scanner;

/**
 * Command-line entry point for the NutriByte pantry application.
 *
 * <p>Milestone 1 provides only the application's greeting and exit flow.
 * Pantry operations will be added in later milestones.</p>
 */
public class NutriByte {
    private static final String EXIT_COMMAND = "bye";

    /**
     * Starts NutriByte and reads commands until the user exits.
     *
     * @param args command-line arguments, currently unused
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            greetUser();
            runCommandLoop(scanner);
        }
    }

    private static void greetUser() {
        System.out.println("Hello! I'm NutriByte.");
        System.out.println("What can I do for you?");
        System.out.println("Type 'bye' to exit.");
    }

    private static void runCommandLoop(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (EXIT_COMMAND.equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
                System.out.println("Goodbye! Keep your pantry fresh.");
                return;
            }
            System.out.println("I'm still learning pantry commands. Type 'bye' to exit.");
        }
    }
}
