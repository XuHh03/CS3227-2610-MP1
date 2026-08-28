package nutribyte.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;

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
        assert initialItems != null : "Loaded pantry items must not be null";
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
        assert items.get(items.size() - 1).getQuantity() == quantity
                : "A newly added item must retain its requested quantity";
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
        assert items.get(items.size() - 1).getCategory() == category
                : "A newly added item must retain its category";
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
                int previousQuantity = item.getQuantity();
                item.changeQuantity(-quantity);
                assert item.getQuantity() == previousQuantity - quantity
                        && item.getQuantity() >= 0 : "Consuming stock must preserve quantity accounting";
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
                int previousQuantity = item.getQuantity();
                item.changeQuantity(quantity);
                assert item.getQuantity() == previousQuantity + quantity
                        : "Restocking must increase quantity by the requested amount";
                return PantryOperationResult.SUCCESS;
            }
        }
        return PantryOperationResult.ITEM_NOT_FOUND;
    }

    /**
     * Deletes one pantry item selected by its one-based list index.
     *
     * @param index one-based item index
     * @return result describing whether the deletion succeeded
     */
    public PantryOperationResult deleteItem(int index) {
        if (index < 1 || index > items.size()) {
            return PantryOperationResult.INVALID_INDEX;
        }
        items.remove(index - 1);
        return PantryOperationResult.SUCCESS;
    }

    /**
     * Edits one item selected by its one-based list index.
     *
     * @param index one-based item index
     * @param field name, quantity, category, or expiry
     * @param value replacement value; use {@code none} to clear expiry
     * @return result describing whether the edit succeeded
     */
    public PantryOperationResult editItem(int index, String field, String value) {
        if (index < 1 || index > items.size()) {
            return PantryOperationResult.INVALID_INDEX;
        }
        PantryItem item = items.get(index - 1);
        assert item != null : "Every valid pantry index must refer to an item";
        try {
            switch (field.toLowerCase(Locale.ROOT)) {
            case "name" -> item.setName(value);
            case "quantity" -> {
                int quantity = Integer.parseInt(value);
                if (quantity <= 0) {
                    return PantryOperationResult.INVALID_VALUE;
                }
                item.setQuantity(quantity);
            }
            case "category" -> item.setCategory(Category.valueOf(value.toUpperCase(Locale.ROOT)));
            case "expiry" -> item.setExpiryDate("none".equalsIgnoreCase(value) ? null : LocalDate.parse(value));
            default -> {
                return PantryOperationResult.INVALID_FIELD;
            }
            }
        } catch (IllegalArgumentException | DateTimeException exception) {
            return PantryOperationResult.INVALID_VALUE;
        }
        return PantryOperationResult.SUCCESS;
    }

    /**
     * Finds items whose names contain the supplied query, ignoring case.
     *
     * @param query text to search for
     * @return matching items in insertion order
     */
    public List<PantryItem> searchItems(String query) {
        List<PantryItem> matches = new ArrayList<>();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        for (PantryItem item : items) {
            String name = item.getName().toLowerCase(Locale.ROOT);
            String category = item.getCategory().name().toLowerCase(Locale.ROOT);
            String expiryDate = item.getExpiryDate() == null
                    ? ""
                    : item.getExpiryDate().toString();
            if (name.contains(normalizedQuery)
                    || category.contains(normalizedQuery)
                    || expiryDate.contains(normalizedQuery)) {
                matches.add(item);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Returns items in the requested category.
     *
     * @param category category to match
     * @return matching items
     */
    public List<PantryItem> filterByCategory(Category category) {
        List<PantryItem> matches = new ArrayList<>();
        for (PantryItem item : items) {
            if (item.getCategory() == category) {
                matches.add(item);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Returns items expiring on or before the supplied date.
     *
     * @param date inclusive upper expiry-date limit
     * @return matching items
     */
    public List<PantryItem> filterByExpiryBefore(LocalDate date) {
        return filterByExpiryRange(null, date);
    }

    /**
     * Returns items whose expiry dates fall within an inclusive range.
     * Items without an expiry date are excluded.
     *
     * @param start inclusive lower limit, or null
     * @param end inclusive upper limit, or null
     * @return matching items
     */
    public List<PantryItem> filterByExpiryRange(LocalDate start, LocalDate end) {
        List<PantryItem> matches = new ArrayList<>();
        for (PantryItem item : items) {
            LocalDate expiryDate = item.getExpiryDate();
            if (expiryDate != null
                    && (start == null || !expiryDate.isBefore(start))
                    && (end == null || !expiryDate.isAfter(end))) {
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
