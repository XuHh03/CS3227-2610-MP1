package nutribyte.model;

/**
 * Represents an item currently stored in the pantry.
 */
public class PantryItem {
    private final String name;
    private int quantity;

    /**
     * Creates a pantry item.
     *
     * @param name item name
     * @param quantity number of units in the pantry
     */
    public PantryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return name + " (" + quantity + ")";
    }
}
