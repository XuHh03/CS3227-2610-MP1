package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests command syntax validation used by the JavaFX interface.
 */
class CommandValidatorTest {
    private final CommandValidator validator = new CommandValidator();

    @Test
    void isValidCommandInput_supportedCommands_returnsTrue() {
        assertTrue(validator.isValidCommandInput("add \"Fresh Milk\" 2 dairy"));
        assertTrue(validator.isValidCommandInput("consume index 1 2"));
        assertTrue(validator.isValidCommandInput("filter expiry-between 2026-01-01 2026-12-31"));
    }

    @Test
    void isValidCommandInput_malformedCommands_returnsFalse() {
        assertFalse(validator.isValidCommandInput("add milk zero"));
        assertFalse(validator.isValidCommandInput("consume index 0 2"));
        assertFalse(validator.isValidCommandInput("filter expiry-between 2026-12-31 2026-01-01"));
    }
}
