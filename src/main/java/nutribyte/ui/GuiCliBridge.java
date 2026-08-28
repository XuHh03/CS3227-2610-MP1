package nutribyte.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Connects the command-line application to the GUI and forwards complete output lines.
 */
final class GuiCliBridge implements AutoCloseable {
    private final Consumer<String> lineConsumer;
    private PipedOutputStream commandWriter;
    private InputStream originalInput;
    private PrintStream originalOutput;

    GuiCliBridge(Consumer<String> lineConsumer) {
        this.lineConsumer = lineConsumer;
    }

    void start() throws IOException {
        if (commandWriter != null) {
            throw new IllegalStateException("The GUI command bridge has already started.");
        }
        PipedOutputStream writer = new PipedOutputStream();
        try {
            PipedInputStream reader = new PipedInputStream(writer);
            originalOutput = System.out;
            originalInput = System.in;
            commandWriter = writer;
            System.setOut(createGuiPrintStream());
            System.setIn(reader);
            Thread commandThread = new Thread(() -> {
                try {
                    NutriByte.main(new String[0]);
                } finally {
                    restoreStreams();
                }
            }, "nutribyte-cli");
            commandThread.setDaemon(true);
            commandThread.start();
        } catch (IOException | RuntimeException exception) {
            writer.close();
            restoreStreams();
            throw exception;
        }
    }

    void send(String command) throws IOException {
        if (commandWriter == null) {
            throw new IOException("The GUI command bridge has not started.");
        }
        commandWriter.write((command + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        commandWriter.flush();
    }

    @Override
    public void close() {
        if (commandWriter != null) {
            try {
                commandWriter.close();
            } catch (IOException exception) {
                restoreStreams();
            }
        }
    }

    private void restoreStreams() {
        if (originalOutput != null) {
            System.setOut(originalOutput);
        }
        if (originalInput != null) {
            System.setIn(originalInput);
        }
    }

    private PrintStream createGuiPrintStream() {
        OutputStream outputStream = new OutputStream() {
            private final StringBuilder lineBuffer = new StringBuilder();
            private final ByteArrayOutputStream encodedBuffer = new ByteArrayOutputStream();

            @Override
            public void write(int value) {
                write(new byte[] {(byte) value}, 0, 1);
            }

            @Override
            public void write(byte[] bytes, int offset, int length) {
                encodedBuffer.write(bytes, offset, length);
                byte[] pendingBytes = encodedBuffer.toByteArray();
                ByteBuffer input = ByteBuffer.wrap(pendingBytes);
                CharBuffer output = CharBuffer.allocate(pendingBytes.length);
                CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);
                CoderResult result = decoder.decode(input, output, false);
                if (result.isError()) {
                    writeText(new String(pendingBytes, StandardCharsets.UTF_8));
                    encodedBuffer.reset();
                    return;
                }
                encodedBuffer.reset();
                encodedBuffer.write(pendingBytes, input.position(), input.remaining());
                output.flip();
                writeText(output.toString());
            }

            private void writeText(String text) {
                lineBuffer.append(text);
                int newline;
                while ((newline = lineBuffer.indexOf("\n")) >= 0) {
                    String line = lineBuffer.substring(0, newline).stripTrailing();
                    lineBuffer.delete(0, newline + 1);
                    lineConsumer.accept(line);
                }
            }
        };
        return new PrintStream(outputStream, true, StandardCharsets.UTF_8);
    }
}
