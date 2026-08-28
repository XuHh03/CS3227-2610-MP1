package nutribyte.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;

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
    void consumeItem_duplicateName_returnsAmbiguousItemWithoutChangingStock() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);
        pantryService.addItem("Milk", 3);

        assertEquals(PantryOperationResult.AMBIGUOUS_ITEM, pantryService.consumeItem("Milk", 1));
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
        assertEquals(3, pantryService.getItems().get(1).getQuantity());
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
    void restockItem_duplicateName_returnsAmbiguousItemWithoutChangingStock() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);
        pantryService.addItem("Milk", 3);

        assertEquals(PantryOperationResult.AMBIGUOUS_ITEM, pantryService.restockItem("Milk", 1));
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
        assertEquals(3, pantryService.getItems().get(1).getQuantity());
    }

    @Test
    void consumeItem_byIndex_updatesSelectedDuplicate() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);
        pantryService.addItem("Milk", 3);

        assertEquals(PantryOperationResult.SUCCESS, pantryService.consumeItem(2, 2));
        assertEquals(5, pantryService.getItems().get(0).getQuantity());
        assertEquals(1, pantryService.getItems().get(1).getQuantity());
    }

    @Test
    void deleteItem_index_removesExactlySelectedItem() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);
        pantryService.addItem("Milk", 2);
        pantryService.addItem("Bread", 1);

        PantryOperationResult result = pantryService.deleteItem(2);

        assertEquals(PantryOperationResult.SUCCESS, result);
        assertIterableEquals(List.of("Milk", "Bread"), pantryService.getItems().stream()
                .map(item -> item.getName())
                .toList());
    }

    @Test
    void deleteItem_invalidIndex_returnsInvalidIndexAndPreservesItems() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        assertEquals(PantryOperationResult.INVALID_INDEX, pantryService.deleteItem(0));
        assertEquals(PantryOperationResult.INVALID_INDEX, pantryService.deleteItem(2));
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
    void searchItems_multiWordQuery_matchesCompletePhrase() {
        PantryService pantryService = new PantryService(List.of(new PantryItem("Red Apples", 2)));

        assertEquals(List.of("Red Apples"), pantryService.searchItems("red apples").stream()
                .map(item -> item.getName()).toList());
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
    void filterByExpiryRange_reversedDates_rejectsInvalidRange() {
        PantryService pantryService = new PantryService();

        assertThrows(IllegalArgumentException.class, () -> pantryService.filterByExpiryRange(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 9, 1)));
    }

    @Test
    void constructor_initialItems_preservesInsertionOrder() {
        List<PantryItem> initialItems = List.of(new PantryItem("Rice", 3), new PantryItem("Milk", 2));

        PantryService pantryService = new PantryService(initialItems);

        assertIterableEquals(initialItems, pantryService.getItems());
    }

    @Test
    void constructor_nullItems_rejectsInput() {
        assertThrows(IllegalArgumentException.class, () -> new PantryService(null));
    }

    @Test
    void getItems_modificationAttempt_throwsUnsupportedOperationException() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 5);

        assertThrows(UnsupportedOperationException.class, () -> pantryService.getItems().clear());
    }

    @Test
    void editItem_duplicateNames_usesOneBasedIndex() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2);
        pantryService.addItem("Milk", 3, Category.DAIRY, LocalDate.of(2026, 1, 1));

        PantryOperationResult result = pantryService.editItem(2, "quantity", "7");

        assertEquals(PantryOperationResult.SUCCESS, result);
        assertEquals(2, pantryService.getItems().get(0).getQuantity());
        assertEquals(7, pantryService.getItems().get(1).getQuantity());
    }

    @Test
    void editItem_allSupportedFields_updatesItem() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2);

        assertEquals(PantryOperationResult.SUCCESS, pantryService.editItem(1, "name", "Fresh Milk"));
        assertEquals(PantryOperationResult.SUCCESS, pantryService.editItem(1, "quantity", "4"));
        assertEquals(PantryOperationResult.SUCCESS, pantryService.editItem(1, "category", "dairy"));
        assertEquals(PantryOperationResult.SUCCESS, pantryService.editItem(1, "expiry", "2026-01-01"));

        PantryItem item = pantryService.getItems().get(0);
        assertEquals("Fresh Milk", item.getName());
        assertEquals(4, item.getQuantity());
        assertEquals(Category.DAIRY, item.getCategory());
        assertEquals(LocalDate.of(2026, 1, 1), item.getExpiryDate());
    }

    @Test
    void editItem_expiryNone_clearsExpiryDate() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2, Category.DAIRY, LocalDate.of(2026, 1, 1));

        PantryOperationResult result = pantryService.editItem(1, "expiry", "none");

        assertEquals(PantryOperationResult.SUCCESS, result);
        assertEquals(null, pantryService.getItems().get(0).getExpiryDate());
    }

    @Test
    void editItem_invalidIndexFieldOrValue_doesNotChangeItem() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2);

        assertEquals(PantryOperationResult.INVALID_INDEX, pantryService.editItem(0, "name", "Bread"));
        assertEquals(PantryOperationResult.INVALID_FIELD, pantryService.editItem(1, "colour", "white"));
        assertEquals(PantryOperationResult.INVALID_VALUE, pantryService.editItem(1, "quantity", "0"));
        assertEquals(PantryOperationResult.INVALID_VALUE, pantryService.editItem(1, "expiry", "tomorrow"));
        assertEquals("Milk", pantryService.getItems().get(0).getName());
        assertEquals(2, pantryService.getItems().get(0).getQuantity());
    }

    @Test
    void editItem_nullFieldOrValue_returnsSpecificValidationResult() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2);

        assertEquals(PantryOperationResult.INVALID_FIELD, pantryService.editItem(1, null, "Milk"));
        assertEquals(PantryOperationResult.INVALID_VALUE, pantryService.editItem(1, "name", null));
        assertEquals("Milk", pantryService.getItems().get(0).getName());
    }

    @Test
    void searchItems_nullOrBlankQuery_returnsNoMatches() {
        PantryService pantryService = new PantryService();
        pantryService.addItem("Milk", 2);

        assertEquals(List.of(), pantryService.searchItems(null));
        assertEquals(List.of(), pantryService.searchItems("   "));
    }
}
