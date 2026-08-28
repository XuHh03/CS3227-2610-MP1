package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    @Test
    void main_addCommand_acceptsOptionalCategoryAndExpiryIndependently() {
        String output = runApplication("add milk 2 dairy\n"
                + "add rice 3 expiry 2026-12-01\n"
                + "add beans 4 canned expiry 2027-01-01\nlist\nbye\n");

        assertTrue(output.contains("milk (2) [dairy]"));
        assertTrue(output.contains("rice (3) [general, expires 2026-12-01]"));
        assertTrue(output.contains("beans (4) [canned, expires 2027-01-01]"));
    }

    @Test
    void main_invalidInput_explainsExpectedCorrection() {
        String output = runApplication("add milk 2\nadd milk zero\nadd milk 2 dairy expires 2026-09-15\n"
                + "add milk 2 dairy 2026-02-30\n"
                + "add milk 2 unknown\nedit 1 name milk!\ndelete nope\nsearch\nbye\n");

        assertTrue(output.contains("positive whole number"));
        assertTrue(output.contains("Invalid add format"));
        assertTrue(output.contains("real calendar date"));
        assertTrue(output.contains("Invalid category 'unknown'"));
        assertTrue(output.contains("Value 'milk!' is invalid for name"));
        assertTrue(output.contains("Item index 'nope' is invalid"));
        assertTrue(output.contains("Search format: search <text>"));
    }

    @Test
    void main_malformedData_doesNotOverwriteExistingFile() throws Exception {
        Path dataFile = temporaryDirectory.resolve("pantry.txt");
        String malformedData = "Milk\tnot-a-number\tDAIRY\t2026-09-15\n";
        Files.writeString(dataFile, malformedData);

        String output = runApplication("bye\n");

        assertTrue(output.contains("The existing data was not changed"));
        assertEquals(malformedData, Files.readString(dataFile));
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
