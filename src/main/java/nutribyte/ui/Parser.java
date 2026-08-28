package nutribyte.ui;

import java.util.Locale;

/**
 * Converts raw command-line input into a command and its arguments.
 */
public class Parser {
    /**
     * Parses one line of user input.
     *
     * @param input raw user input
     * @return parsed command
     */
    public ParsedCommand parse(String input) {
        if (input == null) {
            return new ParsedCommand(Command.UNKNOWN, new String[0]);
        }
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return new ParsedCommand(Command.UNKNOWN, new String[0]);
        }

        String[] parts = trimmedInput.split("\\s+");
        Command command;
        try {
            command = Command.valueOf(parts[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            command = Command.UNKNOWN;
        }

        String[] arguments = new String[parts.length - 1];
        System.arraycopy(parts, 1, arguments, 0, arguments.length);
        return new ParsedCommand(command, arguments);
    }

    /** Supported NutriByte commands. */
    public enum Command {
        ADD,
        LIST,
        CONSUME,
        RESTOCK,
        DELETE,
        SEARCH,
        FILTER,
        EDIT,
        HELP,
        BYE,
        EXIT,
        UNKNOWN
    }

    /**
     * Structured result returned by the parser.
     *
     * @param command parsed command
     * @param arguments command arguments
     */
    public record ParsedCommand(Command command, String[] arguments) {
    }
}
