import com.uor.eng.NetworkClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {
  private VBox siteVBox;
  private TextField siteTextField;
  private long thresholdLatency = 1000;
  private Map<String, Thread> monitoringThreads = new HashMap<>();

  @Override
  public void start(Stage primaryStage) {
    siteVBox = new VBox(10);
    siteTextField = new TextField();
    siteTextField.setPromptText("Enter website or IP address");

    Button addButton = new Button("Add Site");
    addButton.setOnAction(e -> addSite());

    Button stopButton = new Button("Stop All Monitoring");
    stopButton.setOnAction(e -> stopAllMonitoring());

    VBox vbox = new VBox(10, siteTextField, addButton, stopButton, siteVBox);
    Scene scene = new Scene(vbox, 800, 600);
    primaryStage.setTitle("Enhanced Network Monitoring Tool");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void addSite() {
    String siteAddress = siteTextField.getText().trim();
    if (!siteAddress.isEmpty()) {
      siteTextField.clear();

      // Create metrics labels
      Label siteLabel = new Label("Site: " + siteAddress);
      Label latencyLabel = new Label("Latency: Pending...");
      Label packetLossLabel = new Label("Packet Loss: Pending...");
      Label throughputLabel = new Label("Throughput: Pending...");
      Label jitterLabel = new Label("Jitter: Pending...");
      Label qualityLabel = new Label("Connection Quality: Pending...");
      Label errorRateLabel = new Label("Error Rate: Pending...");

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

      VBox siteBox = new VBox(10);
      HBox controlsBox = new HBox(10, siteLabel, stopButton);
      HBox metricsBox = new HBox(10, latencyLabel, packetLossLabel, throughputLabel,
          jitterLabel, qualityLabel, errorRateLabel);
      siteBox.getChildren().addAll(controlsBox, metricsBox, metricsChart);
      siteVBox.getChildren().add(siteBox);

      startMonitoring(siteAddress, latencyLabel, packetLossLabel, throughputLabel,
          jitterLabel, qualityLabel, errorRateLabel,
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
                               Label errorRateLabel, XYChart.Series<Number, Number> latencySeries,
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
            // Update labels with correct format specifiers
            latencyLabel.setText(String.format("Latency: %.1f ms", metrics.getLatency()));
            packetLossLabel.setText(String.format("Packet Loss: %.2f%%", metrics.getPacketLoss()));
            throughputLabel.setText(String.format("Throughput: %.2f KB/s", metrics.getThroughput() / 1024));
            jitterLabel.setText(String.format("Jitter: %.2f ms", metrics.getJitter()));
            qualityLabel.setText(String.format("Quality: %.1f%%", metrics.getConnectionQuality()));
            errorRateLabel.setText(String.format("Error Rate: %.2f%%", metrics.getErrorRate()));

            // Update charts with current sample count
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