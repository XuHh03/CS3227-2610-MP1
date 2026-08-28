package nutribyte.model;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Represents an item currently stored in the pantry.
 */
public final class PantryItem {
    private final String name;
    private final int quantity;
    private final Category category;
    private final LocalDate expiryDate;

    /**
     * Creates a pantry item.
     *
     * @param name item name
     * @param quantity number of units in the pantry
     */
    public PantryItem(String name, int quantity) {
        this(name, quantity, Category.GENERAL, null);
    }

    /**
     * Creates a pantry item with category and expiry metadata.
     *
     * @param name item name
     * @param quantity number of units
     * @param category item category
     * @param expiryDate expiry date, or null when unknown
     */
    public PantryItem(String name, int quantity, Category category, LocalDate expiryDate) {
        validateName(name);
        validateQuantity(quantity);
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null.");
        }
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.expiryDate = expiryDate;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Returns an item with a different name.
     *
     * @param name replacement item name
     * @return a validated item containing the replacement name
     */
    public PantryItem withName(String name) {
        return new PantryItem(name, quantity, category, expiryDate);
    }

    /**
     * Returns an item with a different quantity.
     *
     * @param quantity replacement quantity
     * @return a validated item containing the replacement quantity
     */
    public PantryItem withQuantity(int quantity) {
        return new PantryItem(name, quantity, category, expiryDate);
    }

    /**
     * Returns an item with a different category.
     *
     * @param category replacement category
     * @return a validated item containing the replacement category
     */
    public PantryItem withCategory(Category category) {
        return new PantryItem(name, quantity, category, expiryDate);
    }

    /**
     * Returns an item with a different expiry date.
     *
     * @param expiryDate replacement expiry date, or null when unknown
     * @return a validated item containing the replacement expiry date
     */
    public PantryItem withExpiryDate(LocalDate expiryDate) {
        return new PantryItem(name, quantity, category, expiryDate);
    }

    /**
     * Changes the quantity by the supplied amount.
     *
     * @param amount positive to add stock, negative to consume stock
     */
    public PantryItem withQuantityChange(int amount) {
        long updatedQuantity = (long) quantity + amount;
        if (updatedQuantity < 0 || updatedQuantity > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Quantity must not become negative or overflow.");
        }
        return withQuantity((int) updatedQuantity);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()
                || !name.matches("[\\p{L}\\p{N}](?:[\\p{L}\\p{N} -]*[\\p{L}\\p{N}])?")) {
            throw new IllegalArgumentException("Name must contain letters, numbers, spaces, or hyphens.");
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

    @Override
    public String toString() {
        String details = name + " (" + quantity + ")";
        if (category != Category.GENERAL || expiryDate != null) {
            details += " [" + category.name().toLowerCase(Locale.ROOT);
            if (expiryDate != null) {
                details += ", expires " + expiryDate;
            }
            details += "]";
        }
        return details;
    }
}
