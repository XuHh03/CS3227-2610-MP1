package nutribyte.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests pantry item state and display formatting.
 */
class PantryItemTest {
    @Test
    void constructor_basicItem_usesGeneralCategoryAndNoExpiry() {
        PantryItem item = new PantryItem("Rice", 3);

        assertEquals("Rice", item.getName());
        assertEquals(3, item.getQuantity());
        assertEquals(Category.GENERAL, item.getCategory());
        assertEquals(null, item.getExpiryDate());
        assertEquals("Rice (3)", item.toString());
    }

    @Test
    void toString_metadataItem_includesCategoryAndExpiry() {
        PantryItem item = new PantryItem("Milk", 2, Category.DAIRY, LocalDate.of(2026, 9, 15));

        assertEquals("Milk (2) [dairy, expires 2026-09-15]", item.toString());
    }

    @Test
    void withMethods_updateAllItemFields() {
        PantryItem item = new PantryItem("Milk", 2);
        LocalDate expiryDate = LocalDate.of(2026, 9, 15);

        item = item.withName("Fresh Milk");
        item = item.withQuantity(4);
        item = item.withCategory(Category.DAIRY);
        item = item.withExpiryDate(expiryDate);
        item = item.withQuantityChange(-1);

        assertEquals("Fresh Milk", item.getName());
        assertEquals(3, item.getQuantity());
        assertEquals(Category.DAIRY, item.getCategory());
        assertEquals(expiryDate, item.getExpiryDate());
    }

    @Test
    void constructor_invalidNameOrQuantity_rejectsItem() {
        assertThrows(IllegalArgumentException.class, () -> new PantryItem("", 1));
        assertThrows(IllegalArgumentException.class, () -> new PantryItem("milk!", 1));
        assertThrows(IllegalArgumentException.class, () -> new PantryItem("Milk", 0));
    }

    @Test
    void changeQuantity_belowZeroOrOverflow_rejectsChange() {
        PantryItem item = new PantryItem("Milk", 2);

        assertThrows(IllegalArgumentException.class, () -> item.withQuantityChange(-3));
        assertThrows(IllegalArgumentException.class, () -> item.withQuantityChange(Integer.MAX_VALUE));
        assertEquals(2, item.getQuantity());
    }
}
