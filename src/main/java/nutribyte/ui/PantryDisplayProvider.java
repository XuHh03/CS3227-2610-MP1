package nutribyte.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;
import nutribyte.service.PantryService;
import nutribyte.storage.PantryStorage;

/**
 * Loads pantry data and creates the item subset shown by the GUI.
 */
final class PantryDisplayProvider {
    private final Path dataPath;

    PantryDisplayProvider(Path dataPath) {
        this.dataPath = dataPath;
    }

    PantryDisplay load(PantrySelection selection) throws IOException {
        List<PantryItem> allItems = new PantryStorage(dataPath).load();
        PantryService pantryService = new PantryService(allItems);
        List<PantryItem> displayedItems = switch (selection.view()) {
        case ALL -> allItems;
        case SEARCH -> pantryService.searchItems(selection.firstValue());
        case CATEGORY -> pantryService.filterByCategory(Category.valueOf(
                selection.firstValue().toUpperCase(Locale.ROOT)));
        case EXPIRY_BEFORE -> pantryService.filterByExpiryBefore(LocalDate.parse(selection.firstValue()));
        case EXPIRY_BETWEEN -> pantryService.filterByExpiryRange(
                LocalDate.parse(selection.firstValue()), LocalDate.parse(selection.secondValue()));
        };
        List<Integer> originalIndexes = new ArrayList<>();
        for (PantryItem item : displayedItems) {
            originalIndexes.add(allItems.indexOf(item));
        }
        return new PantryDisplay(displayedItems, originalIndexes);
    }
}

enum PantryView {
    ALL,
    SEARCH,
    CATEGORY,
    EXPIRY_BEFORE,
    EXPIRY_BETWEEN
}

record PantrySelection(PantryView view, String firstValue, String secondValue) {
    static PantrySelection all() {
        return new PantrySelection(PantryView.ALL, null, null);
    }
}

record PantryDisplay(List<PantryItem> items, List<Integer> originalIndexes) {
}
