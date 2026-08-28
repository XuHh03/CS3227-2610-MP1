package nutribyte.ui;

import java.time.LocalDate;
import java.util.Locale;

import nutribyte.model.Category;

/**
 * Validates the argument format of commands before they are sent to the CLI.
 */
final class CommandValidator {
    /**
     * Checks whether a complete command has valid syntax and values.
     *
     * @param input command entered by the user
     * @return true if the command can be submitted
     */
    boolean isValidCommandInput(String input) {
        Parser.ParsedCommand parsedCommand = new Parser().parse(input);
        String[] arguments = parsedCommand.arguments();
        try {
            return switch (parsedCommand.command()) {
            case ADD -> isValidAddInput(arguments);
            case CONSUME, RESTOCK -> isValidQuantityCommand(arguments);
            case DELETE -> arguments.length == 1 && isPositiveInteger(arguments[0]);
            case SEARCH -> arguments.length > 0;
            case FILTER -> isValidFilterInput(arguments);
            case EDIT -> arguments.length == 3 && isPositiveInteger(arguments[0])
                    && isValidEditValue(arguments[1], arguments[2]);
            case LIST, HELP, BYE, EXIT -> arguments.length == 0;
            case UNKNOWN -> false;
            };
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isValidAddInput(String[] arguments) {
        if (arguments.length < 2 || arguments.length > 5) {
            return false;
        }
        if (!isValidName(arguments[0]) || !isPositiveInteger(arguments[1])) {
            return false;
        }
        if (arguments.length == 2) {
            return true;
        }
        if (arguments.length == 3) {
            return isValidCategory(arguments[2]);
        }
        if (arguments.length == 4) {
            return (isValidCategory(arguments[2]) || "expiry".equalsIgnoreCase(arguments[2]))
                    && isValidDate(arguments[3]);
        }
        return isValidCategory(arguments[2])
                && "expiry".equalsIgnoreCase(arguments[3])
                && isValidDate(arguments[4]);
    }

    private boolean isValidQuantityCommand(String[] arguments) {
        if (arguments.length == 2) {
            return isPositiveInteger(arguments[1]);
        }
        return arguments.length == 3
                && "index".equalsIgnoreCase(arguments[0])
                && isPositiveInteger(arguments[1])
                && isPositiveInteger(arguments[2]);
    }

    private boolean isValidName(String value) {
        return value.matches("[\\p{L}\\p{N}](?:[\\p{L}\\p{N} -]*[\\p{L}\\p{N}])?");
    }

    private boolean isValidFilterInput(String[] arguments) {
        if (arguments.length < 2) {
            return false;
        }
        if ("category".equalsIgnoreCase(arguments[0])) {
            return arguments.length == 2 && isValidCategory(arguments[1]);
        }
        if ("expiry-before".equalsIgnoreCase(arguments[0])) {
            return arguments.length == 2 && isValidDate(arguments[1]);
        }
        return "expiry-between".equalsIgnoreCase(arguments[0])
                && arguments.length == 3
                && isValidDate(arguments[1])
                && isValidDate(arguments[2])
                && !LocalDate.parse(arguments[1]).isAfter(LocalDate.parse(arguments[2]));
    }

    private boolean isValidEditValue(String field, String value) {
        return switch (field.toLowerCase(Locale.ROOT)) {
        case "name" -> isValidName(value);
        case "quantity" -> isPositiveInteger(value);
        case "category" -> isValidCategory(value);
        case "expiry" -> "none".equalsIgnoreCase(value) || isValidDate(value);
        default -> false;
        };
    }

    private boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean isValidCategory(String value) {
        try {
            Category.valueOf(value.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isValidDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (java.time.format.DateTimeParseException exception) {
            return false;
        }
    }
}
