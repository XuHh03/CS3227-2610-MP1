package nutribyte.ui;

import java.util.List;

import nutribyte.model.PantryItem;

/**
 * Handles user-facing output for the NutriByte command-line interface.
 */
public class Ui {
    /**
     * Displays the application greeting and available commands.
     */
    public void showGreeting() {
        System.out.println("Hey! I'm Byte, your pantry sidekick.");
        System.out.println("Ready to keep your shelves fresh?");
        System.out.println("Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],");
        System.out.println("          restock <name> <quantity>, delete <index>, search <text>,");
        System.out.println("          filter category|expiry-before|expiry-between, edit <index> <field> <value>,");
        System.out.println("          help, list, bye");
    }

    /**
     * Displays the available commands and their usage.
     */
    public void showHelp() {
        System.out.println("Byte's pantry toolkit:");
        System.out.println("  add <name> <quantity> [category] [expiry YYYY-MM-DD]");
        System.out.println("      Stock up your pantry.");
        System.out.println("  list");
        System.out.println("      Take a quick look at your shelves.");
        System.out.println("  consume <name> <quantity>");
        System.out.println("      Mark some stock as consumed.");
        System.out.println("  restock <name> <quantity>");
        System.out.println("      Top up an item's stock.");
        System.out.println("  delete <index>");
        System.out.println("      Clear the pantry item at its displayed list index.");
        System.out.println("  search <text>");
        System.out.println("      Find an item on your shelves.");
        System.out.println("  filter category <category>");
        System.out.println("      Show only one food group.");
        System.out.println("  filter expiry-before <date>");
        System.out.println("      Spot items that need attention first.");
        System.out.println("  filter expiry-between <start> <end>");
        System.out.println("      Check a date window for upcoming expiry.");
        System.out.println("  edit <index> name|quantity|category|expiry <value>");
        System.out.println("      Fix an item; use 'none' to clear its expiry date.");
        System.out.println("  help");
        System.out.println("      Ask Byte for a quick command guide.");
        System.out.println("  bye or exit");
        System.out.println("      Let Byte know you're done for now.");
    }

    /**
     * Displays pantry items or an empty-pantry message.
     *
     * @param items items to display
     */
    public void showItems(List<PantryItem> items) {
        if (items.isEmpty()) {
            System.out.println("Your pantry is empty—let's stock it up!");
            return;
        }

        System.out.println("Here's what's on your shelves:");
        int itemNumber = 1;
        for (PantryItem item : items) {
            System.out.println(itemNumber + ". " + item);
            itemNumber++;
        }
    }
}
