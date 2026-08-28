package nutribyte.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;
import nutribyte.service.PantryOperationResult;
import nutribyte.service.PantryService;
import nutribyte.storage.PantryStorage;

/**
 * Command-line entry point for the NutriByte pantry application.
 *
 * <p>The command-line interface supports pantry inventory, quantity, metadata,
 * search, filtering, editing, and persistence operations.</p>
 */
public class NutriByte {
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
            if (parsedCommand.command() == Parser.Command.BYE
                    || parsedCommand.command() == Parser.Command.EXIT) {
                System.out.println("Catch you later—keep it fresh!");
                savePantry(pantryService, storage);
                return;
            }
            switch (parsedCommand.command()) {
            case LIST -> {
                UI.showItems(pantryService.getItems());
            }
            case ADD -> {
                addItem(parsedCommand.arguments(), pantryService);
            }
            case CONSUME -> {
                changeQuantity(parsedCommand.arguments(), pantryService, false);
            }
            case RESTOCK -> {
                changeQuantity(parsedCommand.arguments(), pantryService, true);
            }
            case DELETE -> {
                deleteItem(parsedCommand.arguments(), pantryService);
            }
            case SEARCH -> {
                searchItems(parsedCommand.arguments(), pantryService);
            }
            case FILTER -> {
                filterItems(parsedCommand.arguments(), pantryService);
            }
            case EDIT -> {
                editItem(parsedCommand.arguments(), pantryService);
            }
            case HELP -> {
                UI.showHelp();
            }
            default -> {
                System.out.println("Hmm, I don't recognize that command. Try 'add', 'consume', 'restock',");
                System.out.println("'filter', 'edit', 'list', or 'bye'.");
            }
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
            System.out.println("Nice! Added " + parts[0] + " (" + quantity + ") to the pantry.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity must be a whole number.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid item: " + exception.getMessage());
        }
    }

    private static Category parseCategory(String value) {
        try {
            return Category.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            System.out.println("Unknown category. Use general, grains, dairy, produce, meat, canned,");
            System.out.println("snacks, or other.");
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
                System.out.println((restock ? "Topped up " : "Marked as consumed ")
                        + parts[0] + " (" + quantity + ").");
            } else if (result == PantryOperationResult.INVALID_QUANTITY) {
                System.out.println("Quantity must be greater than zero.");
            } else if (result == PantryOperationResult.INSUFFICIENT_STOCK) {
                System.out.println("Not enough stock to consume " + quantity + " " + parts[0] + ".");
            } else {
                System.out.println("Item not found: " + parts[0]);
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity must be a whole number.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Could not update quantity: " + exception.getMessage());
        }
    }

    private static void deleteItem(String[] parts, PantryService pantryService) {
        if (parts.length != 1) {
            System.out.println("Usage: delete <index>");
            return;
        }

        try {
            int index = Integer.parseInt(parts[0]);
            if (index < 1 || index > pantryService.getItems().size()) {
                System.out.println("Item number is out of range.");
                return;
            }
            PantryItem item = pantryService.getItems().get(index - 1);
            PantryOperationResult result = pantryService.deleteItem(index);
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println("Cleared item " + index + ": " + item);
            } else {
                System.out.println("Item number is out of range.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Item number must be a whole number.");
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
                System.out.println("Updated item " + index + "—looking good!");
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
            System.out.println("Usage: filter category <category>, expiry-before <date>, or");
            System.out.println("expiry-between <start> <end>");
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
                System.out.println("Usage: filter category <category>, expiry-before <date>, or");
                System.out.println("expiry-between <start> <end>");
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
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid expiry range: " + exception.getMessage());
        }
    }
}
