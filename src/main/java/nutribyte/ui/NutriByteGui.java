package nutribyte.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import nutribyte.model.Category;
import nutribyte.model.PantryItem;
import nutribyte.storage.PantryStorage;

/**
 * JavaFX front end for NutriByte's command-based pantry application.
 */
public class NutriByteGui extends Application {
    private static final String DATA_FILE_PROPERTY = "nutribyte.dataFile";
    private static final String DEFAULT_DATA_FILE = "data/pantry.txt";
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private volatile boolean suppressListOutput;

    /**
     * Starts the pantry display and command input controls.
     *
     * @param stage primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        ListView<PantryItem> pantryList = createPantryList();
        VBox conversation = createConversation();
        ScrollPane conversationScroll = createConversationScroll(conversation);

        Label title = new Label("NutriByte Pantry");
        title.setText("NutriByte • Byte's Pantry");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Your upbeat sidekick for fresh, tidy shelves.");
        subtitle.getStyleClass().add("page-subtitle");
        VBox heading = new VBox(3, title, subtitle);
        heading.getStyleClass().add("page-heading");

        Label pantryLabel = new Label("YOUR PANTRY");
        pantryLabel.getStyleClass().add("section-label");
        StackPane pantrySurface = createPantrySurface(pantryList);
        VBox pantrySection = new VBox(8, pantryLabel, pantrySurface);
        VBox.setVgrow(pantrySurface, Priority.ALWAYS);

        TextField commandField = new TextField();
        commandField.setPromptText("Ask Byte: add rice 3 grains 2026-12-01");
        commandField.getStyleClass().add("command-field");
        Button runButton = new Button("Run");
        runButton.getStyleClass().add("run-button");
        runButton.setDefaultButton(true);
        HBox commandBar = new HBox(8, commandField, runButton);
        HBox.setHgrow(commandField, Priority.ALWAYS);

        BorderPane layout = new BorderPane();
        layout.setTop(heading);
        layout.setCenter(pantrySection);
        layout.setBottom(new VBox(8, conversationScroll, commandBar));
        layout.getStyleClass().add("app-root");
        layout.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
        BorderPane.setMargin(pantrySection, new Insets(18, 0, 14, 0));

        stage.setTitle("NutriByte");
        stage.setMinWidth(520);
        stage.setMinHeight(420);
        stage.setScene(new Scene(layout, 760, 560));
        stage.show();

        refreshPantryList(pantryList);
        Timeline refreshTimer = new Timeline(new KeyFrame(Duration.millis(500),
                event -> refreshPantryList(pantryList)));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
        connectCli(commandField, runButton, conversation);
    }

    private ListView<PantryItem> createPantryList() {
        ListView<PantryItem> pantryList = new ListView<>();
        pantryList.setPlaceholder(new Label("Your pantry is empty. Add an item below to get started."));
        pantryList.getStyleClass().add("pantry-list");
        pantryList.setCellFactory(list -> new PantryItemCell());
        return pantryList;
    }

    private StackPane createPantrySurface(ListView<PantryItem> pantryList) {
        ImageView shelfImage = new ImageView(new Image(getClass()
                .getResourceAsStream("/images/background.jpeg")));
        shelfImage.setPreserveRatio(true);
        shelfImage.setSmooth(true);
        shelfImage.getStyleClass().add("shelf-image");
        shelfImage.fitWidthProperty().bind(pantryList.widthProperty().multiply(0.8));
        shelfImage.fitHeightProperty().bind(pantryList.heightProperty().multiply(0.95));
        shelfImage.setMouseTransparent(true);

        StackPane pantrySurface = new StackPane(shelfImage, pantryList);
        pantrySurface.getStyleClass().add("pantry-surface");
        return pantrySurface;
    }

    private VBox createConversation() {
        VBox conversation = new VBox(6);
        conversation.getStyleClass().add("conversation");
        return conversation;
    }

    private ScrollPane createConversationScroll(VBox conversation) {
        ScrollPane conversationScroll = new ScrollPane(conversation);
        conversationScroll.setFitToWidth(true);
        conversationScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversationScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        conversationScroll.setPrefViewportHeight(170);
        conversationScroll.setMaxHeight(210);
        conversationScroll.getStyleClass().add("conversation-scroll");
        conversation.heightProperty().addListener((observable, oldHeight, newHeight) ->
                conversationScroll.setVvalue(1.0));
        return conversationScroll;
    }

    private void refreshPantryList(ListView<PantryItem> pantryList) {
        Path dataPath = Path.of(System.getProperty(DATA_FILE_PROPERTY, DEFAULT_DATA_FILE));
        try {
            List<PantryItem> items = new PantryStorage(dataPath).load();
            pantryList.getItems().setAll(items);
        } catch (IOException | RuntimeException exception) {
            pantryList.getItems().clear();
        }
    }

    private void connectCli(TextField commandField, Button runButton, VBox conversation) {
        PrintStream originalOutput = System.out;
        InputStream originalInput = System.in;
        PipedOutputStream commandWriter = new PipedOutputStream();
        try {
            PipedInputStream commandReader = new PipedInputStream(commandWriter);
            System.setOut(createGuiPrintStream(conversation));
            System.setIn(commandReader);
            Thread commandThread = new Thread(() -> {
                NutriByte.main(new String[0]);
                System.setOut(originalOutput);
                System.setIn(originalInput);
            }, "nutribyte-cli");
            commandThread.setDaemon(true);
            commandThread.start();
        } catch (IOException exception) {
            appendMessage(conversation, "Unable to start command input.", true, false);
        }

        Runnable submitCommand = () -> sendCommand(commandField, commandWriter, conversation);
        runButton.setOnAction(event -> submitCommand.run());
        commandField.setOnAction(event -> submitCommand.run());
    }

    private PrintStream createGuiPrintStream(VBox conversation) {
        OutputStream outputStream = new OutputStream() {
            private final StringBuilder lineBuffer = new StringBuilder();

            @Override
            public void write(int value) {
                writeText(new String(new byte[] {(byte) value}, StandardCharsets.UTF_8));
            }

            @Override
            public void write(byte[] bytes, int offset, int length) {
                writeText(new String(bytes, offset, length, StandardCharsets.UTF_8));
            }

            private void writeText(String text) {
                lineBuffer.append(text);
                int newline;
                while ((newline = lineBuffer.indexOf("\n")) >= 0) {
                    String line = lineBuffer.substring(0, newline).stripTrailing();
                    lineBuffer.delete(0, newline + 1);
                    if (shouldDisplayOutput(line)) {
                        appendMessage(conversation, line, isError(line), false);
                    }
                }
            }
        };
        return new PrintStream(outputStream, true, StandardCharsets.UTF_8);
    }

    private void sendCommand(TextField commandField, PipedOutputStream commandWriter, VBox conversation) {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        boolean validCommandInput = isValidCommandInput(command);
        String commandName = command.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        suppressListOutput = "list".equals(commandName);
        conversation.getChildren().clear();
        appendMessage(conversation, command, false, true);
        try {
            commandWriter.write((command + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            commandWriter.flush();
            if (validCommandInput) {
                commandField.clear();
            }
        } catch (IOException exception) {
            appendMessage(conversation, "Unable to send command.", true, false);
        }
    }

    private boolean shouldDisplayOutput(String line) {
        if (!suppressListOutput) {
            return true;
        }
        return !line.startsWith("Pantry items:")
                && !line.startsWith("Here's what's on your shelves:")
                && !line.matches("\\d+\\..*");
    }

    private boolean isValidCommandInput(String input) {
        Parser.ParsedCommand parsedCommand = new Parser().parse(input);
        String[] arguments = parsedCommand.arguments();
        try {
            return switch (parsedCommand.command()) {
            case ADD -> isValidAddInput(arguments);
            case CONSUME, RESTOCK -> arguments.length == 2 && isPositiveInteger(arguments[1]);
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
        if (arguments.length != 2 && arguments.length != 4) {
            return false;
        }
        if (!isPositiveInteger(arguments[1])) {
            return false;
        }
        return arguments.length == 2
                || isValidCategory(arguments[2]) && isValidDate(arguments[3]);
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
                && isValidDate(arguments[2]);
    }

    private boolean isValidEditValue(String field, String value) {
        return switch (field.toLowerCase(Locale.ROOT)) {
        case "name" -> !value.isBlank();
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

    private boolean isError(String text) {
        return text.startsWith("Unknown command")
                || text.startsWith("Usage:")
                || text.startsWith("Quantity must")
                || text.startsWith("Item not found")
                || text.startsWith("Not enough stock")
                || text.startsWith("Expiry date must")
                || text.startsWith("Unknown category")
                || text.startsWith("Item number")
                || text.startsWith("Editable fields")
                || text.startsWith("Invalid value")
                || text.startsWith("Could not");
    }

    private void appendMessage(VBox conversation, String text, boolean error, boolean userMessage) {
        Platform.runLater(() -> {
            if (text.isEmpty()) {
                return;
            }
            if (!userMessage && !conversation.getChildren().isEmpty()) {
                Label previousMessage = (Label) conversation.getChildren()
                        .get(conversation.getChildren().size() - 1);
                if ("app-message".equals(previousMessage.getId())
                        || "error-message".equals(previousMessage.getId())) {
                    previousMessage.setText(previousMessage.getText() + System.lineSeparator() + text);
                    return;
                }
            }
            Label message = new Label(text);
            message.setWrapText(true);
            message.setMaxWidth(Double.MAX_VALUE);
            if (userMessage) {
                message.setId("user-message");
                message.setAlignment(Pos.CENTER_RIGHT);
                message.setGraphic(createAvatar("/images/user.png"));
                message.setContentDisplay(ContentDisplay.RIGHT);
                message.getStyleClass().add("user-message");
                VBox.setMargin(message, new Insets(0, 0, 0, 80));
            } else if (error) {
                message.setId("error-message");
                message.setGraphic(createAvatar("/images/bot.jpeg"));
                message.setContentDisplay(ContentDisplay.LEFT);
                message.getStyleClass().add("error-message");
                VBox.setMargin(message, new Insets(0, 25, 0, 0));
            } else {
                message.setId("app-message");
                message.setGraphic(createAvatar("/images/bot.jpeg"));
                message.setContentDisplay(ContentDisplay.LEFT);
                message.getStyleClass().add("app-message");
                VBox.setMargin(message, new Insets(0, 25, 0, 0));
            }
            conversation.getChildren().add(message);
        });
    }

    private ImageView createAvatar(String resourcePath) {
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(resourcePath)));
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);
        avatar.setPreserveRatio(false);
        avatar.setClip(new javafx.scene.shape.Circle(15, 15, 15));
        return avatar;
    }

    /**
     * Renders one pantry item with a visible index and separated metadata.
     */
    private static class PantryItemCell extends ListCell<PantryItem> {
        @Override
        protected void updateItem(PantryItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().add("item-cell");
                return;
            }

            Label index = new Label((getIndex() + 1) + ".");
            index.setMinWidth(30);
            index.getStyleClass().add("item-index");

            Label name = new Label(item.getName());
            name.getStyleClass().add("item-name");
            Label quantity = new Label("Qty: " + item.getQuantity());
            quantity.getStyleClass().add("item-quantity");
            HBox title = new HBox(10, name, quantity);
            title.setAlignment(Pos.CENTER_LEFT);

            Label category = createBadge(item.getCategory().name(), categoryStyleClass(item.getCategory()));
            Label expiry = createExpiryBadge(item.getExpiryDate());
            HBox metadata = new HBox(8, category, expiry);
            metadata.setAlignment(Pos.CENTER_LEFT);

            VBox details = new VBox(5, title, metadata);
            HBox row = new HBox(8, index, details);
            row.setAlignment(Pos.CENTER_LEFT);
            setText(null);
            setGraphic(row);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            getStyleClass().add("item-cell");
        }

        private Label createBadge(String text, String styleClass) {
            Label badge = new Label(text);
            badge.getStyleClass().addAll("badge", styleClass);
            return badge;
        }

        private Label createExpiryBadge(LocalDate expiryDate) {
            if (expiryDate == null) {
                return createBadge("No expiry date", "expiry-none");
            }
            LocalDate today = LocalDate.now();
            String status = expiryDate.isBefore(today) ? "Expired: "
                    : expiryDate.isEqual(today) ? "Expires today: " : "Expires: ";
            String styleClass = expiryDate.isBefore(today) ? "expiry-expired"
                    : !expiryDate.isAfter(today.plusDays(7)) ? "expiry-soon" : "expiry-safe";
            return createBadge(status + expiryDate.format(EXPIRY_FORMAT), styleClass);
        }

        private String categoryStyleClass(Category category) {
            return switch (category) {
            case DAIRY -> "category-dairy";
            case PRODUCE -> "category-produce";
            case GRAINS -> "category-grains";
            case MEAT -> "category-meat";
            case CANNED -> "category-canned";
            case SNACKS -> "category-snacks";
            case GENERAL, OTHER -> "category-neutral";
            };
        }
    }
}
