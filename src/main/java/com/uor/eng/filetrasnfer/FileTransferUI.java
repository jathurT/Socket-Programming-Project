package com.uor.eng.filetrasnfer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class FileTransferUI extends Application {
    private TableView<ClientData> table;
    private ObservableList<ClientData> clientList;
    private TextField clientIPField;
    private File selectedFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Multi-Client File Transfer");

        // Table Setup
        table = new TableView<>();
        clientList = FXCollections.observableArrayList();
        table.setItems(clientList);

        // Define Table Columns
        TableColumn<ClientData, String> ipColumn = new TableColumn<>("Client IP");
        ipColumn.setCellValueFactory(cellData -> cellData.getValue().clientIPProperty());

        TableColumn<ClientData, String> fileColumn = new TableColumn<>("File Name");
        fileColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());

        TableColumn<ClientData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        TableColumn<ClientData, Long> pingColumn = new TableColumn<>("Ping (ms)");
        pingColumn.setCellValueFactory(cellData -> cellData.getValue().pingProperty().asObject());

        TableColumn<ClientData, Long> latencyColumn = new TableColumn<>("Latency (ms)");
        latencyColumn.setCellValueFactory(cellData -> cellData.getValue().latencyProperty().asObject());

        TableColumn<ClientData, Double> throughputColumn = new TableColumn<>("Throughput (MB/s)");
        throughputColumn.setCellValueFactory(cellData -> cellData.getValue().throughputProperty().asObject());

        TableColumn<ClientData, Double> packetLossColumn = new TableColumn<>("Packet Loss (%)");
        packetLossColumn.setCellValueFactory(cellData -> cellData.getValue().packetLossProperty().asObject());

        TableColumn<ClientData, Double> jitterColumn = new TableColumn<>("Jitter (ms)");
        jitterColumn.setCellValueFactory(cellData -> cellData.getValue().jitterProperty().asObject());

        TableColumn<ClientData, Double> bandwidthColumn = new TableColumn<>("Bandwidth Utilization (%)");
        bandwidthColumn.setCellValueFactory(cellData -> cellData.getValue().bandwidthUtilizationProperty().asObject());

        TableColumn<ClientData, Double> uploadSpeedColumn = new TableColumn<>("Upload Speed (MB/s)");
        uploadSpeedColumn.setCellValueFactory(cellData -> cellData.getValue().uploadSpeedProperty().asObject());

        table.getColumns().addAll(ipColumn, fileColumn, statusColumn, pingColumn, latencyColumn,
                throughputColumn, packetLossColumn, jitterColumn, bandwidthColumn, uploadSpeedColumn);

        // Client IP Entry Field
        clientIPField = new TextField();
        clientIPField.setPromptText("Enter Client IP");

        // Buttons
        Button addClientBtn = new Button("Add Client");
        addClientBtn.setOnAction(e -> addClient());

        Button browseFileBtn = new Button("Browse File");
        browseFileBtn.setOnAction(e -> selectFile(primaryStage));

        Button startTransferBtn = new Button("Start Transfer");
        startTransferBtn.setOnAction(e -> startFileTransfer());

        Button removeRowBtn = new Button("Remove Selected");
        removeRowBtn.setOnAction(e -> removeSelectedRow());

        // Layout
        HBox inputBox = new HBox(10, clientIPField, addClientBtn, browseFileBtn, startTransferBtn, removeRowBtn);
        inputBox.setPadding(new Insets(10));

        VBox root = new VBox(10, table, inputBox);
        root.setPadding(new Insets(10));

        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();
    }

    private void addClient() {
        String clientIP = clientIPField.getText().trim();
        if (!clientIP.isEmpty()) {
            clientList.add(new ClientData(clientIP, "", "Pending", 0, 0, 0, 0, 0, 0, 0));
            clientIPField.clear();
        }
    }

    private void selectFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            for (ClientData client : clientList) {
                client.setFileName(selectedFile.getName());
            }
        }
    }

    private void startFileTransfer() {
        if (selectedFile == null || clientList.isEmpty()) {
            showAlert("Error", "Please add clients and select a file before starting the transfer.");
            return;
        }

        for (ClientData client : clientList) {
            new Thread(() -> {
                try {
                    // Simulate file transfer
                    long startTime = System.nanoTime();
                    Thread.sleep(2000); // Simulated transfer delay
                    long endTime = System.nanoTime();

                    // Fake metrics
                    long ping = (long) (Math.random() * 100);
                    long latency = (long) (Math.random() * 50);
                    double throughput = Math.random() * 10;
                    double packetLoss = Math.random() * 5;
                    double jitter = Math.random() * 10;
                    double bandwidthUtilization = Math.random() * 100;
                    double uploadSpeed = Math.random() * 50;

                    // Update UI
                    client.setPing(ping);
                    client.setLatency(latency);
                    client.setThroughput(throughput);
                    client.setPacketLoss(packetLoss);
                    client.setJitter(jitter);
                    client.setBandwidthUtilization(bandwidthUtilization);
                    client.setUploadSpeed(uploadSpeed);
                    client.setStatus("Success");
                } catch (InterruptedException e) {
                    client.setStatus("Failed");
                }
            }).start();
        }
    }

    private void removeSelectedRow() {
        ClientData selectedClient = table.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            clientList.remove(selectedClient);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

