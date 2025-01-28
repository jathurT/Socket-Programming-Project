package com.uor.eng.ui;


import com.uor.eng.model.Host;
import com.uor.eng.model.Log;
import com.uor.eng.model.User;
import com.uor.eng.service.DatabaseService;
import com.uor.eng.service.MonitoringService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

@Slf4j
public class MainWindow {
  private final DatabaseService databaseService;
  private final MonitoringService monitoringService;
  private final User currentUser;
  private TableView<Host> hostsTable;
  private TableView<Log> logsTable;

  public MainWindow(DatabaseService databaseService, MonitoringService monitoringService, User currentUser) {
    this.databaseService = databaseService;
    this.monitoringService = monitoringService;
    this.currentUser = currentUser;
  }

  public void show(Stage stage) {
    VBox root = new VBox(10);
    root.setPadding(new Insets(10));

    // Menu Bar
    MenuBar menuBar = createMenuBar();
    root.getChildren().add(menuBar);

    // Hosts Table
    hostsTable = createHostsTable();
    root.getChildren().add(new TitledPane("Monitored Hosts", hostsTable));

    // Logs Table
    logsTable = createLogsTable();
    root.getChildren().add(new TitledPane("Recent Logs", logsTable));

    // Add Host Button
    Button addHostButton = new Button("Add New Host");
    addHostButton.setOnAction(e -> showAddHostDialog());
    root.getChildren().add(addHostButton);

    // Scene Setup
    Scene scene = new Scene(root, 800, 600);
    stage.setTitle("Network Monitor - " + currentUser.getUsername());
    stage.setScene(scene);
    stage.show();

    // Start periodic refresh
    startPeriodicRefresh();
  }

  private MenuBar createMenuBar() {
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    MenuItem refreshMenuItem = new MenuItem("Refresh");
    refreshMenuItem.setOnAction(e -> refreshData());
    MenuItem exitMenuItem = new MenuItem("Exit");
    exitMenuItem.setOnAction(e -> Platform.exit());
    fileMenu.getItems().addAll(refreshMenuItem, exitMenuItem);
    menuBar.getMenus().add(fileMenu);
    return menuBar;
  }

  private TableView<Host> createHostsTable() {
    TableView<Host> table = new TableView<>();
    table.getColumns().addAll(
        createColumn("Name", (Host host) -> host.getName()),
        createColumn("Hostname", (Host host) -> host.getHostname()),
        createColumn("Type", (Host host) -> host.getType().name()),
        createColumn("Status", (Host host) -> host.getStatus())
    );
    return table;
  }

  private TableView<Log> createLogsTable() {
    TableView<Log> table = new TableView<>();
    TableColumn<Log, LocalDateTime> timeColumn = new TableColumn<>("Time");
    timeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTime()));
    timeColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(LocalDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setText(null);
        } else {
          setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
      }
    });

    // Host column - show host name instead of ID
    TableColumn<Log, String> hostColumn = new TableColumn<>("Host");
    hostColumn.setCellValueFactory(data -> {
      Host host = databaseService.getUserHosts(currentUser.getId()).stream()
          .filter(h -> h.getId().equals(data.getValue().getHostId()))
          .findFirst()
          .orElse(null);
      return new SimpleObjectProperty<>(host != null ? host.getName() : "Unknown");
    });

    // Status columns
    TableColumn<Log, String> oldStatusColumn = new TableColumn<>("Old Status");
    oldStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getOldStatus()));

    TableColumn<Log, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));

    // Response time column
    TableColumn<Log, String> responseTimeColumn = new TableColumn<>("Response Time");
    responseTimeColumn.setCellValueFactory(data -> {
      Double responseTime = data.getValue().getResponseTime();
      return new SimpleObjectProperty<>(responseTime != null ? responseTime + " ms" : "N/A");
    });

    table.getColumns().addAll(timeColumn, hostColumn, oldStatusColumn, statusColumn, responseTimeColumn);
    return table;
  }

  private <T, U> TableColumn<T, U> createColumn(String title, Function<T, U> mapper) {
    TableColumn<T, U> column = new TableColumn<>(title);
    column.setCellValueFactory(data -> new SimpleObjectProperty<>(mapper.apply(data.getValue())));
    return column;
  }

  private void showAddHostDialog() {
    AddHostDialog dialog = new AddHostDialog(databaseService, currentUser);
    dialog.showAndWait().ifPresent(e -> refreshData());
  }

  private void refreshData() {
    List<Host> hosts = databaseService.getUserHosts(currentUser.getId());
    hostsTable.getItems().setAll(hosts);

    hosts.forEach(host -> {
      monitoringService.startMonitoring(host);
      List<Log> hostLogs = databaseService.getHostLogs(host.getId(), 100);
      logsTable.getItems().addAll(hostLogs);
    });
  }

  private void startPeriodicRefresh() {
    Timer timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(() -> refreshData());
      }
    }, 0, 60000);
  }
}
