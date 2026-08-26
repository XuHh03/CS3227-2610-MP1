package nutribyte.model;

import java.time.LocalDate;

/**
 * Represents an item currently stored in the pantry.
 */
public class PantryItem {
    private String name;
    private int quantity;
    private Category category;
    private LocalDate expiryDate;

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

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Changes the quantity by the supplied amount.
     *
     * @param amount positive to add stock, negative to consume stock
     */
    public void changeQuantity(int amount) {
        quantity += amount;
    }

    @Override
    public String toString() {
        String details = name + " (" + quantity + ")";
        if (category != Category.GENERAL || expiryDate != null) {
            details += " [" + category.toString().toLowerCase();
            if (expiryDate != null) {
                details += ", expires " + expiryDate;
            }
            details += "]";
        }
        return details;
    }
}
