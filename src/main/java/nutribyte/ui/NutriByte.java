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
import java.util.List;

/**
 * Command-line entry point for the NutriByte pantry application.
 *
 * <p>Milestone 1 provides only the application's greeting and exit flow.
 * Pantry operations will be added in later milestones.</p>
 */
public class NutriByte {
    private static final String EXIT_COMMAND = "bye";
    private static final Parser PARSER = new Parser();
    private static final Ui UI = new Ui();

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
            UI.showGreeting();
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

    private static void runCommandLoop(Scanner scanner, PantryService pantryService, PantryStorage storage) {
        while (scanner.hasNextLine()) {
            Parser.ParsedCommand parsedCommand = PARSER.parse(scanner.nextLine());
            String command = parsedCommand.command().name().toLowerCase();
            if (EXIT_COMMAND.equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
                System.out.println("Goodbye! Keep your pantry fresh.");
                savePantry(pantryService, storage);
                return;
            }
            if (command.equalsIgnoreCase("list")) {
                UI.showItems(pantryService.getItems());
            } else if (command.equalsIgnoreCase("add")) {
                addItem(parsedCommand.arguments(), pantryService);
            } else if (command.equalsIgnoreCase("consume")) {
                changeQuantity(parsedCommand.arguments(), pantryService, false);
            } else if (command.equalsIgnoreCase("restock")) {
                changeQuantity(parsedCommand.arguments(), pantryService, true);
            } else if (command.equalsIgnoreCase("delete")) {
                deleteItem(parsedCommand.arguments(), pantryService);
            } else if (command.equalsIgnoreCase("search")) {
                searchItems(parsedCommand.arguments(), pantryService);
            } else if (command.equalsIgnoreCase("filter")) {
                filterItems(parsedCommand.arguments(), pantryService);
            } else if (command.equalsIgnoreCase("edit")) {
                editItem(parsedCommand.arguments(), pantryService);
            } else if (command.equalsIgnoreCase("help")) {
                UI.showHelp();
            } else {
                System.out.println("Unknown command. Try 'add', 'consume', 'restock', 'delete', 'search', 'filter', 'edit', 'list', or 'bye'.");
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

    private static void addItem(String[] parts, PantryService pantryService) {
        if (parts.length != 2 && parts.length != 4) {
            System.out.println("Usage: add <name> <quantity> [category] [expiry YYYY-MM-DD]");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[1]);
            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
            } else {
            if (parts.length == 2) {
                pantryService.addItem(parts[0], quantity);
            } else if (parts.length == 4) {
                Category category = parseCategory(parts[2]);
                LocalDate expiryDate = parseExpiryDate(parts[3]);
                if (category == null || expiryDate == null) {
                    return;
                }
                pantryService.addItem(parts[0], quantity, category, expiryDate);
            } else {
                System.out.println("Usage: add <name> <quantity> [category] [expiry YYYY-MM-DD]");
                return;
            }
            System.out.println("Added: " + parts[0] + " (" + quantity + ")");
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

    private static void changeQuantity(String[] parts, PantryService pantryService, boolean restock) {
        String command = restock ? "restock" : "consume";
        if (parts.length != 2) {
            System.out.println("Usage: " + (restock ? "restock" : "consume") + " <name> <quantity>");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[1]);
            PantryOperationResult result = restock
                    ? pantryService.restockItem(parts[0], quantity)
                    : pantryService.consumeItem(parts[0], quantity);
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println((restock ? "Restocked: " : "Consumed: ")
                        + parts[0] + " (" + quantity + ")");
            } else if (result == PantryOperationResult.INVALID_QUANTITY) {
                System.out.println("Quantity must be greater than zero.");
            } else if (result == PantryOperationResult.INSUFFICIENT_STOCK) {
                System.out.println("Not enough stock to consume " + quantity + " " + parts[0] + ".");
            } else {
                System.out.println("Item not found: " + parts[0]);
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity must be a whole number.");
        }
    }

    private static void deleteItem(String[] parts, PantryService pantryService) {
        if (parts.length == 0) {
            System.out.println("Usage: delete <name>");
            return;
        }
        String name = parts[0];

        if (pantryService.deleteItem(name)) {
            System.out.println("Deleted: " + name);
        } else {
            System.out.println("Item not found: " + name);
        }
    }

    private static void editItem(String[] parts, PantryService pantryService) {
        if (parts.length != 3) {
            System.out.println("Usage: edit <index> name|quantity|category|expiry <value>");
            return;
        }
        try {
            int index = Integer.parseInt(parts[0]);
            PantryOperationResult result = pantryService.editItem(index, parts[1], parts[2]);
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println("Edited item " + index + ".");
            } else if (result == PantryOperationResult.INVALID_INDEX) {
                System.out.println("Item number is out of range.");
            } else if (result == PantryOperationResult.INVALID_FIELD) {
                System.out.println("Editable fields are name, quantity, category, and expiry.");
            } else {
                System.out.println("Invalid value for " + parts[1] + ".");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Item number must be a whole number.");
        }
    }

    private static void searchItems(String[] parts, PantryService pantryService) {
        if (parts.length == 0) {
            System.out.println("Usage: search <text>");
            return;
        }
        String query = parts[0];

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

    private static void filterItems(String[] parts, PantryService pantryService) {
        if (parts.length < 2) {
            System.out.println("Usage: filter category <category>, expiry-before <date>, or expiry-between <start> <end>");
            return;
        }

        try {
            List<PantryItem> matches;
            if (parts[0].equalsIgnoreCase("category") && parts.length == 2) {
                Category category = parseCategory(parts[1]);
                if (category == null) {
                    return;
                }
                matches = pantryService.filterByCategory(category);
            } else if (parts[0].equalsIgnoreCase("expiry-before") && parts.length == 2) {
                matches = pantryService.filterByExpiryBefore(LocalDate.parse(parts[1]));
            } else if (parts[0].equalsIgnoreCase("expiry-between") && parts.length == 3) {
                matches = pantryService.filterByExpiryRange(
                        LocalDate.parse(parts[1]), LocalDate.parse(parts[2]));
            } else {
                System.out.println("Usage: filter category <category>, expiry-before <date>, or expiry-between <start> <end>");
                return;
            }

            if (matches.isEmpty()) {
                System.out.println("No matching items found.");
            } else {
                System.out.println("Filtered items:");
                int itemNumber = 1;
                for (PantryItem item : matches) {
                    System.out.println(itemNumber + ". " + item);
                    itemNumber++;
                }
            }
        } catch (DateTimeParseException exception) {
            System.out.println("Expiry date must use YYYY-MM-DD format.");
        }
    }
}
