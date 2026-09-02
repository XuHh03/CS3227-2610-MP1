package nutribyte.ui;

import javafx.application.Application;

/**
 * Launches the NutriByte JavaFX application from an executable JAR.
 */
public final class NutriByteLauncher {
    private NutriByteLauncher() {
        // Utility class; do not instantiate.
    }

    /**
     * Starts the JavaFX user interface.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        Application.launch(NutriByteGui.class, args);
    }
}
