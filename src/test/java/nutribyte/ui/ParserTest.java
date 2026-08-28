package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests conversion of user input into structured commands.
 */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parse_supportedCommandWithArguments_returnsCommandAndArguments() {
        Parser.ParsedCommand result = parser.parse("add rice 3 grains 2026-12-01");

        assertEquals(Parser.Command.ADD, result.command());
        assertArrayEquals(new String[] {"rice", "3", "grains", "2026-12-01"}, result.arguments());
    }

    @Test
    void parse_mixedCaseAndExtraWhitespace_normalizesCommandAndArguments() {
        Parser.ParsedCommand result = parser.parse("  FiLtEr   category   grains  ");

        assertEquals(Parser.Command.FILTER, result.command());
        assertArrayEquals(new String[] {"category", "grains"}, result.arguments());
    }

    @Test
    void parse_commandWithoutArguments_returnsEmptyArguments() {
        Parser.ParsedCommand result = parser.parse("help");

        assertEquals(Parser.Command.HELP, result.command());
        assertArrayEquals(new String[0], result.arguments());
    }

    @Test
    void parse_blankInput_returnsUnknownCommand() {
        Parser.ParsedCommand result = parser.parse("   \t  ");

        assertEquals(Parser.Command.UNKNOWN, result.command());
        assertArrayEquals(new String[0], result.arguments());
    }

    @Test
    void parse_unrecognizedCommand_returnsUnknownCommandAndPreservesArguments() {
        Parser.ParsedCommand result = parser.parse("remove rice");

        assertEquals(Parser.Command.UNKNOWN, result.command());
        assertArrayEquals(new String[] {"rice"}, result.arguments());
    }

    @Test
    void parse_nullInput_returnsUnknownCommand() {
        Parser.ParsedCommand result = parser.parse(null);

        assertEquals(Parser.Command.UNKNOWN, result.command());
        assertArrayEquals(new String[0], result.arguments());
    }

    @Test
    void parse_editCommand_returnsEditCommand() {
        Parser.ParsedCommand result = parser.parse("edit 1 quantity 5");

        assertEquals(Parser.Command.EDIT, result.command());
        assertArrayEquals(new String[] {"1", "quantity", "5"}, result.arguments());
    }

    @Test
    void parse_quotedMultiWordName_keepsNameAsOneArgument() {
        Parser.ParsedCommand result = parser.parse("add \"Fresh Milk\" 2 dairy");

        assertEquals(Parser.Command.ADD, result.command());
        assertArrayEquals(new String[] {"Fresh Milk", "2", "dairy"}, result.arguments());
    }

    @Test
    void parsedCommand_argumentsAreDefensivelyCopied() {
        Parser.ParsedCommand result = parser.parse("add rice 3");
        String[] arguments = result.arguments();

        arguments[0] = "changed";

        assertArrayEquals(new String[] {"rice", "3"}, result.arguments());
    }
}
