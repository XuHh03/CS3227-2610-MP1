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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #19324d;");
        Label subtitle = new Label("Your upbeat sidekick for fresh, tidy shelves.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #617387;");
        VBox heading = new VBox(3, title, subtitle);

        Label pantryLabel = new Label("YOUR PANTRY");
        pantryLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #617387;");
        VBox pantrySection = new VBox(8, pantryLabel, pantryList);
        VBox.setVgrow(pantryList, Priority.ALWAYS);

        TextField commandField = new TextField();
        commandField.setPromptText("Ask Byte: add rice 3 grains 2026-12-01");
        commandField.setPrefHeight(38);
        Button runButton = new Button("Run");
        runButton.setPrefHeight(38);
        runButton.setDefaultButton(true);
        HBox commandBar = new HBox(8, commandField, runButton);
        HBox.setHgrow(commandField, Priority.ALWAYS);

        BorderPane layout = new BorderPane();
        layout.setTop(heading);
        layout.setCenter(pantrySection);
        layout.setBottom(new VBox(8, conversationScroll, commandBar));
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f7fb;");
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
        pantryList.setStyle("-fx-background-color: white; -fx-control-inner-background: white;"
                + " -fx-border-color: #d9e2ec; -fx-border-radius: 10; -fx-background-radius: 10;");
        pantryList.setCellFactory(list -> new PantryItemCell());
        return pantryList;
    }

    private VBox createConversation() {
        VBox conversation = new VBox(6);
        conversation.setPadding(new Insets(10, 12, 10, 12));
        conversation.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #d9e2ec; -fx-border-radius: 10;");
        return conversation;
    }

    private ScrollPane createConversationScroll(VBox conversation) {
        ScrollPane conversationScroll = new ScrollPane(conversation);
        conversationScroll.setFitToWidth(true);
        conversationScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversationScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        conversationScroll.setPrefViewportHeight(170);
        conversationScroll.setMaxHeight(210);
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
                    if (!line.startsWith("Pantry items:") && !line.matches("\\d+\\..*")) {
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
        conversation.getChildren().clear();
        appendMessage(conversation, command, false, true);
        try {
            commandWriter.write((command + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            commandWriter.flush();
            commandField.clear();
        } catch (IOException exception) {
            appendMessage(conversation, "Unable to send command.", true, false);
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
                        || (error && "error-message".equals(previousMessage.getId()))) {
                    previousMessage.setText(previousMessage.getText() + System.lineSeparator() + text);
                    return;
                }
            }
            Label message = new Label(text);
            message.setWrapText(true);
            message.setMaxWidth(Double.MAX_VALUE);
            message.setPadding(new Insets(7, 10, 7, 10));
            if (userMessage) {
                message.setId("user-message");
                message.setAlignment(Pos.CENTER_RIGHT);
                message.setTextFill(Color.WHITE);
                message.setStyle("-fx-background-color: #2f6fed; -fx-background-radius: 12 12 3 12;");
                VBox.setMargin(message, new Insets(0, 0, 0, 80));
            } else if (error) {
                message.setId("error-message");
                message.setTextFill(Color.web("#9b2c2c"));
                message.setStyle("-fx-background-color: #fff0f0; -fx-background-radius: 8;"
                        + " -fx-border-color: #f2b8b8; -fx-border-radius: 8;");
                VBox.setMargin(message, new Insets(0, 25, 0, 0));
            } else {
                message.setId("app-message");
                message.setTextFill(Color.web("#34495e"));
                message.setStyle("-fx-background-color: #eef3f8; -fx-background-radius: 8;"
                        + " -fx-font-size: 14px;");
                VBox.setMargin(message, new Insets(0, 25, 0, 0));
            }
            conversation.getChildren().add(message);
        });
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
                setStyle("-fx-background-color: white;");
                return;
            }

            Label index = new Label((getIndex() + 1) + ".");
            index.setMinWidth(30);
            index.setStyle("-fx-font-weight: bold; -fx-text-fill: #718096;");

            Label name = new Label(item.getName());
            name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #19324d;");
            Label quantity = new Label("Qty: " + item.getQuantity());
            quantity.setStyle("-fx-font-size: 13px; -fx-text-fill: #617387;");
            HBox title = new HBox(10, name, quantity);
            title.setAlignment(Pos.CENTER_LEFT);

            Label category = createBadge(item.getCategory().name(), categoryColor(item.getCategory()));
            Label expiry = createExpiryBadge(item.getExpiryDate());
            HBox metadata = new HBox(8, category, expiry);
            metadata.setAlignment(Pos.CENTER_LEFT);

            VBox details = new VBox(5, title, metadata);
            HBox row = new HBox(8, index, details);
            row.setAlignment(Pos.CENTER_LEFT);
            setText(null);
            setGraphic(row);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setPrefHeight(68);
            setStyle("-fx-background-color: white; -fx-padding: 8px 12px;");
        }

        private Label createBadge(String text, String color) {
            Label badge = new Label(text);
            badge.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;"
                    + " -fx-padding: 3px 8px; -fx-font-size: 11px; -fx-font-weight: bold;");
            badge.setTextFill(Color.WHITE);
            return badge;
        }

        private Label createExpiryBadge(LocalDate expiryDate) {
            if (expiryDate == null) {
                return createBadge("No expiry date", "#718096");
            }
            LocalDate today = LocalDate.now();
            String status = expiryDate.isBefore(today) ? "Expired: "
                    : expiryDate.isEqual(today) ? "Expires today: " : "Expires: ";
            String color = expiryDate.isBefore(today) ? "#c53030"
                    : !expiryDate.isAfter(today.plusDays(7)) ? "#dd6b20" : "#2f855a";
            return createBadge(status + expiryDate.format(EXPIRY_FORMAT), color);
        }

        private String categoryColor(Category category) {
            return switch (category) {
            case DAIRY -> "#805ad5";
            case PRODUCE -> "#0f766e";
            case GRAINS -> "#b7791f";
            case MEAT -> "#c53030";
            case CANNED -> "#3182ce";
            case SNACKS -> "#dd6b20";
            case GENERAL, OTHER -> "#718096";
            };
        }
    }
}
