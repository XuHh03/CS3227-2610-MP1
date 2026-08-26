package nutribyte.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;
import org.junit.jupiter.api.Test;

/**
 * Tests pantry operations provided by {@link PantryService}.
 */
class PantryServiceTest {
    @Test
    void addItem_basicItem_addsWithDefaultMetadata() {
        PantryService pantryService = new PantryService();

        pantryService.addItem("Milk", 5);

        assertEquals(1, pantryService.getItems().size());
        assertEquals("Milk", pantryService.getItems().get(0).getName());
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
        assertEquals(Category.GENERAL, pantryService.getItems().get(0).getCategory());
    }

    @Test
    void addItem_withMetadata_preservesCategoryAndExpiryDate() {
        PantryService pantryService = new PantryService();
        LocalDate expiryDate = LocalDate.of(2026, 12, 1);

        pantryService.addItem("Rice", 3, Category.GRAINS, expiryDate);

        assertEquals(Category.GRAINS, pantryService.getItems().get(0).getCategory());
        assertEquals(expiryDate, pantryService.getItems().get(0).getExpiryDate());
    }

    @Test
    void consumeItem_validQuantity_reducesStock() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        PantryOperationResult result = pantryService.consumeItem("Milk", 2);

        assertEquals(PantryOperationResult.SUCCESS, result);
        assertEquals(3, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void consumeItem_itemNameIsCaseInsensitive_reducesStock() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        PantryOperationResult result = pantryService.consumeItem("mIlK", 1);

        assertEquals(PantryOperationResult.SUCCESS, result);
        assertEquals(4, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void consumeItem_zeroOrNegativeQuantity_rejectsOperation() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        PantryOperationResult zeroResult = pantryService.consumeItem("Milk", 0);
        PantryOperationResult negativeResult = pantryService.consumeItem("Milk", -1);

        assertEquals(PantryOperationResult.INVALID_QUANTITY, zeroResult);
        assertEquals(PantryOperationResult.INVALID_QUANTITY, negativeResult);
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void consumeItem_unknownItem_returnsItemNotFound() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        PantryOperationResult result = pantryService.consumeItem("Bread", 1);

        assertEquals(PantryOperationResult.ITEM_NOT_FOUND, result);
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void consumeItem_quantityExceedsStock_returnsInsufficientStock() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        PantryOperationResult result = pantryService.consumeItem("Milk", 6);

        assertEquals(PantryOperationResult.INSUFFICIENT_STOCK, result);
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void restockItem_validQuantity_increasesStock() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        PantryOperationResult result = pantryService.restockItem("milk", 3);

        assertEquals(PantryOperationResult.SUCCESS, result);
        assertEquals(8, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void restockItem_zeroOrNegativeQuantity_rejectsOperation() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        assertEquals(PantryOperationResult.INVALID_QUANTITY, pantryService.restockItem("Milk", 0));
        assertEquals(PantryOperationResult.INVALID_QUANTITY, pantryService.restockItem("Milk", -1));
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void restockItem_unknownItem_returnsItemNotFound() {
        PantryService pantryService = new PantryService();

        assertEquals(PantryOperationResult.ITEM_NOT_FOUND, pantryService.restockItem("Milk", 2));
    }

    @Test
    void deleteItem_existingItem_removesOnlyMatchingItem() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);
        pantryService.addItem("Bread", 2);

        boolean deleted = pantryService.deleteItem("MILK");

        assertEquals(true, deleted);
        assertIterableEquals(List.of("Bread"), pantryService.getItems().stream()
                .map(item -> item.getName())
                .toList());
    }

    @Test
    void deleteItem_unknownItem_returnsFalseAndPreservesItems() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        assertEquals(false, pantryService.deleteItem("Bread"));
        assertEquals(1, pantryService.getItems().size());
    }

    @Test
    void searchItems_matchesNameCategoryAndExpiryIgnoringCase() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Rice", 3, Category.GRAINS, LocalDate.of(2026, 12, 1));
        pantryService.addItem("Milk", 2, Category.DAIRY, LocalDate.of(2026, 9, 15));

        assertEquals(List.of("Rice"), pantryService.searchItems("rice").stream()
                .map(item -> item.getName()).toList());
        assertEquals(List.of("Rice"), pantryService.searchItems("grains").stream()
                .map(item -> item.getName()).toList());
        assertEquals(List.of("Milk"), pantryService.searchItems("2026-09").stream()
                .map(item -> item.getName()).toList());
    }

    @Test
    void searchItems_noMatch_returnsEmptyList() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Rice", 3);

        assertEquals(List.of(), pantryService.searchItems("unknown"));
    }

    @Test
    void filterByCategory_matchingCategory_returnsMatchingItems() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Rice", 3, Category.GRAINS, null);
        pantryService.addItem("Milk", 2, Category.DAIRY, null);

        assertEquals(List.of("Rice"), pantryService.filterByCategory(Category.GRAINS).stream()
                .map(item -> item.getName()).toList());
    }

    @Test
    void filterByExpiryBefore_inclusiveDate_excludesUndatedAndLaterItems() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2, Category.DAIRY, LocalDate.of(2026, 9, 15));
        pantryService.addItem("Rice", 3, Category.GRAINS, LocalDate.of(2026, 12, 1));
        pantryService.addItem("Salt", 1, Category.OTHER, null);

        assertEquals(List.of("Milk"), pantryService.filterByExpiryBefore(LocalDate.of(2026, 9, 15)).stream()
                .map(item -> item.getName()).toList());
    }

    @Test
    void filterByExpiryRange_inclusiveBounds_returnsItemsWithinRange() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Early", 1, Category.OTHER, LocalDate.of(2026, 9, 1));
        pantryService.addItem("Middle", 1, Category.OTHER, LocalDate.of(2026, 9, 15));
        pantryService.addItem("Late", 1, Category.OTHER, LocalDate.of(2026, 10, 1));

        assertEquals(List.of("Middle", "Late"), pantryService
                .filterByExpiryRange(LocalDate.of(2026, 9, 15), null).stream()
                .map(item -> item.getName()).toList());
    }

    @Test
    void constructor_initialItems_preservesInsertionOrder() {
        List<PantryItem> initialItems = List.of(new PantryItem("Rice", 3), new PantryItem("Milk", 2));

        PantryService pantryService = new PantryService(initialItems);

        assertIterableEquals(initialItems, pantryService.getItems());
    }

    @Test
    void getItems_modificationAttempt_throwsUnsupportedOperationException() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        assertThrows(UnsupportedOperationException.class, () -> pantryService.getItems().clear());
    }
}
