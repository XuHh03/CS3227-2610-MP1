package nutribyte.ui;

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
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return new ParsedCommand(Command.UNKNOWN, new String[0]);
        }

        String[] parts = trimmedInput.split("\\s+");
        Command command;
        try {
            command = Command.valueOf(parts[0].toUpperCase());
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
        HELP,
        BYE,
        EXIT,
        UNKNOWN
    }

    /** Structured result returned by the parser. */
    public record ParsedCommand(Command command, String[] arguments) {
    }
}
