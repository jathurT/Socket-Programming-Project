package com.uor.eng;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MultipleDevicesMonitoringVisulal extends Application {

    private TextArea outputArea;
    private TextField ipField;
    private Button startButton, stopButton;
    private volatile boolean isRunning = false;

    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> bandwidthSeries;
    private XYChart.Series<Number, Number> pingSeries;
    private int timeCounter = 0; // Used to increment x-axis for real-time plotting

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
        outputArea.setPrefHeight(150); // Set a fixed height for the output area

        startButton = new Button("Start Monitoring");
        stopButton = new Button("Stop Monitoring");
        stopButton.setDisable(true);

        startButton.setOnAction(e -> startMonitoring());
        stopButton.setOnAction(e -> stopMonitoring());

        // Create the line chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis.setLabel("Value");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(200); // Make the chart smaller
        lineChart.setLegendVisible(true); // Show the legend for the series

        bandwidthSeries = new XYChart.Series<>();
        bandwidthSeries.setName("Bandwidth (Mbps)");

        pingSeries = new XYChart.Series<>();
        pingSeries.setName("Ping (ms)");

        lineChart.getData().addAll(bandwidthSeries, pingSeries);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(ipField, startButton, stopButton, lineChart, outputArea);

        primaryStage.setScene(new Scene(layout, 600, 500));
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
                pb.command("cmd.exe", "/c", "ping -n 1 " + ip);
            } else {
                pb.command("sh", "-c", "ping -c 1 " + ip);
            }

            while (isRunning) {
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null && isRunning) {
                    String finalLine = line;
                    Platform.runLater(() -> {
                        outputArea.appendText(finalLine + "\n");
                        extractPingData(finalLine); // Process the ping data and plot it
                    });
                }

                process.waitFor();
                Thread.sleep(1000); // Ping every second
            }
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
                Platform.runLater(() -> {
                    outputArea.appendText(finalLine + "\n");
                    extractBandwidthData(finalLine); // Process the bandwidth data and plot it
                });
            }

            process.waitFor();
        } catch (Exception e) {
            Platform.runLater(() -> outputArea.appendText("Error running iperf3: " + e.getMessage() + "\n"));
        }
    }

    private void extractPingData(String line) {
        // Assuming ping output includes time=xxx ms
        if (line.contains("time=")) {
            try {
                String[] parts = line.split("time=");
                String pingTimeStr = parts[1].split(" ")[0]; // Extracting the time value
                double pingTime = Double.parseDouble(pingTimeStr);

                // Create a new data point with the current timeCounter and pingTime
                XYChart.Data<Number, Number> pingData = new XYChart.Data<>(timeCounter++, pingTime);

                // Set the color to green for the ping data point
                pingData.getNode().setStyle("-fx-stroke: green; -fx-fill: green;");

                // Plot the ping data point on the chart
                Platform.runLater(() -> {
                    pingSeries.getData().add(pingData);
                });

            } catch (Exception e) {
                Platform.runLater(() -> outputArea.appendText("Error processing ping data: " + e.getMessage() + "\n"));
            }
        }
    }

    private void extractBandwidthData(String line) {
        // Assuming iperf outputs data like: [ 5]  0.0-1.0 sec  1000 MBytes  8.00 Gbits/sec
        if (line.contains("Mbits/sec")) {
            try {
                String[] parts = line.split("\\s+");
                double bandwidth = Double.parseDouble(parts[6]); // Extracting the bandwidth value

                // Create a new data point with the current timeCounter and bandwidth
                XYChart.Data<Number, Number> bandwidthData = new XYChart.Data<>(timeCounter++, bandwidth);

                // Plot the bandwidth data point on the chart
                Platform.runLater(() -> {
                    bandwidthSeries.getData().add(bandwidthData);
                });
            } catch (Exception e) {
                Platform.runLater(() -> outputArea.appendText("Error processing bandwidth data: " + e.getMessage() + "\n"));
            }
        }
    }
}
