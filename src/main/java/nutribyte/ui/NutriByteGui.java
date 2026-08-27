package nutribyte.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import nutribyte.model.PantryItem;
import nutribyte.storage.PantryStorage;

/**
 * JavaFX front end for NutriByte's command-based pantry application.
 */
public class NutriByteGui extends Application {
    private static final String DATA_FILE_PROPERTY = "nutribyte.dataFile";
    private static final String DEFAULT_DATA_FILE = "data/pantry.txt";

    /**
     * Starts the pantry display and command input controls.
     *
     * @param stage primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        ListView<String> pantryList = new ListView<>();
        TextArea statusArea = createStatusArea();
        TextField commandField = new TextField();
        commandField.setPromptText("Enter a command, e.g. add rice 3");
        Button runButton = new Button("Run");
        HBox commandBar = new HBox(8, commandField, runButton);
        HBox.setHgrow(commandField, Priority.ALWAYS);

        BorderPane layout = new BorderPane();
        layout.setCenter(pantryList);
        layout.setBottom(new VBox(8, statusArea, commandBar));
        layout.setPadding(new Insets(15));
        stage.setTitle("NutriByte");
        stage.setScene(new Scene(layout, 700, 500));
        stage.show();

        refreshPantryList(pantryList);
        Timeline refreshTimer = new Timeline(new KeyFrame(Duration.millis(500),
                event -> refreshPantryList(pantryList)));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
        connectCli(commandField, runButton, statusArea);
    }

    private TextArea createStatusArea() {
        TextArea statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setWrapText(true);
        statusArea.setPrefRowCount(3);
        statusArea.setMaxHeight(90);
        statusArea.setPromptText("Status and error messages appear here");
        return statusArea;
    }

    private void refreshPantryList(ListView<String> pantryList) {
        Path dataPath = Path.of(System.getProperty(DATA_FILE_PROPERTY, DEFAULT_DATA_FILE));
        try {
            List<PantryItem> items = new PantryStorage(dataPath).load();
            pantryList.getItems().setAll(items.stream().map(PantryItem::toString).toList());
        } catch (IOException | RuntimeException exception) {
            pantryList.getItems().clear();
        }
    }

    private void connectCli(TextField commandField, Button runButton, TextArea statusArea) {
        PrintStream originalOutput = System.out;
        InputStream originalInput = System.in;
        PipedOutputStream commandWriter = new PipedOutputStream();
        try {
            PipedInputStream commandReader = new PipedInputStream(commandWriter);
            System.setOut(createGuiPrintStream(statusArea));
            System.setIn(commandReader);
            Thread commandThread = new Thread(() -> {
                NutriByte.main(new String[0]);
                System.setOut(originalOutput);
                System.setIn(originalInput);
            }, "nutribyte-cli");
            commandThread.setDaemon(true);
            commandThread.start();
        } catch (IOException exception) {
            statusArea.setText("Unable to start command input.");
        }

        Runnable submitCommand = () -> sendCommand(commandField, commandWriter, statusArea);
        runButton.setOnAction(event -> submitCommand.run());
        commandField.setOnAction(event -> submitCommand.run());
    }

    private PrintStream createGuiPrintStream(TextArea statusArea) {
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
                    String line = lineBuffer.substring(0, newline).trim();
                    lineBuffer.delete(0, newline + 1);
                    if (!line.startsWith("Pantry items:") && !line.matches("\\d+\\..*")) {
                        appendStatus(statusArea, line);
                    }
                }
            }
        };
        return new PrintStream(outputStream, true, StandardCharsets.UTF_8);
    }

    private void sendCommand(TextField commandField, PipedOutputStream commandWriter, TextArea statusArea) {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        try {
            commandWriter.write((command + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            commandWriter.flush();
            commandField.clear();
        } catch (IOException exception) {
            statusArea.setText("Unable to send command.");
        }
    }

    private void appendStatus(TextArea statusArea, String text) {
        Platform.runLater(() -> {
            if (!text.isEmpty()) {
                statusArea.appendText(text + System.lineSeparator());
                statusArea.positionCaret(statusArea.getLength());
            }
        });
    }
}
