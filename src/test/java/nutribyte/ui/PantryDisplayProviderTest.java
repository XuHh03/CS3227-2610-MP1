package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;
import nutribyte.storage.PantryStorage;

/**
 * Tests GUI pantry loading and selection logic without starting JavaFX.
 */
class PantryDisplayProviderTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void load_searchSelection_preservesOriginalIndexes() throws Exception {
        Path dataFile = temporaryDirectory.resolve("pantry.txt");
        new PantryStorage(dataFile).save(List.of(
                new PantryItem("Rice", 3, Category.GRAINS, null),
                new PantryItem("Milk", 2, Category.DAIRY, null)));

        PantryDisplay display = new PantryDisplayProvider(dataFile)
                .load(new PantrySelection(PantryView.SEARCH, "milk", null));

        assertEquals(List.of("Milk"), display.items().stream().map(PantryItem::getName).toList());
        assertEquals(List.of(1), display.originalIndexes());
    }

    @Test
    void load_expiryRangeSelection_returnsItemsWithinInclusiveRange() throws Exception {
        Path dataFile = temporaryDirectory.resolve("pantry.txt");
        new PantryStorage(dataFile).save(List.of(
                new PantryItem("Early", 1, Category.OTHER, LocalDate.of(2026, 9, 1)),
                new PantryItem("Milk", 2, Category.DAIRY, LocalDate.of(2026, 9, 15)),
                new PantryItem("Late", 1, Category.OTHER, LocalDate.of(2026, 10, 1))));

        PantryDisplay display = new PantryDisplayProvider(dataFile)
                .load(new PantrySelection(PantryView.EXPIRY_BETWEEN, "2026-09-15", "2026-10-01"));

        assertEquals(List.of("Milk", "Late"), display.items().stream().map(PantryItem::getName).toList());
        assertEquals(List.of(1, 2), display.originalIndexes());
    }
}
