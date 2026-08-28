package nutribyte.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;

/**
 * Stores pantry items in a simple tab-separated text file.
 */
public class PantryStorage {
    private final Path filePath;

    /**
     * Creates storage backed by the supplied file.
     *
     * @param filePath file used to save and load pantry data
     */
    public PantryStorage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads pantry items. A missing file represents an empty pantry.
     *
     * @return loaded items
     * @throws IOException if the file cannot be read
     */
    public List<PantryItem> load() throws IOException {
        List<PantryItem> items = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return items;
        }

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split("\\t", -1);
            if (fields.length != 4) {
                throw new IllegalArgumentException("Persisted pantry records must contain four fields.");
            }
            Category category = Category.valueOf(fields[2]);
            LocalDate expiryDate = fields[3].isBlank() ? null : LocalDate.parse(fields[3]);
            items.add(new PantryItem(fields[0], Integer.parseInt(fields[1]), category, expiryDate));
        }
        return items;
    }

    /**
     * Saves all pantry items, replacing the previous file contents.
     *
     * @param items items to save
     * @throws IOException if the file cannot be written
     */
    public void save(List<PantryItem> items) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<String> lines = new ArrayList<>();
        for (PantryItem item : items) {
            assert item != null : "The pantry must not contain null items when saving";
            assert item.getCategory() != null : "Every persisted item must have a category";
            String expiry = item.getExpiryDate() == null ? "" : item.getExpiryDate().toString();
            lines.add(item.getName() + "\t" + item.getQuantity() + "\t"
                    + item.getCategory().name() + "\t" + expiry);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }
}
