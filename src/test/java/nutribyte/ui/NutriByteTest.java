package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests representative end-to-end command-line workflows.
 */
class NutriByteTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void main_duplicateNames_deleteByIndexRemovesSelectedItem() {
        String output = runApplication("add milk 2\nadd milk 3 dairy 2026-09-15\n"
                + "delete 2\nlist\nbye\n");

        assertTrue(output.contains("Cleared item 2: milk (3) [dairy, expires 2026-09-15]"));
        assertTrue(output.contains("1. milk (2)"));
        assertTrue(!output.contains("2. milk (3) [dairy, expires 2026-09-15]"));
    }

    @Test
    void main_searchCommand_displaysMatchingItems() {
        String output = runApplication("add rice 3\nadd milk 2\nsearch rice\nbye\n");

        assertTrue(output.contains("Matching items:"));
        assertTrue(output.contains("1. rice (3)"));
    }

    private String runApplication(String input) {
        String propertyName = "nutribyte.dataFile";
        String previousDataFile = System.getProperty(propertyName);
        PrintStream originalOutput = System.out;
        java.io.InputStream originalInput = System.in;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setProperty(propertyName, temporaryDirectory.resolve("pantry.txt").toString());
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            NutriByte.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOutput);
            System.setIn(originalInput);
            if (previousDataFile == null) {
                System.clearProperty(propertyName);
            } else {
                System.setProperty(propertyName, previousDataFile);
            }
        }
    }
}
