package com.uor.eng;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertService {
  private static final long DEFAULT_THRESHOLD_LATENCY = 1000;

  public static void showLatencyAlert(String siteAddress, double latency) {
    if (latency > DEFAULT_THRESHOLD_LATENCY) {
      Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("High Latency Alert");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Latency for %s exceeded threshold: %.1f ms",
            siteAddress, latency));
        alert.show();
      });
    }
  }

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