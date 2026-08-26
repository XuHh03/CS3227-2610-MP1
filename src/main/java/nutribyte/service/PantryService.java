package nutribyte.service;

import nutribyte.model.PantryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides pantry operations independently of the user interface.
 */
public class PantryService {
    private final List<PantryItem> items = new ArrayList<>();

    /**
     * Adds one item to the pantry.
     *
     * @param name item name
     * @param quantity number of units
     */
    public void addItem(String name, int quantity) {
        items.add(new PantryItem(name, quantity));
    }

    /**
     * Reduces the quantity of a named pantry item.
     *
     * @param name item name
     * @param quantity number of units consumed
     * @return true if the item was found and updated
     */
    public boolean consumeItem(String name, int quantity) {
        for (PantryItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                item.changeQuantity(-quantity);
                return true;
            }
        }
        return false;
    }

    /**
     * Increases the quantity of a named pantry item.
     *
     * @param name item name
     * @param quantity number of units restocked
     * @return true if the item was found and updated
     */
    public boolean restockItem(String name, int quantity) {
        for (PantryItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                item.changeQuantity(quantity);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the current pantry items in insertion order.
     *
     * @return an unmodifiable view of the pantry items
     */
    public List<PantryItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
