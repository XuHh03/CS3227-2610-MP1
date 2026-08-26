package nutribyte.ui;

import nutribyte.model.PantryItem;
import nutribyte.service.PantryService;
import nutribyte.service.PantryOperationResult;

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
            PantryService pantryService = new PantryService();
            greetUser();
            runCommandLoop(scanner, pantryService);
        }
    }

    private static void greetUser() {
        System.out.println("Hello! I'm NutriByte.");
        System.out.println("What can I do for you?");
        System.out.println("Commands: add <name> <quantity>, consume <name> <quantity>,");
        System.out.println("          restock <name> <quantity>, list, bye");
    }

    private static void runCommandLoop(Scanner scanner, PantryService pantryService) {
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (EXIT_COMMAND.equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
                System.out.println("Goodbye! Keep your pantry fresh.");
                return;
            }
            if (command.equalsIgnoreCase("list")) {
                listItems(pantryService);
            } else if (command.toLowerCase().startsWith("add ")) {
                addItem(command, pantryService);
            } else if (command.toLowerCase().startsWith("consume ")) {
                changeQuantity(command, pantryService, false);
            } else if (command.toLowerCase().startsWith("restock ")) {
                changeQuantity(command, pantryService, true);
            } else {
                System.out.println("Unknown command. Try 'add', 'consume', 'restock', 'list', or 'bye'.");
            }
        }
    }

    private static void addItem(String command, PantryService pantryService) {
        String[] parts = command.split("\\s+");
        if (parts.length != 3) {
            System.out.println("Usage: add <name> <quantity>");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[2]);
            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
            } else {
                pantryService.addItem(parts[1], quantity);
                System.out.println("Added: " + parts[1] + " (" + quantity + ")");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity must be a whole number.");
        }
    }

    private static void listItems(PantryService pantryService) {
        if (pantryService.getItems().isEmpty()) {
            System.out.println("Your pantry is empty.");
            return;
        }

        System.out.println("Pantry items:");
        int itemNumber = 1;
        for (PantryItem item : pantryService.getItems()) {
            System.out.println(itemNumber + ". " + item);
            itemNumber++;
        }
    }

    private static void changeQuantity(String command, PantryService pantryService, boolean restock) {
        String[] parts = command.split("\\s+");
        if (parts.length != 3) {
            System.out.println("Usage: " + (restock ? "restock" : "consume") + " <name> <quantity>");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[2]);
            PantryOperationResult result = restock
                    ? pantryService.restockItem(parts[1], quantity)
                    : pantryService.consumeItem(parts[1], quantity);
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println((restock ? "Restocked: " : "Consumed: ")
                        + parts[1] + " (" + quantity + ")");
            } else if (result == PantryOperationResult.INVALID_QUANTITY) {
                System.out.println("Quantity must be greater than zero.");
            } else if (result == PantryOperationResult.INSUFFICIENT_STOCK) {
                System.out.println("Not enough stock to consume " + quantity + " " + parts[1] + ".");
            } else {
                System.out.println("Item not found: " + parts[1]);
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity must be a whole number.");
        }
    }
}
