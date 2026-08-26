package nutribyte.ui;

import nutribyte.model.PantryItem;
import nutribyte.model.Category;
import nutribyte.service.PantryService;
import nutribyte.service.PantryOperationResult;
import nutribyte.storage.PantryStorage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.io.IOException;
import java.nio.file.Path;
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
        Path dataPath = Path.of(System.getProperty("nutribyte.dataFile", "data/pantry.txt"));
        PantryStorage storage = new PantryStorage(dataPath);
        try (Scanner scanner = new Scanner(System.in)) {
            PantryService pantryService = loadPantry(storage);
            greetUser();
            runCommandLoop(scanner, pantryService, storage);
        }
    }

    private static PantryService loadPantry(PantryStorage storage) {
        try {
            return new PantryService(storage.load());
        } catch (IOException | RuntimeException exception) {
            System.out.println("Could not load pantry data. Starting with an empty pantry.");
            return new PantryService();
        }
    }

    private static void greetUser() {
        System.out.println("Hello! I'm NutriByte.");
        System.out.println("What can I do for you?");
        System.out.println("Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],");
        System.out.println("          restock <name> <quantity>, delete <name>, search <text>,");
        System.out.println("          list, bye");
    }

    private static void runCommandLoop(Scanner scanner, PantryService pantryService, PantryStorage storage) {
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (EXIT_COMMAND.equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
                System.out.println("Goodbye! Keep your pantry fresh.");
                savePantry(pantryService, storage);
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
            } else if (command.toLowerCase().startsWith("delete ")) {
                deleteItem(command, pantryService);
            } else if (command.toLowerCase().startsWith("search ")) {
                searchItems(command, pantryService);
            } else {
                System.out.println("Unknown command. Try 'add', 'consume', 'restock', 'delete', 'search', 'list', or 'bye'.");
            }
            savePantry(pantryService, storage);
        }
    }

    private static void savePantry(PantryService pantryService, PantryStorage storage) {
        try {
            storage.save(pantryService.getItems());
        } catch (IOException exception) {
            System.out.println("Could not save pantry data.");
        }
    }

    private static void addItem(String command, PantryService pantryService) {
        String[] parts = command.split("\\s+");
        if (parts.length != 3 && parts.length != 5) {
            System.out.println("Usage: add <name> <quantity> [category] [expiry YYYY-MM-DD]");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[2]);
            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
            } else {
            if (parts.length == 3) {
                pantryService.addItem(parts[1], quantity);
            } else if (parts.length == 5) {
                Category category = parseCategory(parts[3]);
                LocalDate expiryDate = parseExpiryDate(parts[4]);
                if (category == null || expiryDate == null) {
                    return;
                }
                pantryService.addItem(parts[1], quantity, category, expiryDate);
            } else {
                System.out.println("Usage: add <name> <quantity> [category] [expiry YYYY-MM-DD]");
                return;
            }
            System.out.println("Added: " + parts[1] + " (" + quantity + ")");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity must be a whole number.");
        }
    }

    private static Category parseCategory(String value) {
        try {
            return Category.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.out.println("Unknown category. Use general, grains, dairy, produce, meat, canned, snacks, or other.");
            return null;
        }
    }

    private static LocalDate parseExpiryDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            System.out.println("Expiry date must use YYYY-MM-DD format.");
            return null;
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

    private static void deleteItem(String command, PantryService pantryService) {
        String name = command.substring("delete".length()).trim();
        if (name.isEmpty()) {
            System.out.println("Usage: delete <name>");
            return;
        }

        if (pantryService.deleteItem(name)) {
            System.out.println("Deleted: " + name);
        } else {
            System.out.println("Item not found: " + name);
        }
    }

    private static void searchItems(String command, PantryService pantryService) {
        String query = command.substring("search".length()).trim();
        if (query.isEmpty()) {
            System.out.println("Usage: search <text>");
            return;
        }

        if (pantryService.searchItems(query).isEmpty()) {
            System.out.println("No matching items found.");
            return;
        }

        System.out.println("Matching items:");
        int itemNumber = 1;
        for (PantryItem item : pantryService.searchItems(query)) {
            System.out.println(itemNumber + ". " + item);
            itemNumber++;
        }
    }
}
