package com.uor.eng;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.Map;

public class NetworkMonitoringControls {
  private final Map<String, MonitoringState> monitoringStates = new HashMap<>();
  private final long thresholdLatency;

  public NetworkMonitoringControls(long thresholdLatency) {
    this.thresholdLatency = thresholdLatency;
  }

  private static class MonitoringState {
    Thread monitorThread;
    boolean isActive;
    Button stopButton;
    Button resumeButton;

    MonitoringState(Thread thread, Button stop, Button resume) {
      this.monitorThread = thread;
      this.isActive = true;
      this.stopButton = stop;
      this.resumeButton = resume;
      updateButtonStates();
    }

    void updateButtonStates() {
      stopButton.setDisable(!isActive);
      resumeButton.setDisable(isActive);
    }
  }

  public void startMonitoring(String siteAddress,
                              Main.MetricLabels labels,
                              XYChart.Series<Number, Number> latencySeries,
                              XYChart.Series<Number, Number> throughputSeries,
                              XYChart.Series<Number, Number> qualitySeries,
                              Button stopButton,
                              Button resumeButton) {
    // First ensure any existing monitoring is stopped
    stopMonitoring(siteAddress);

    Thread monitorThread = new Thread(() -> {
      NetworkClient client = new NetworkClient(siteAddress, thresholdLatency);
      int sampleCount = 0;

      while (!Thread.currentThread().isInterrupted()) {
        try {
          NetworkClient.NetworkMetrics metrics = client.monitorNetwork();
          sampleCount++;
          final int currentSample = sampleCount;

          Platform.runLater(() -> updateMetrics(metrics, currentSample, labels,
              latencySeries, throughputSeries, qualitySeries));

          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Platform.runLater(() -> updateLabelsOnStop(labels));
          break;
        } catch (Exception e) {
          Platform.runLater(() -> labels.latencyLabel.setText("Error: " + e.getMessage()));
        }
      }
    });

    MonitoringState state = new MonitoringState(monitorThread, stopButton, resumeButton);
    monitoringStates.put(siteAddress, state);

    stopButton.setOnAction(e -> stopMonitoring(siteAddress));
    resumeButton.setOnAction(e -> resumeMonitoring(siteAddress, labels,
        latencySeries, throughputSeries, qualitySeries));

    monitorThread.start();
    state.updateButtonStates();
  }

  private void updateMetrics(NetworkClient.NetworkMetrics metrics, int sampleCount,
                             Main.MetricLabels labels,
                             XYChart.Series<Number, Number> latencySeries,
                             XYChart.Series<Number, Number> throughputSeries,
                             XYChart.Series<Number, Number> qualitySeries) {
    // Update labels
    labels.latencyLabel.setText(String.format("Latency: %.1f ms", metrics.getLatency()));
    labels.packetLossLabel.setText(String.format("Packet Loss: %.2f%%", metrics.getPacketLoss()));
    labels.throughputLabel.setText(String.format("Throughput: %.2f KB/s", metrics.getThroughput() / 1024));
    labels.jitterLabel.setText(String.format("Jitter: %.2f ms", metrics.getJitter()));
    labels.qualityLabel.setText(String.format("Quality: %.1f%%", metrics.getConnectionQuality()));
    labels.errorRateLabel.setText(String.format("Error Rate: %.2f%%", metrics.getErrorRate()));
    labels.downloadSpeedLabel.setText(String.format("Download Speed: %.2f Mbps", metrics.getDownloadSpeed()));
    labels.uploadSpeedLabel.setText(String.format("Upload Speed: %.2f Mbps", metrics.getUploadSpeed()));
    labels.connectionStatusLabel.setText(metrics.isSuccessful() ? "Connection Status: Connected" : "Connection Status: Failed");

    // Update charts
    if (latencySeries.getData().size() > 50) {
      latencySeries.getData().remove(0);
      throughputSeries.getData().remove(0);
      qualitySeries.getData().remove(0);
    }

    latencySeries.getData().add(new XYChart.Data<>(sampleCount, metrics.getLatency()));
    throughputSeries.getData().add(new XYChart.Data<>(sampleCount, metrics.getThroughput() / 1024));
    qualitySeries.getData().add(new XYChart.Data<>(sampleCount, metrics.getConnectionQuality()));
  }

  private void updateLabelsOnStop(Main.MetricLabels labels) {
    labels.latencyLabel.setText("Latency: Stopped");
    labels.packetLossLabel.setText("Packet Loss: Stopped");
    labels.throughputLabel.setText("Throughput: Stopped");
    labels.jitterLabel.setText("Jitter: Stopped");
    labels.qualityLabel.setText("Quality: Stopped");
    labels.errorRateLabel.setText("Error Rate: Stopped");
    labels.downloadSpeedLabel.setText("Download Speed: Stopped");
    labels.uploadSpeedLabel.setText("Upload Speed: Stopped");
    labels.connectionStatusLabel.setText("Connection Status: Stopped");
  }

  public void stopMonitoring(String siteAddress) {
    MonitoringState state = monitoringStates.get(siteAddress);
    if (state != null && state.isActive) {
      state.monitorThread.interrupt();
      try {
        state.monitorThread.join(1000); // Wait for thread to stop
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      state.isActive = false;
      state.updateButtonStates();
    }
  }

  private void resumeMonitoring(String siteAddress,
                                Main.MetricLabels labels,
                                XYChart.Series<Number, Number> latencySeries,
                                XYChart.Series<Number, Number> throughputSeries,
                                XYChart.Series<Number, Number> qualitySeries) {
    MonitoringState state = monitoringStates.get(siteAddress);
    if (state != null && !state.isActive) {
      startMonitoring(siteAddress, labels, latencySeries, throughputSeries, qualitySeries,
          state.stopButton, state.resumeButton);
    }
  }

  public void stopAllMonitoring() {
    for (String siteAddress : monitoringStates.keySet()) {
      stopMonitoring(siteAddress);
    }
    monitoringStates.clear();
  }
}
