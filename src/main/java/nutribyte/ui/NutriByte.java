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
            PantryLoadResult pantryLoadResult = loadPantry(storage);
            UI.showGreeting();
            runCommandLoop(scanner, pantryLoadResult.service(), storage,
                    pantryLoadResult.persistenceAvailable());
        }
    }

    private static PantryLoadResult loadPantry(PantryStorage storage) {
        try {
            return new PantryLoadResult(new PantryService(storage.load()), true);
        } catch (IOException | RuntimeException exception) {
            System.out.println("Could not load pantry data. Starting with an empty pantry.");
            System.out.println("The existing data was not changed; fix the data file before saving again.");
            return new PantryLoadResult(new PantryService(), false);
        }
    }

    private static void runCommandLoop(Scanner scanner, PantryService pantryService,
            PantryStorage storage, boolean persistenceAvailable) {
        while (scanner.hasNextLine()) {
            Parser.ParsedCommand parsedCommand = PARSER.parse(scanner.nextLine());
            if (parsedCommand.command() == Parser.Command.BYE
                    || parsedCommand.command() == Parser.Command.EXIT) {
                System.out.println("Catch you later—keep it fresh!");
                if (persistenceAvailable) {
                    savePantry(pantryService, storage);
                }
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
                System.out.println("I don't recognize that command. Type 'help' to see the available commands.");
            }
            }
            if (persistenceAvailable) {
                savePantry(pantryService, storage);
            }
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
        if (parts.length < 2 || parts.length > 5
                || parts.length == 4 && !"expiry".equalsIgnoreCase(parts[2])
                && !isCategory(parts[2])) {
            System.out.println("Add format: add <name> <quantity> [category] [expiry <YYYY-MM-DD>]");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[1]);
            if (quantity <= 0) {
                System.out.println("Quantity must be a positive whole number. You entered " + parts[1] + ".");
            } else {
            if (parts.length == 2) {
                pantryService.addItem(parts[0], quantity);
            } else if (parts.length == 3) {
                Category category = parseCategory(parts[2]);
                if (category == null) {
                    return;
                }
                pantryService.addItem(parts[0], quantity, category, null);
            } else if (parts.length == 4) {
                Category category = "expiry".equalsIgnoreCase(parts[2])
                        ? Category.GENERAL : parseCategory(parts[2]);
                LocalDate expiryDate = parseExpiryDate(parts[3]);
                if (category == null || expiryDate == null) {
                    return;
                }
                pantryService.addItem(parts[0], quantity, category, expiryDate);
            } else if (parts.length == 5) {
                if (!"expiry".equalsIgnoreCase(parts[3])) {
                    System.out.println("Invalid add format. Use: add <name> <quantity> <category> expiry <YYYY-MM-DD>");
                    return;
                }
                Category category = parseCategory(parts[2]);
                LocalDate expiryDate = parseExpiryDate(parts[4]);
                if (category == null || expiryDate == null) {
                    return;
                }
                pantryService.addItem(parts[0], quantity, category, expiryDate);
            } else {
                System.out.println("Add format: add <name> <quantity> [category] [expiry <YYYY-MM-DD>]");
                return;
            }
            System.out.println("Nice! Added " + parts[0] + " (" + quantity + ") to the pantry.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Quantity '" + parts[1]
                    + "' is invalid. Enter a positive whole number, such as 3.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid item name: " + exception.getMessage());
        }
    }

    private static boolean isCategory(String value) {
        try {
            Category.valueOf(value.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static Category parseCategory(String value) {
        try {
            return Category.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid category '" + value + "'. Choose general, grains, dairy, produce,");
            System.out.println("meat, canned, snacks, or other.");
            return null;
        }
    }

    private static LocalDate parseExpiryDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid expiry date '" + value
                    + "'. Use YYYY-MM-DD and a real calendar date, such as 2026-09-15.");
            return null;
        }
    }

    private static void changeQuantity(String[] parts, PantryService pantryService, boolean restock) {
        boolean indexMode = parts.length == 3 && "index".equalsIgnoreCase(parts[0]);
        String quantityText = indexMode ? parts[2] : parts.length > 1 ? parts[1] : "";
        if (parts.length != 2 && !indexMode) {
            System.out.println("Usage: " + (restock ? "restock" : "consume")
                    + " <name> <positive quantity> or " + (restock ? "restock" : "consume")
                    + " index <index> <positive quantity>");
            return;
        }

        try {
            int index = indexMode ? Integer.parseInt(parts[1]) : -1;
            int quantity = Integer.parseInt(indexMode ? parts[2] : parts[1]);
            PantryOperationResult result;
            if (indexMode) {
                result = restock ? pantryService.restockItem(index, quantity)
                        : pantryService.consumeItem(index, quantity);
            } else {
                result = restock ? pantryService.restockItem(parts[0], quantity)
                        : pantryService.consumeItem(parts[0], quantity);
            }
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println((restock ? "Topped up " : "Marked as consumed ")
                        + (indexMode ? "item " + parts[1] : parts[0]) + " (" + quantity + ").");
            } else if (result == PantryOperationResult.INVALID_QUANTITY) {
                System.out.println("Quantity must be a positive whole number. You entered " + quantityText + ".");
            } else if (result == PantryOperationResult.INVALID_INDEX) {
                System.out.println("Item index " + parts[1] + " is out of range. Use an index shown by 'list'.");
            } else if (result == PantryOperationResult.AMBIGUOUS_ITEM) {
                System.out.println("Multiple pantry items match '" + parts[0]
                        + "'. Use " + (restock ? "restock" : "consume") + " index <index> <quantity>.");
            } else if (result == PantryOperationResult.INSUFFICIENT_STOCK) {
                String target = indexMode ? "item " + parts[1] : parts[0];
                System.out.println("Not enough stock in " + target + " to consume " + quantity + " units.");
            } else {
                System.out.println("Item not found: " + parts[0]);
            }
        } catch (NumberFormatException exception) {
            if (indexMode && !parts[1].matches("[+-]?\\d+")) {
                System.out.println("Item index '" + parts[1]
                        + "' is invalid. Enter a positive whole number.");
            } else {
                System.out.println("Quantity '" + quantityText
                        + "' is invalid. Enter a positive whole number, such as 3.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("Could not update quantity. " + exception.getMessage());
        }
    }

    private static void deleteItem(String[] parts, PantryService pantryService) {
        if (parts.length != 1) {
            System.out.println("Delete format: delete <positive pantry index>.");
            return;
        }

        try {
            int index = Integer.parseInt(parts[0]);
            if (index < 1 || index > pantryService.getItems().size()) {
                System.out.println("Item index " + parts[0] + " is out of range. Use an index shown by 'list'.");
                return;
            }
            PantryItem item = pantryService.getItems().get(index - 1);
            PantryOperationResult result = pantryService.deleteItem(index);
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println("Cleared item " + index + ": " + item);
            } else {
                System.out.println("Item index " + parts[0] + " is out of range. Use an index shown by 'list'.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Item index '" + parts[0] + "' is invalid. Enter a positive whole number.");
        }
    }

    private static void editItem(String[] parts, PantryService pantryService) {
        if (parts.length != 3) {
            System.out.println("Edit format: edit <index> <name|quantity|category|expiry> <value>.");
            return;
        }
        try {
            int index = Integer.parseInt(parts[0]);
            PantryOperationResult result = pantryService.editItem(index, parts[1], parts[2]);
            if (result == PantryOperationResult.SUCCESS) {
                System.out.println("Updated item " + index + "—looking good!");
            } else if (result == PantryOperationResult.INVALID_INDEX) {
                System.out.println("Item index " + parts[0] + " is out of range. Use an index shown by 'list'.");
            } else if (result == PantryOperationResult.INVALID_FIELD) {
                System.out.println("Field '" + parts[1]
                        + "' cannot be edited. Choose name, quantity, category, or expiry.");
            } else {
                System.out.println("Value '" + parts[2] + "' is invalid for " + parts[1] + ".");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Item index '" + parts[0] + "' is invalid. Enter a positive whole number.");
        }
    }

    private static void searchItems(String[] parts, PantryService pantryService) {
        if (parts.length == 0) {
            System.out.println("Search format: search <text>, for example search milk.");
            return;
        }
        String query = String.join(" ", parts);

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
        System.out.println("Here you go! The matching items are shown above. "
                + "Use 'list' to see your full pantry again.");
    }

    private static void filterItems(String[] parts, PantryService pantryService) {
        if (parts.length < 2) {
            System.out.println("Filter format: filter category <category>, expiry-before <date>, or");
            System.out.println("expiry-between <start YYYY-MM-DD> <end YYYY-MM-DD>.");
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
                System.out.println("Filter format: filter category <category>, expiry-before <date>, or");
                System.out.println("expiry-between <start YYYY-MM-DD> <end YYYY-MM-DD>.");
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
                System.out.println("Here you go! The matching items are shown above. "
                        + "Use 'list' to see your full pantry again.");
            }
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid expiry date. Use YYYY-MM-DD and a real calendar date, such as 2026-09-15.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid expiry range: " + exception.getMessage());
        }
    }

    private record PantryLoadResult(PantryService service, boolean persistenceAvailable) {
    }
}
