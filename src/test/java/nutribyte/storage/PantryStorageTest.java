package nutribyte.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;

/**
 * Tests persistence of pantry items in tab-separated storage.
 */
class PantryStorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void saveThenLoad_itemsWithAndWithoutExpiry_preservesData() throws Exception {
        Path file = temporaryDirectory.resolve("nested/pantry.txt");
        PantryStorage storage = new PantryStorage(file);
        List<PantryItem> expected = List.of(
                new PantryItem("Rice", 3, Category.GRAINS, LocalDate.of(2026, 12, 1)),
                new PantryItem("Salt", 1, Category.OTHER, null));

        storage.save(expected);

        List<PantryItem> actual = storage.load();
        assertEquals(2, actual.size());
        assertEquals("Rice", actual.get(0).getName());
        assertEquals(3, actual.get(0).getQuantity());
        assertEquals(Category.GRAINS, actual.get(0).getCategory());
        assertEquals(LocalDate.of(2026, 12, 1), actual.get(0).getExpiryDate());
        assertEquals("Salt", actual.get(1).getName());
        assertEquals(Category.OTHER, actual.get(1).getCategory());
        assertNull(actual.get(1).getExpiryDate());
        assertTrue(Files.exists(file));
    }

    @Test
    void load_missingFile_returnsEmptyList() throws Exception {
        PantryStorage storage = new PantryStorage(temporaryDirectory.resolve("missing.txt"));

        assertTrue(storage.load().isEmpty());
    }

    @Test
    void load_blankLines_ignoresBlankLines() throws Exception {
        Path file = temporaryDirectory.resolve("pantry.txt");
        Files.writeString(file, "Rice\t3\tGRAINS\t2026-12-01\n\n");

        List<PantryItem> items = new PantryStorage(file).load();

        assertEquals(1, items.size());
        assertEquals("Rice", items.get(0).getName());
    }

    @Test
    void load_malformedRecord_throwsRuntimeException() throws Exception {
        Path file = temporaryDirectory.resolve("malformed.txt");
        Files.writeString(file, "Rice\tthree\tGRAINS\t2026-12-01\n");

        assertThrows(RuntimeException.class, () -> new PantryStorage(file).load());
    }

    @Test
    void load_recordWithWrongFieldCount_rejectsFile() throws Exception {
        Path file = temporaryDirectory.resolve("wrong-fields.txt");
        Files.writeString(file, "Rice\t3\tGRAINS\n");

        assertThrows(IllegalArgumentException.class, () -> new PantryStorage(file).load());
    }

    @Test
    void save_emptyList_createsEmptyFile() throws Exception {
        Path file = temporaryDirectory.resolve("empty/pantry.txt");

        new PantryStorage(file).save(List.of());

        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).isEmpty());
    }

    @Test
    void save_nullItems_rejectsInput() {
        PantryStorage storage = new PantryStorage(temporaryDirectory.resolve("pantry.txt"));

        assertThrows(IllegalArgumentException.class, () -> storage.save(null));
    }
}
