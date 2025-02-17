package com.uor.eng;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class MonitoringSite {
  @Getter
  private final String siteAddress;

  @Getter @Setter
  private Runnable monitoringTask;

  private boolean running = true;
  private final VBox rootLayout;
  private final Button stopButton;
  private final MetricsDisplay metricsDisplay;
  private final ChartDisplay chartDisplay;

  // Add callbacks for metrics updates and site removal
  private Consumer<NetworkMetrics> onMetricsUpdated;
  private Runnable onSiteRemoved;

  public MonitoringSite(String siteAddress) {
    this.siteAddress = siteAddress;
    this.stopButton = new Button("Stop");
    stopButton.setOnAction(e -> stop());
    this.metricsDisplay = new MetricsDisplay();
    this.chartDisplay = new ChartDisplay();
    chartDisplay.getChart().setTitle("Metrics for " + siteAddress);

    this.rootLayout = new VBox(10, stopButton, metricsDisplay.getGrid(), chartDisplay.getChart());
  }

  public void setOnMetricsUpdated(Consumer<NetworkMetrics> callback) {
    this.onMetricsUpdated = callback;
  }

  public void setOnSiteRemoved(Runnable callback) {
    this.onSiteRemoved = callback;
  }

  public void updateMetrics(NetworkMetrics metrics) {
    Platform.runLater(() -> {
      if (!running) return;

      metricsDisplay.update(metrics);
      chartDisplay.update(metrics);
      if (onMetricsUpdated != null) {
        onMetricsUpdated.accept(metrics);
      }
    });
  }

  public boolean isRunning() {
    return running;
  }

  public void stop() {
    running = false;
    stopButton.setDisable(true);

    // Notify that this site should be removed
    if (onSiteRemoved != null) {
      Platform.runLater(() -> onSiteRemoved.run());
    }
  }

  public Node getView() {
    return rootLayout;
  }
}