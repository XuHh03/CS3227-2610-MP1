package nutribyte.ui;

import java.util.ArrayList;
import java.util.List;
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

        List<String> parts = splitArguments(trimmedInput);
        Command command;
        try {
            command = Command.valueOf(parts.get(0).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            command = Command.UNKNOWN;
        }

        String[] arguments = parts.subList(1, parts.size()).toArray(String[]::new);
        return new ParsedCommand(command, arguments);
    }

    private List<String> splitArguments(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean insideQuotes = false;
        for (char character : input.toCharArray()) {
            if (character == '"') {
                insideQuotes = !insideQuotes;
            } else if (Character.isWhitespace(character) && !insideQuotes) {
                if (!currentPart.isEmpty()) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }
            } else {
                currentPart.append(character);
            }
        }
        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString());
        }
        return parts;
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
        public ParsedCommand {
            arguments = arguments == null ? new String[0] : arguments.clone();
        }

        @Override
        public String[] arguments() {
            return arguments.clone();
        }
    }
}
