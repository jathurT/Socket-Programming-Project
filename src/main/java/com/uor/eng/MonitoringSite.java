package com.uor.eng;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

public class MonitoringSite {
  private final String address;
  private final MetricsDisplay metricsDisplay;
  private final ChartDisplay chartDisplay;
  @Getter
  private final VBox view;
  @Setter
  @Getter
  private Runnable monitoringTask;
  private final AtomicBoolean isRunning;
  private final Runnable onStopCallback;

  public MonitoringSite(String address, Runnable onStopCallback) {
    this.address = address;
    this.onStopCallback = onStopCallback;
    this.metricsDisplay = new MetricsDisplay();
    this.chartDisplay = new ChartDisplay();
    this.view = createView();
    this.isRunning = new AtomicBoolean(true);
  }

  private VBox createView() {
    HBox controls = createControls();
    return new VBox(10, controls, metricsDisplay.getGrid(), chartDisplay.getChart());
  }

  private HBox createControls() {
    Label siteLabel = new Label("Site: " + address);
    siteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    Button stopButton = new Button("Stop");
    stopButton.setOnAction(e -> {
      stop();
      if (onStopCallback != null) {
        onStopCallback.run();
      }
    });

    return new HBox(10, siteLabel, stopButton);
  }

  public void updateMetrics(NetworkMetrics metrics) {
    Platform.runLater(() -> {
      metricsDisplay.update(metrics);
      chartDisplay.update(metrics);
    });
  }

  public void stop() {
    isRunning.set(false);
  }

  public boolean isRunning() {
    return isRunning.get();
  }

}