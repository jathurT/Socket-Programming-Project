package com.uor.eng;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Main extends Application {
  private VBox siteVBox;
  private TextField siteTextField;
  private final long thresholdLatency = 1000;
  private final Map<String, Thread> monitoringThreads = new HashMap<>();

  public static class MetricLabels {
    final Label latencyLabel = new Label("Latency: Pending...");
    final Label packetLossLabel = new Label("Packet Loss: Pending...");
    final Label throughputLabel = new Label("Throughput: Pending...");
    final Label jitterLabel = new Label("Jitter: Pending...");
    final Label qualityLabel = new Label("Connection Quality: Pending...");
    final Label errorRateLabel = new Label("Error Rate: Pending...");
    final Label downloadSpeedLabel = new Label("Download Speed: Pending...");
    final Label uploadSpeedLabel = new Label("Upload Speed: Pending...");
    final Label connectionStatusLabel = new Label("Connection Status: Pending...");

    MetricLabels() {
      String labelStyle = "-fx-font-size: 12px;";

      latencyLabel.setStyle(labelStyle);
      packetLossLabel.setStyle(labelStyle);
      throughputLabel.setStyle(labelStyle);
      jitterLabel.setStyle(labelStyle);
      qualityLabel.setStyle(labelStyle);
      errorRateLabel.setStyle(labelStyle);
      downloadSpeedLabel.setStyle(labelStyle);
      uploadSpeedLabel.setStyle(labelStyle);
      connectionStatusLabel.setStyle(labelStyle);
    }
  }

  @Override
  public void start(Stage primaryStage) {
    siteVBox = new VBox(10);
    siteTextField = new TextField();
    siteTextField.setPromptText("Enter website or IP address");

    Button addButton = new Button("Add Site");
    addButton.setOnAction(e -> addSite());

    Button stopButton = new Button("Stop All Monitoring");
    stopButton.setOnAction(e -> stopAllMonitoring());

    VBox controlBox = new VBox(10, siteTextField, addButton, stopButton);
    controlBox.setAlignment(Pos.CENTER);

    // Wrap the siteVBox in a ScrollPane to allow scrolling when there are many sites.
    ScrollPane scrollPane = new ScrollPane(siteVBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    VBox vbox = new VBox(10, controlBox, scrollPane);
    Scene scene = new Scene(vbox, 800, 600);
    primaryStage.setTitle("Network Monitoring Tool");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void addSite() {
    String siteAddress = siteTextField.getText().trim();
    if (!siteAddress.isEmpty()) {
      siteTextField.clear();

      // Create metrics labels and charts
      Label siteLabel = new Label("Site: " + siteAddress);
      siteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

      // Labels for the new metrics
      Label latencyLabel = new Label("Latency: Pending...");
      Label packetLossLabel = new Label("Packet Loss: Pending...");
      Label throughputLabel = new Label("Throughput: Pending...");
      Label jitterLabel = new Label("Jitter: Pending...");
      Label qualityLabel = new Label("Connection Quality: Pending...");
      Label errorRateLabel = new Label("Error Rate: Pending...");
      Label pingTimeLabel = new Label("Ping Time: Pending...");
      Label downloadSpeedLabel = new Label("Download Speed: Pending...");
      Label uploadSpeedLabel = new Label("Upload Speed: Pending...");
      Label rttLabel = new Label("RTT: Pending...");
      Label connectionStatusLabel = new Label("Connection Status: Pending...");

      // Create charts
      LineChart<Number, Number> metricsChart = createMetricsChart();
      XYChart.Series<Number, Number> latencySeries = new XYChart.Series<>();
      XYChart.Series<Number, Number> throughputSeries = new XYChart.Series<>();
      XYChart.Series<Number, Number> qualitySeries = new XYChart.Series<>();

      latencySeries.setName("Latency (ms)");
      throughputSeries.setName("Throughput (KB/s)");
      qualitySeries.setName("Quality Score");

      metricsChart.getData().addAll(latencySeries, throughputSeries, qualitySeries);

      Button stopButton = new Button("Stop");
      stopButton.setOnAction(e -> stopMonitoring(siteAddress));

      Button resumeButton = new Button("Resume");
      resumeButton.setOnAction(e -> resumeMonitoring(siteAddress, latencyLabel, packetLossLabel, throughputLabel,
          jitterLabel, qualityLabel, errorRateLabel, pingTimeLabel, downloadSpeedLabel,
          uploadSpeedLabel, rttLabel, connectionStatusLabel, latencySeries, throughputSeries, qualitySeries));

      HBox controlsBox = new HBox(10, siteLabel, stopButton, resumeButton);
      controlsBox.setAlignment(Pos.CENTER_LEFT);

      // GridPane to display all metrics in a clean layout
      GridPane metricsGrid = new GridPane();
      metricsGrid.setVgap(5);
      metricsGrid.setHgap(10);
      metricsGrid.add(latencyLabel, 0, 0);
      metricsGrid.add(packetLossLabel, 1, 0);
      metricsGrid.add(throughputLabel, 0, 1);
      metricsGrid.add(jitterLabel, 1, 1);
      metricsGrid.add(qualityLabel, 0, 2);
      metricsGrid.add(errorRateLabel, 1, 2);
      metricsGrid.add(pingTimeLabel, 0, 3);
      metricsGrid.add(downloadSpeedLabel, 1, 3);
      metricsGrid.add(uploadSpeedLabel, 0, 4);
      metricsGrid.add(rttLabel, 1, 4);
      metricsGrid.add(connectionStatusLabel, 0, 5, 2, 1);

      VBox siteBox = new VBox(10, controlsBox, metricsGrid, metricsChart);
      siteVBox.getChildren().add(siteBox);

      startMonitoring(siteAddress, latencyLabel, packetLossLabel, throughputLabel,
          jitterLabel, qualityLabel, errorRateLabel, pingTimeLabel,
          downloadSpeedLabel, uploadSpeedLabel, rttLabel, connectionStatusLabel,
          latencySeries, throughputSeries, qualitySeries);
    }
  }

  private LineChart<Number, Number> createMetricsChart() {
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Time (s)");
    yAxis.setLabel("Values");

    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle("Network Metrics Over Time");
    chart.setCreateSymbols(false);
    chart.setAnimated(false);
    return chart;
  }

  private void startMonitoring(String siteAddress, Label latencyLabel, Label packetLossLabel,
                               Label throughputLabel, Label jitterLabel, Label qualityLabel,
                               Label errorRateLabel, Label pingTimeLabel, Label downloadSpeedLabel,
                               Label uploadSpeedLabel, Label rttLabel, Label connectionStatusLabel,
                               XYChart.Series<Number, Number> latencySeries,
                               XYChart.Series<Number, Number> throughputSeries,
                               XYChart.Series<Number, Number> qualitySeries) {
    Thread monitorThread = new Thread(() -> {
      NetworkClient client = new NetworkClient(siteAddress, thresholdLatency);
      int sampleCount = 0;

      while (!Thread.currentThread().isInterrupted()) {
        try {
          NetworkClient.NetworkMetrics metrics = client.monitorNetwork();
          sampleCount++;

          int finalSampleCount = sampleCount;
          Platform.runLater(() -> {
            latencyLabel.setText(String.format("Latency: %.1f ms", metrics.getLatency()));
            packetLossLabel.setText(String.format("Packet Loss: %.2f%%", metrics.getPacketLoss()));
            throughputLabel.setText(String.format("Throughput: %.2f KB/s", metrics.getThroughput() / 1024));
            jitterLabel.setText(String.format("Jitter: %.2f ms", metrics.getJitter()));
            qualityLabel.setText(String.format("Quality: %.1f%%", metrics.getConnectionQuality()));
            errorRateLabel.setText(String.format("Error Rate: %.2f%%", metrics.getErrorRate()));
            pingTimeLabel.setText(String.format("Ping Time: %.1f ms", metrics.getLatency()));  // Example for Ping Time
            downloadSpeedLabel.setText(String.format("Download Speed: %.2f Mbps", metrics.getThroughput() / 1024 / 1024));  // Example for Download Speed
            uploadSpeedLabel.setText(String.format("Upload Speed: %.2f Mbps", metrics.getThroughput() / 1024 / 1024));  // Example for Upload Speed
            rttLabel.setText(String.format("RTT: %.2f ms", metrics.getLatency()));  // Example for RTT
            connectionStatusLabel.setText(metrics.isSuccessful() ? "Connection Status: Successful" : "Connection Status: Failed");

            if (latencySeries.getData().size() > 50) {
              latencySeries.getData().remove(0);
              throughputSeries.getData().remove(0);
              qualitySeries.getData().remove(0);
            }

            latencySeries.getData().add(new XYChart.Data<>(finalSampleCount, metrics.getLatency()));
            throughputSeries.getData().add(new XYChart.Data<>(finalSampleCount, metrics.getThroughput() / 1024));
            qualitySeries.getData().add(new XYChart.Data<>(finalSampleCount, metrics.getConnectionQuality()));

            if (metrics.getLatency() > thresholdLatency) {
              showAlert("High Latency Alert",
                  String.format("Latency for %s exceeded threshold: %.1f ms",
                      siteAddress, metrics.getLatency()));
            }
          });

          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        } catch (Exception e) {
          e.printStackTrace();
          Platform.runLater(() -> {
            latencyLabel.setText("Error: " + e.getMessage());
          });
        }
      }
    });

    monitoringThreads.put(siteAddress, monitorThread);
    monitorThread.start();
  }

  private void stopMonitoring(String siteAddress) {
    Thread thread = monitoringThreads.remove(siteAddress);
    if (thread != null) {
      thread.interrupt();
    }
  }

  private void resumeMonitoring(String siteAddress, Label latencyLabel, Label packetLossLabel,
                                Label throughputLabel, Label jitterLabel, Label qualityLabel,
                                Label errorRateLabel, Label pingTimeLabel, Label downloadSpeedLabel,
                                Label uploadSpeedLabel, Label rttLabel, Label connectionStatusLabel,
                                XYChart.Series<Number, Number> latencySeries,
                                XYChart.Series<Number, Number> throughputSeries,
                                XYChart.Series<Number, Number> qualitySeries) {
    startMonitoring(siteAddress, latencyLabel, packetLossLabel, throughputLabel,
        jitterLabel, qualityLabel, errorRateLabel, pingTimeLabel, downloadSpeedLabel,
        uploadSpeedLabel, rttLabel, connectionStatusLabel,
        latencySeries, throughputSeries, qualitySeries);
  }

  private void stopAllMonitoring() {
    monitoringThreads.forEach((site, thread) -> thread.interrupt());
    monitoringThreads.clear();
    siteVBox.getChildren().clear();
  }

  private void showAlert(String title, String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.show();
    });
  }

  public static void main(String[] args) {
    launch(args);
  }
}
