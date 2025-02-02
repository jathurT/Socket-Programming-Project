package com.uor.eng;

import javafx.scene.control.Label;

public class MetricLabels {
  final Label latencyLabel = new Label("Latency: Pending...");
  final Label packetLossLabel = new Label("Packet Loss: Pending...");
  final Label throughputLabel = new Label("Throughput: Pending...");
  final Label jitterLabel = new Label("Jitter: Pending...");
  final Label qualityLabel = new Label("Connection Quality: Pending...");
  final Label errorRateLabel = new Label("Error Rate: Pending...");
  final Label downloadSpeedLabel = new Label("Download Speed: Pending...");
  final Label uploadSpeedLabel = new Label("Upload Speed: Pending...");
  final Label connectionStatusLabel = new Label("Connection Status: Pending...");

  public void updateFromMetrics(NetworkMetrics metrics) {
    latencyLabel.setText(String.format("Latency: %.1f ms", metrics.getLatency()));
    packetLossLabel.setText(String.format("Packet Loss: %.2f%%", metrics.getPacketLoss()));
    throughputLabel.setText(String.format("Throughput: %.2f KB/s", metrics.getThroughput() / 1024));
    jitterLabel.setText(String.format("Jitter: %.2f ms", metrics.getJitter()));
    qualityLabel.setText(String.format("Quality: %.1f%%", metrics.getConnectionQuality()));
    errorRateLabel.setText(String.format("Error Rate: %.2f%%", metrics.getErrorRate()));
    downloadSpeedLabel.setText(String.format("Download Speed: %.2f Mbps", metrics.getDownloadSpeed()));
    uploadSpeedLabel.setText(String.format("Upload Speed: %.2f Mbps", metrics.getUploadSpeed()));
    connectionStatusLabel.setText(metrics.isSuccessful() ? "Connection Status: Connected" : "Connection Status: Failed");
  }

  public void setAllStopped() {
    latencyLabel.setText("Latency: Stopped");
    packetLossLabel.setText("Packet Loss: Stopped");
    throughputLabel.setText("Throughput: Stopped");
    jitterLabel.setText("Jitter: Stopped");
    qualityLabel.setText("Quality: Stopped");
    errorRateLabel.setText("Error Rate: Stopped");
    downloadSpeedLabel.setText("Download Speed: Stopped");
    uploadSpeedLabel.setText("Upload Speed: Stopped");
    connectionStatusLabel.setText("Connection Status: Stopped");
  }
}
