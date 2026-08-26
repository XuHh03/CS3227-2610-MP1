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
        System.out.println("          filter category|expiry-before|expiry-between, list, bye");
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
