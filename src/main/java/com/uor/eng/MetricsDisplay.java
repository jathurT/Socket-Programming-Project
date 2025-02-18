package com.uor.eng;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * Displays detailed metrics for a single site (latency, packet loss, throughput, etc.).
 */
public class MetricsDisplay {
  @Getter
  private final GridPane grid;

  private final Label latencyLabel;
  private final Label dnsTimeLabel;
  private final Label tcpTimeLabel;
  private final Label tlsTimeLabel;
  private final Label ttfbLabel;
  private final Label packetLossLabel;
  private final Label throughputLabel;
  private final Label jitterLabel;
  private final Label qualityLabel;
  private final Label mosLabel;
  private final Label errorRateLabel;
  private final Label downloadSpeedLabel;
  private final Label uploadSpeedLabel;
  private final Label pingStatsLabel;
  private final Label connectionStatusLabel;

  public MetricsDisplay() {
    this.grid = new GridPane();

    // Initialize labels with a consistent style
    this.latencyLabel         = createMetricLabel("Latency");
    this.dnsTimeLabel         = createMetricLabel("DNS Resolution");
    this.tcpTimeLabel         = createMetricLabel("TCP Connection");
    this.tlsTimeLabel         = createMetricLabel("TLS Handshake");
    this.ttfbLabel            = createMetricLabel("Time to First Byte");
    this.packetLossLabel      = createMetricLabel("Packet Loss");
    this.throughputLabel      = createMetricLabel("Throughput");
    this.jitterLabel          = createMetricLabel("Jitter");
    this.qualityLabel         = createMetricLabel("Connection Quality");
    this.mosLabel             = createMetricLabel("MOS Score");
    this.errorRateLabel       = createMetricLabel("Error Rate");
    this.downloadSpeedLabel   = createMetricLabel("Download Speed");
    this.uploadSpeedLabel     = createMetricLabel("Upload Speed");
    this.pingStatsLabel       = createMetricLabel("Ping Statistics");
    this.connectionStatusLabel= createMetricLabel("Status");

    setupGrid();
    setupTooltips();
  }

  private Label createMetricLabel(String name) {
    Label label = new Label("Pending...");
    label.getStyleClass().add("metric-label");
    return label;
  }

  private void setupGrid() {
    grid.setVgap(5);
    grid.setHgap(10);
    grid.getStyleClass().add("metrics-grid");

    // Column 1
    grid.add(createMetricGroup("Latency Metrics",
        latencyLabel, dnsTimeLabel, tcpTimeLabel, tlsTimeLabel, ttfbLabel), 0, 0);

    // Column 2
    grid.add(createMetricGroup("Performance Metrics",
        throughputLabel, jitterLabel, packetLossLabel, pingStatsLabel), 1, 0);

    // Column 3
    grid.add(createMetricGroup("Quality Metrics",
        qualityLabel, mosLabel, errorRateLabel), 0, 1);

    // Column 4
    grid.add(createMetricGroup("Speed Metrics",
        downloadSpeedLabel, uploadSpeedLabel), 1, 1);

    // Status (spans both columns)
    grid.add(connectionStatusLabel, 0, 2, 2, 1);
  }

  private VBox createMetricGroup(String title, Label... labels) {
    VBox group = new VBox(5);
    group.getStyleClass().add("metric-group");

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("group-title");
    group.getChildren().add(titleLabel);

    for (Label label : labels) {
      group.getChildren().add(label);
    }

    return group;
  }

  private void setupTooltips() {
    Tooltip.install(latencyLabel, new Tooltip("Round-trip time for network packets"));
    Tooltip.install(dnsTimeLabel, new Tooltip("Time taken to resolve domain name"));
    Tooltip.install(tcpTimeLabel, new Tooltip("Time to establish TCP connection"));
    Tooltip.install(tlsTimeLabel, new Tooltip("Time for SSL/TLS handshake"));
    Tooltip.install(ttfbLabel, new Tooltip("Time to receive first byte of response"));
    // Add more tooltips for other metrics if desired
  }

  /**
   * Called when we have fresh metrics to display.
   */
  public void update(NetworkMetrics metrics) {
    if (!metrics.isSuccessful()) {
      updateError(metrics.getErrorMessage());
      return;
    }
    updateMetrics(metrics);
  }

  private void updateMetrics(NetworkMetrics metrics) {
    latencyLabel.setText(String.format("Latency: %.1f ms", metrics.getLatency()));
    dnsTimeLabel.setText(String.format("DNS: %.1f ms", metrics.getDnsTime()));
    tcpTimeLabel.setText(String.format("TCP: %.1f ms", metrics.getTcpTime()));
    tlsTimeLabel.setText(String.format("TLS: %.1f ms", metrics.getTlsTime()));
    ttfbLabel.setText(String.format("TTFB: %.1f ms", metrics.getTtfb()));
    packetLossLabel.setText(String.format("Packet Loss: %.2f%%", metrics.getPacketLoss()));
    throughputLabel.setText(String.format("Throughput: %.2f KB/s", metrics.getThroughput() / 1024));
    jitterLabel.setText(String.format("Jitter: %.2f ms", metrics.getJitter()));
    qualityLabel.setText(String.format("Quality: %.1f%%", metrics.getConnectionQuality()));
    mosLabel.setText(String.format("MOS: %.1f", metrics.getMos()));
    errorRateLabel.setText(String.format("Error Rate: %.2f%%", metrics.getErrorRate()));
    downloadSpeedLabel.setText(String.format("Download: %.2f Mbps", metrics.getDownloadSpeed()));
    uploadSpeedLabel.setText(String.format("Upload: %.2f Mbps", metrics.getUploadSpeed()));
    pingStatsLabel.setText(String.format("Ping min/avg/max: %.1f/%.1f/%.1f ms",
        metrics.getMinPing(), metrics.getAvgPing(), metrics.getMaxPing()));

    // Update connection status
    updateConnectionStatus(metrics);
  }

  private void updateConnectionStatus(NetworkMetrics metrics) {
    String status = "Connected";
    String styleClass = "status-good";

    // Simple logic for demonstration; adjust thresholds as needed
    if (metrics.getLatency() > 1000) {
      status = "High Latency";
      styleClass = "status-warning";
    } else if (metrics.getPacketLoss() > 5) {
      status = "High Packet Loss";
      styleClass = "status-warning";
    }

    connectionStatusLabel.setText("Status: " + status);
    connectionStatusLabel.getStyleClass().removeAll("status-good", "status-warning", "status-error");
    connectionStatusLabel.getStyleClass().add(styleClass);
  }

  /**
   * Called when there's an error (unsuccessful metrics retrieval).
   */
  private void updateError(String error) {
    // Label all metrics as error or "N/A"
    latencyLabel.setText("Error: " + error);
    dnsTimeLabel.setText("DNS: N/A");
    tcpTimeLabel.setText("TCP: N/A");
    tlsTimeLabel.setText("TLS: N/A");
    ttfbLabel.setText("TTFB: N/A");
    packetLossLabel.setText("Packet Loss: N/A");
    throughputLabel.setText("Throughput: N/A");
    jitterLabel.setText("Jitter: N/A");
    qualityLabel.setText("Quality: N/A");
    mosLabel.setText("MOS: N/A");
    errorRateLabel.setText("Error Rate: N/A");
    downloadSpeedLabel.setText("Download: N/A");
    uploadSpeedLabel.setText("Upload: N/A");
    pingStatsLabel.setText("Ping: N/A");

    connectionStatusLabel.setText("Status: Error");
    connectionStatusLabel.getStyleClass().removeAll("status-good", "status-warning", "status-error");
    connectionStatusLabel.getStyleClass().add("status-error");
  }
}
