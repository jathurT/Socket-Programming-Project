package com.uor.eng.filetransfervisual;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferApp extends Application {
    private static final int PORT = 12347;
    private File selectedFile;
    private XYChart.Series<Number, Number> latencySeries = new XYChart.Series<>();
    private int latencyCount = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Transfer and Network Metrics");

        // UI Elements
        TextField ipAddressField = new TextField();
        ipAddressField.setPromptText("Enter IP address (e.g., 127.0.0.1)");

        Button browseButton = new Button("Browse File");
        Button sendFileButton = new Button("Send File");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        TextArea networkMetricsArea = new TextArea();
        networkMetricsArea.setEditable(false);

        // Line Chart for Latency
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Attempt");
        yAxis.setLabel("Latency (ms)");
        LineChart<Number, Number> latencyChart = new LineChart<>(xAxis, yAxis);
        latencyChart.setTitle("Latency Over Time");
        latencySeries.setName("Latency");
        latencyChart.getData().add(latencySeries);

        // Layout for the UI
        VBox vbox = new VBox(10, new Label("IP Address:"), ipAddressField, browseButton, sendFileButton,
                new Label("Network Metrics:"), networkMetricsArea, progressBar, latencyChart);
        vbox.setPadding(new Insets(15));

        // File chooser setup
        FileChooser fileChooser = new FileChooser();
        browseButton.setOnAction(e -> {
            selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                networkMetricsArea.appendText("Selected file: " + selectedFile.getAbsolutePath() + "\n");
            }
        });

        // Action for sending files
        sendFileButton.setOnAction(e -> {
            String ipAddress = ipAddressField.getText();
            if (selectedFile != null && !ipAddress.isEmpty()) {
                networkMetricsArea.appendText("Sending file to: " + ipAddress + "\n");
                showNetworkMetrics(ipAddress, networkMetricsArea);

                // Execute file transfer in a separate thread
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> sendFile(ipAddress, selectedFile, progressBar, networkMetricsArea));
            } else {
                networkMetricsArea.appendText("Please select a file and enter a valid IP address.\n");
            }
        });

        // Set the Scene
        Scene scene = new Scene(vbox, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendFile(String ipAddress, File file, ProgressBar progressBar, TextArea networkMetricsArea) {
        long startTime = System.nanoTime();

        try (Socket socket = new Socket(ipAddress, PORT);
             InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             FileInputStream fileInputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             OutputStream outputStream = socket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            // Wait for Server ACK
            String serverAck = dataInputStream.readUTF();
            if (!serverAck.equals("ACK")) {
                networkMetricsArea.appendText("Server didn't acknowledge connection!\n");
                return;
            }

            long firstByteSentTime = System.nanoTime();
            double latencyMs = (firstByteSentTime - startTime) / 1_000_000.0;
            Platform.runLater(() -> updateLatencyChart(latencyMs));

            // Send the file name
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesSent = 0;
            long fileSize = file.length();

            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                double progress = (double) totalBytesSent / fileSize;
                progressBar.setProgress(progress);
            }

            long endTime = System.nanoTime();
            double transferTime = (endTime - firstByteSentTime) / 1_000_000_000.0;
            double throughput = (totalBytesSent * 8) / (transferTime * 1_000_000.0);

            networkMetricsArea.appendText("File sent successfully.\n");
            networkMetricsArea.appendText(String.format("Total Bytes Sent: %d bytes\n", totalBytesSent));
            networkMetricsArea.appendText(String.format("Latency: %.2f ms\n", latencyMs));
            networkMetricsArea.appendText(String.format("Transfer Time: %.2f seconds\n", transferTime));
            networkMetricsArea.appendText(String.format("Throughput: %.2f Mbps\n", throughput));

        } catch (IOException ex) {
            networkMetricsArea.appendText("Error during file transfer: " + ex.getMessage() + "\n");
        }
    }

    private void updateLatencyChart(double latencyMs) {
        latencySeries.getData().add(new XYChart.Data<>(latencyCount++, latencyMs));
    }

    private void showNetworkMetrics(String ipAddress, TextArea networkMetricsArea) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            long start = System.currentTimeMillis();
            boolean reachable = address.isReachable(1000);
            long end = System.currentTimeMillis();

            if (reachable) {
                long pingTime = end - start;
                networkMetricsArea.appendText("Ping time: " + pingTime + " ms\n");
                Platform.runLater(() -> updateLatencyChart((double) pingTime));
            } else {
                networkMetricsArea.appendText("Unable to reach IP address.\n");
            }
        } catch (Exception e) {
            networkMetricsArea.appendText("Error: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

