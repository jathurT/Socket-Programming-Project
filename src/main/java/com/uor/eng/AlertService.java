package com.uor.eng;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Displays alerts for high latency or errors in the application.
 */
public class AlertService {
  private static final long DEFAULT_THRESHOLD_LATENCY = 2000;

  /**
   * Shows a warning alert if latency is above a default threshold.
   */
  public static void showLatencyAlert(String siteAddress, double latency) {
    if (latency > DEFAULT_THRESHOLD_LATENCY) {
      Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("High Latency Alert");
        alert.setHeaderText(null);
        alert.setContentText(String.format(
            "Latency for %s exceeded threshold: %.1f ms", siteAddress, latency));
        alert.show();
      });
    }
  }

  /**
   * Displays a generic error alert with a title and message.
   */
  public static void showError(String title, String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.show();
    });
  }
}
