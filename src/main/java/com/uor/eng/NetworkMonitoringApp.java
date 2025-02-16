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

public class NetworkMonitoringApp extends Application {

    private TextArea outputArea;
    private TextField ipField;
    private Button startButton, stopButton;
    private volatile boolean isRunning = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Network Performance Monitor");

        ipField = new TextField();
        ipField.setPromptText("Enter target IP (e.g., 192.168.1.1)");

        outputArea = new TextArea();
        outputArea.setEditable(false);

        startButton = new Button("Start Monitoring");
        stopButton = new Button("Stop Monitoring");
        stopButton.setDisable(true);

        startButton.setOnAction(e -> startMonitoring());
        stopButton.setOnAction(e -> stopMonitoring());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(ipField, startButton, stopButton, outputArea);

        primaryStage.setScene(new Scene(layout, 500, 400));
        primaryStage.show();
    }

    private void startMonitoring() {
        String ipAddress = ipField.getText().trim();
        if (ipAddress.isEmpty()) {
            outputArea.appendText("Please enter a valid IP address.\n");
            return;
        }

        isRunning = true;
        startButton.setDisable(true);
        stopButton.setDisable(false);
        outputArea.appendText("Monitoring started for " + ipAddress + "...\n");

        new Thread(() -> runPing(ipAddress)).start();  // Run ping in a separate thread
        new Thread(() -> runIperf(ipAddress)).start(); // Run iperf in a separate thread
    }

    private void stopMonitoring() {
        isRunning = false;
        startButton.setDisable(false);
        stopButton.setDisable(true);
        outputArea.appendText("Monitoring stopped.\n");
    }

    private void runPing(String ip) {
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
                String finalLine = line;
                Platform.runLater(() -> outputArea.appendText(finalLine + "\n"));
            }

            process.waitFor();
        } catch (Exception e) {
            Platform.runLater(() -> outputArea.appendText("Error running ping: " + e.getMessage() + "\n"));
        }
    }

    private void runIperf(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("iperf3", "-c", ip, "-t", "5");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null && isRunning) {
                String finalLine = line;
                Platform.runLater(() -> outputArea.appendText(finalLine + "\n"));
            }

            process.waitFor();
        } catch (Exception e) {
            Platform.runLater(() -> outputArea.appendText("Error running iperf3: " + e.getMessage() + "\n"));
        }
    }
}
