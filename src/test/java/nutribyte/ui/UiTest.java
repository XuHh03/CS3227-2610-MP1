package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import nutribyte.model.Category;
import nutribyte.model.PantryItem;

/**
 * Tests user-facing command-line output branches.
 */
class UiTest {
    @Test
    void showGreeting_includesByteIdentityAndCommandSummary() {
        String output = captureOutput(() -> new Ui().showGreeting());

        assertTrue(output.contains("Byte, your pantry sidekick"));
        assertTrue(output.contains("delete <index>"));
    }

    @Test
    void showHelp_includesFriendlyDescriptionsForAllCommands() {
        String output = captureOutput(() -> new Ui().showHelp());

        assertTrue(output.contains("Byte's pantry toolkit:"));
        assertTrue(output.contains("Stock up your pantry."));
        assertTrue(output.contains("Let Byte know you're done for now."));
    }

    @Test
    void showItems_emptyAndPopulatedPantries_displayExpectedMessages() {
        Ui ui = new Ui();
        PantryItem milk = new PantryItem("Milk", 2, Category.DAIRY, LocalDate.of(2026, 9, 15));
        String emptyOutput = captureOutput(() -> ui.showItems(List.of()));
        String populatedOutput = captureOutput(() -> ui.showItems(List.of(milk)));

        assertTrue(emptyOutput.contains("Your pantry is empty"));
        assertTrue(populatedOutput.contains("Here's what's on your shelves:"));
        assertTrue(populatedOutput.contains("1. Milk (2) [dairy, expires 2026-09-15]"));
    }

    private String captureOutput(Runnable action) {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            action.run();
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOutput);
        }
    }
}
