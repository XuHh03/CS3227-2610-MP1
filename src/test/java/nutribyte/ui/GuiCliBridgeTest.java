package nutribyte.ui;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests lifecycle and command forwarding for the GUI-to-CLI bridge.
 */
class GuiCliBridgeTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void send_beforeStart_rejectsCommand() {
        GuiCliBridge bridge = new GuiCliBridge(line -> { });

        assertThrows(java.io.IOException.class, () -> bridge.send("help"));
    }

    @Test
    void startAndSend_byeRestoresProcessStreams() throws Exception {
        PrintStream originalOutput = System.out;
        InputStream originalInput = System.in;
        String propertyName = "nutribyte.dataFile";
        String previousDataFile = System.getProperty(propertyName);
        CopyOnWriteArrayList<String> lines = new CopyOnWriteArrayList<>();
        CountDownLatch finished = new CountDownLatch(1);
        GuiCliBridge bridge = new GuiCliBridge(line -> {
            lines.add(line);
            if (line.contains("Catch you later")) {
                finished.countDown();
            }
        });
        try {
            System.setProperty(propertyName, temporaryDirectory.resolve("pantry.txt").toString());
            bridge.start();
            bridge.send("add rice 3");
            bridge.send("bye");

            assertTrue(finished.await(2, TimeUnit.SECONDS));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Nice! Added rice (3)")));
            assertTrue(waitForOriginalStreams(originalOutput, originalInput));
        } finally {
            bridge.close();
            if (previousDataFile == null) {
                System.clearProperty(propertyName);
            } else {
                System.setProperty(propertyName, previousDataFile);
            }
        }
    }

    private boolean waitForOriginalStreams(PrintStream expectedOutput, InputStream expectedInput)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (System.out == expectedOutput && System.in == expectedInput) {
                return true;
            }
            Thread.sleep(10);
        }
        assertSame(expectedOutput, System.out);
        assertSame(expectedInput, System.in);
        return false;
    }
}
