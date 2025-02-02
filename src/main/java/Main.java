import com.uor.eng.NetworkClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

  private VBox siteVBox;
  private TextField siteTextField;
  private long thresholdLatency = 1000; // Threshold for latency in ms

  @Override
  public void start(Stage primaryStage) {
    siteVBox = new VBox(10);

    siteTextField = new TextField();
    siteTextField.setPromptText("Enter website or IP address");

    Button addButton = new Button("Add Site");
    addButton.setOnAction(e -> addSite());

    Button stopButton = new Button("Stop Monitoring");
    stopButton.setOnAction(e -> stopMonitoring());

    // Main layout
    VBox vbox = new VBox(10, siteTextField, addButton, stopButton, siteVBox);
    Scene scene = new Scene(vbox, 600, 600);
    primaryStage.setTitle("Network Monitoring Tool");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void addSite() {
    String siteAddress = siteTextField.getText().trim();
    if (!siteAddress.isEmpty()) {
      siteTextField.clear();

      // Create a container for the site
      HBox siteBox = new HBox(10);
      Label siteLabel = new Label("Monitoring: " + siteAddress);
      Label latencyLabel = new Label("Latency: Pending...");
      Label packetLossLabel = new Label("Packet Loss: Pending...");
      Label responseTimeLabel = new Label("Response Time: Pending...");
      Label bandwidthLabel = new Label("Bandwidth Usage: Pending...");
      Label uptimeLabel = new Label("Uptime: Pending...");
      Label errorLabel = new Label("Errors: 0");
      Label jitterLabel = new Label("Jitter: Pending...");

      // Create LineChart for Latency over time
      NumberAxis xAxis = new NumberAxis();
      NumberAxis yAxis = new NumberAxis();
      xAxis.setLabel("Time (s)");
      yAxis.setLabel("Latency (ms)");

      LineChart<Number, Number> latencyChart = new LineChart<>(xAxis, yAxis);
      XYChart.Series<Number, Number> latencySeries = new XYChart.Series<>();
      latencySeries.setName("Latency");
      latencyChart.getData().add(latencySeries);

      Button monitorButton = new Button("Monitor");

      monitorButton.setOnAction(e -> monitorSite(siteAddress, latencyLabel, packetLossLabel, responseTimeLabel, bandwidthLabel, uptimeLabel, errorLabel, jitterLabel, latencySeries));

      siteBox.getChildren().addAll(siteLabel, latencyLabel, packetLossLabel, responseTimeLabel, bandwidthLabel, uptimeLabel, errorLabel, jitterLabel, monitorButton, latencyChart);
      siteVBox.getChildren().add(siteBox);
    }
  }


// ... other imports

  private void monitorSite(String siteAddress, Label latencyLabel, Label packetLossLabel, Label responseTimeLabel, Label bandwidthLabel, Label uptimeLabel, Label errorLabel, Label jitterLabel, XYChart.Series<Number, Number> latencySeries) {
    new Thread(() -> {
      try {
        NetworkClient client = new NetworkClient(siteAddress, thresholdLatency);
        int packetLossCount = 0;
        long totalResponseTime = 0;
        int sampleCount = 0;
        long lastResponseTime = 0;
        long totalErrors = 0;
        long uptime = 0;
        long startTime = System.currentTimeMillis();

        while (true) {
          long latency = client.monitorLatency();
          totalResponseTime += latency;
          sampleCount++;

          long avgResponseTime = totalResponseTime / sampleCount;
          long jitter = Math.abs(latency - lastResponseTime);
          lastResponseTime = latency;

          uptime = (System.currentTimeMillis() - startTime) / 1000;

          // Updating the UI on the FX thread
          long finalUptime = uptime;
          Platform.runLater(() -> {
            latencyLabel.setText("Latency: " + latency + " ms");
            packetLossLabel.setText("Packet Loss: " + client.getPacketLossPercentage() + " %");
            responseTimeLabel.setText("Avg Response Time: " + avgResponseTime + " ms");
            bandwidthLabel.setText("Bandwidth Usage: " + client.getBandwidthUsage() + " KB");
            jitterLabel.setText("Jitter: " + jitter + " ms");
            uptimeLabel.setText("Uptime: " + finalUptime + " s");
            errorLabel.setText("Errors: " + totalErrors);
          });

          latencySeries.getData().add(new XYChart.Data<>(sampleCount, latency));
          if (latencySeries.getData().size() > 100) {
            latencySeries.getData().remove(0);
          }

          if (latency > thresholdLatency) {
            showAlert("High Latency Alert", "Latency for " + siteAddress + " exceeded the threshold: " + latency + " ms");
          }

          Thread.sleep(1000); // Update every second
        }
      } catch (Exception e) {
        e.printStackTrace();
        Platform.runLater(() -> latencyLabel.setText("Latency: Error"));
      }
    }).start();
  }


  private void stopMonitoring() {
    siteVBox.getChildren().clear();
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
