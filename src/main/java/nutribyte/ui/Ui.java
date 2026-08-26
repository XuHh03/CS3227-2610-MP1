package nutribyte.ui;

import nutribyte.model.PantryItem;

import java.util.List;

/**
 * Handles user-facing output for the NutriByte command-line interface.
 */
public class Ui {
    /**
     * Displays the application greeting and available commands.
     */
    public void showGreeting() {
        System.out.println("Hello! I'm NutriByte.");
        System.out.println("What can I do for you?");
        System.out.println("Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],");
        System.out.println("          restock <name> <quantity>, delete <name>, search <text>,");
        System.out.println("          filter category|expiry-before|expiry-between, edit <index> <field> <value>,");
        System.out.println("          help, list, bye");
    }

    /**
     * Displays the available commands and their usage.
     */
    public void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  add <name> <quantity> [category] [expiry YYYY-MM-DD]");
        System.out.println("      Add an item to the pantry.");
        System.out.println("  list");
        System.out.println("      Display all pantry items.");
        System.out.println("  consume <name> <quantity>");
        System.out.println("      Reduce an item's quantity.");
        System.out.println("  restock <name> <quantity>");
        System.out.println("      Increase an item's quantity.");
        System.out.println("  delete <name>");
        System.out.println("      Remove an item from the pantry.");
        System.out.println("  search <text>");
        System.out.println("      Search item names.");
        System.out.println("  filter category <category>");
        System.out.println("      Filter items by category.");
        System.out.println("  filter expiry-before <date>");
        System.out.println("      Filter items expiring on or before a date.");
        System.out.println("  filter expiry-between <start> <end>");
        System.out.println("      Filter items within an inclusive expiry-date range.");
        System.out.println("  edit <index> name|quantity|category|expiry <value>");
        System.out.println("      Correct an item; use 'none' to clear its expiry date.");
        System.out.println("  help");
        System.out.println("      Display this help message.");
        System.out.println("  bye or exit");
        System.out.println("      Exit NutriByte.");
    }

    /**
     * Displays pantry items or an empty-pantry message.
     *
     * @param items items to display
     */
    public void showItems(List<PantryItem> items) {
        if (items.isEmpty()) {
            System.out.println("Your pantry is empty.");
            return;
        }

        System.out.println("Pantry items:");
        int itemNumber = 1;
        for (PantryItem item : items) {
            System.out.println(itemNumber + ". " + item);
            itemNumber++;
        }
    }
}
