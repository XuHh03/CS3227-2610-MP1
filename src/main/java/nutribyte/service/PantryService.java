package nutribyte.service;

import nutribyte.model.PantryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import nutribyte.model.Category;

/**
 * Provides pantry operations independently of the user interface.
 */
public class PantryService {
    private final List<PantryItem> items = new ArrayList<>();

    /**
     * Creates an empty pantry service.
     */
    public PantryService() {
    }

    /**
     * Creates a pantry service with previously loaded items.
     *
     * @param initialItems items to place in the pantry
     */
    public PantryService(List<PantryItem> initialItems) {
        items.addAll(initialItems);
    }

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
     * Adds an item with category and expiry metadata.
     *
     * @param name item name
     * @param quantity number of units
     * @param category item category
     * @param expiryDate expiry date, or null when unknown
     */
    public void addItem(String name, int quantity, Category category, LocalDate expiryDate) {
        items.add(new PantryItem(name, quantity, category, expiryDate));
    }

    /**
     * Reduces the quantity of a named pantry item.
     *
     * @param name item name
     * @param quantity number of units consumed
     * @return true if the item was found and updated
     */
    public PantryOperationResult consumeItem(String name, int quantity) {
        if (quantity <= 0) {
            return PantryOperationResult.INVALID_QUANTITY;
        }
        for (PantryItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                if (quantity > item.getQuantity()) {
                    return PantryOperationResult.INSUFFICIENT_STOCK;
                }
                item.changeQuantity(-quantity);
                return PantryOperationResult.SUCCESS;
            }
        }
        return PantryOperationResult.ITEM_NOT_FOUND;
    }

    /**
     * Increases the quantity of a named pantry item.
     *
     * @param name item name
     * @param quantity number of units restocked
     * @return true if the item was found and updated
     */
    public PantryOperationResult restockItem(String name, int quantity) {
        if (quantity <= 0) {
            return PantryOperationResult.INVALID_QUANTITY;
        }
        for (PantryItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                item.changeQuantity(quantity);
                return PantryOperationResult.SUCCESS;
            }
        }
        return PantryOperationResult.ITEM_NOT_FOUND;
    }

    /**
     * Deletes the first pantry item with the supplied name.
     *
     * @param name item name
     * @return true if an item was deleted
     */
    public boolean deleteItem(String name) {
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).getName().equalsIgnoreCase(name)) {
                items.remove(index);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds items whose names contain the supplied query, ignoring case.
     *
     * @param query text to search for
     * @return matching items in insertion order
     */
    public List<PantryItem> searchItems(String query) {
        List<PantryItem> matches = new ArrayList<>();
        for (PantryItem item : items) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                matches.add(item);
            }
        }
        return Collections.unmodifiableList(matches);
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
