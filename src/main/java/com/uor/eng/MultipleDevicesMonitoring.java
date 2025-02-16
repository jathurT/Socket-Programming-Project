package com.uor.eng;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MultipleDevicesMonitoring extends Application {

    private TextArea outputArea;
    private TextField ipField1, ipField2, ipField3;
    private Button startButton, stopButton;
    private volatile boolean isRunning = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Network Performance Monitor");

        // IP address input fields for three devices
        ipField1 = new TextField();
        ipField1.setPromptText("Enter target IP 1 (e.g., 192.168.1.1)");

        ipField2 = new TextField();
        ipField2.setPromptText("Enter target IP 2 (e.g., 192.168.1.2)");

        ipField3 = new TextField();
        ipField3.setPromptText("Enter target IP 3 (e.g., 192.168.1.3)");

        outputArea = new TextArea();
        outputArea.setEditable(false);

        startButton = new Button("Start Monitoring");
        stopButton = new Button("Stop Monitoring");
        stopButton.setDisable(true);

        startButton.setOnAction(e -> startMonitoring());
        stopButton.setOnAction(e -> stopMonitoring());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(ipField1, ipField2, ipField3, startButton, stopButton, outputArea);

        primaryStage.setScene(new Scene(layout, 500, 500));
        primaryStage.show();
    }

    private void startMonitoring() {
        String ip1 = ipField1.getText().trim();
        String ip2 = ipField2.getText().trim();
        String ip3 = ipField3.getText().trim();

        if (ip1.isEmpty() || ip2.isEmpty() || ip3.isEmpty()) {
            outputArea.appendText("Please enter valid IP addresses for all three devices.\n");
            return;
        }

        isRunning = true;
        startButton.setDisable(true);
        stopButton.setDisable(false);
        outputArea.appendText("Monitoring started...\n");

        // Start monitoring for all three IPs
        new Thread(() -> runPing(ip1, "Device 1")).start();
        new Thread(() -> runIperf(ip1, "Device 1")).start();

        new Thread(() -> runPing(ip2, "Device 2")).start();
        new Thread(() -> runIperf(ip2, "Device 2")).start();

        new Thread(() -> runPing(ip3, "Device 3")).start();
        new Thread(() -> runIperf(ip3, "Device 3")).start();
    }

    private void stopMonitoring() {
        isRunning = false;
        startButton.setDisable(false);
        stopButton.setDisable(true);
        outputArea.appendText("Monitoring stopped.\n");
    }

    private void runPing(String ip, String deviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd.exe", "/c", "ping -n 4 " + ip);
            } else {
                pb.command("sh", "-c", "ping -c 4 " + ip);
            }

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null && isRunning) {
                String finalLine = deviceName + " - " + line;
                Platform.runLater(() -> outputArea.appendText(finalLine + "\n"));
            }

            process.waitFor();
        } catch (Exception e) {
            Platform.runLater(() -> outputArea.appendText("Error running ping for " + deviceName + ": " + e.getMessage() + "\n"));
        }
    }

    private void runIperf(String ip, String deviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("iperf3", "-c", ip, "-t", "5");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null && isRunning) {
                String finalLine = deviceName + " - " + line;
                Platform.runLater(() -> outputArea.appendText(finalLine + "\n"));
            }

            process.waitFor();
        } catch (Exception e) {
            Platform.runLater(() -> outputArea.appendText("Error running iperf3 for " + deviceName + ": " + e.getMessage() + "\n"));
        }
    }
}
