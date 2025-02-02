package com.uor.eng;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import lombok.Getter;

public class MetricsDisplay {
  @Getter
  private final GridPane grid;
  private final Label latencyLabel;
  private final Label packetLossLabel;
  private final Label throughputLabel;
  private final Label jitterLabel;
  private final Label qualityLabel;
  private final Label errorRateLabel;
  private final Label downloadSpeedLabel;
  private final Label uploadSpeedLabel;
  private final Label connectionStatusLabel;

  public MetricsDisplay() {
    this.grid = new GridPane();
    this.latencyLabel = createMetricLabel();
    this.packetLossLabel = createMetricLabel();
    this.throughputLabel = createMetricLabel();
    this.jitterLabel = createMetricLabel();
    this.qualityLabel = createMetricLabel();
    this.errorRateLabel = createMetricLabel();
    this.downloadSpeedLabel = createMetricLabel();
    this.uploadSpeedLabel = createMetricLabel();
    this.connectionStatusLabel = createMetricLabel();

    setupGrid();
  }

  private Label createMetricLabel() {
    Label label = new Label("Pending...");
    label.setStyle("-fx-font-size: 12px;");
    return label;
  }

  private void setupGrid() {
    grid.setVgap(5);
    grid.setHgap(10);

    grid.add(latencyLabel, 0, 0);
    grid.add(packetLossLabel, 1, 0);
    grid.add(throughputLabel, 0, 1);
    grid.add(jitterLabel, 1, 1);
    grid.add(qualityLabel, 0, 2);
    grid.add(errorRateLabel, 1, 2);
    grid.add(downloadSpeedLabel, 0, 3);
    grid.add(uploadSpeedLabel, 1, 3);
    grid.add(connectionStatusLabel, 0, 4, 2, 1);
  }

  public void update(NetworkMetrics metrics) {
    if (!metrics.isSuccessful()) {
      updateError(metrics.getErrorMessage());
      return;
    }
    updateMetrics(metrics);
  }

  private void updateMetrics(NetworkMetrics metrics) {
    latencyLabel.setText(String.format("Latency: %.1f ms", metrics.getLatency()));
    packetLossLabel.setText(String.format("Packet Loss: %.2f%%", metrics.getPacketLoss()));
    throughputLabel.setText(String.format("Throughput: %.2f KB/s", metrics.getThroughput() / 1024));
    jitterLabel.setText(String.format("Jitter: %.2f ms", metrics.getJitter()));
    qualityLabel.setText(String.format("Quality: %.1f%%", metrics.getConnectionQuality()));
    errorRateLabel.setText(String.format("Error Rate: %.2f%%", metrics.getErrorRate()));
    downloadSpeedLabel.setText(String.format("Download Speed: %.2f Mbps", metrics.getDownloadSpeed()));
    uploadSpeedLabel.setText(String.format("Upload Speed: %.2f Mbps", metrics.getUploadSpeed()));
    connectionStatusLabel.setText("Connection Status: Connected");
  }

  private void updateError(String error) {
    latencyLabel.setText("Error: " + error);
    packetLossLabel.setText("N/A");
    throughputLabel.setText("N/A");
    jitterLabel.setText("N/A");
    qualityLabel.setText("N/A");
    errorRateLabel.setText("N/A");
    downloadSpeedLabel.setText("N/A");
    uploadSpeedLabel.setText("N/A");
    connectionStatusLabel.setText("Connection Status: Error");
  }

}