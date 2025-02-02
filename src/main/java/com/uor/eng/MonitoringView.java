package com.uor.eng;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class MonitoringView {
  private final MonitoringController controller;
  private final GridPane sitesGrid;
  private final TextField siteInput;
  private final Map<String, MonitoringSite> activeSites;
  private int currentRow = 0;
  private int currentCol = 0;
  private static final int MAX_COLUMNS = 2;
  private final ScrollPane scrollPane;

  public MonitoringView(MonitoringController controller) {
    this.controller = controller;
    this.sitesGrid = new GridPane();
    this.siteInput = new TextField();
    this.activeSites = new HashMap<>();

    // Configure GridPane
    sitesGrid.setHgap(10);
    sitesGrid.setVgap(10);
    sitesGrid.setPadding(new Insets(10));

    // Configure ScrollPane
    this.scrollPane = new ScrollPane(sitesGrid);
    scrollPane.setFitToWidth(true);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
  }

  public void initialize(Stage primaryStage) {
    VBox root = createMainLayout();
    Scene scene = new Scene(root, 1200, 800); // Increased default window size

    primaryStage.setTitle("Network Monitoring Tool");
    primaryStage.setScene(scene);
    primaryStage.show();

    primaryStage.setOnCloseRequest(event -> controller.stopAllMonitoring());
  }

  private VBox createMainLayout() {
    VBox controlBox = createControlBox();

    // Make monitoring views take up equal space
    ColumnConstraints column = new ColumnConstraints();
    column.setPercentWidth(50); // Each column takes 50% of the width
    sitesGrid.getColumnConstraints().addAll(column, column);

    VBox root = new VBox(10, controlBox, scrollPane);
    root.setPadding(new Insets(10));
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    return root;
  }

  private VBox createControlBox() {
    siteInput.setPromptText("Enter website or IP address (comma-separated for multiple sites)");
    siteInput.setPrefWidth(400);

    Button addButton = new Button("Add Site(s)");
    addButton.setOnAction(e -> addNewSites());

    siteInput.setOnAction(e -> addNewSites());

    Button stopAllButton = new Button("Stop All Monitoring");
    stopAllButton.setOnAction(e -> {
      controller.stopAllMonitoring();
      sitesGrid.getChildren().clear();
      activeSites.clear();
      currentRow = 0;
      currentCol = 0;
    });

    HBox controls = new HBox(10, siteInput, addButton, stopAllButton);
    controls.setAlignment(Pos.CENTER);

    VBox controlBox = new VBox(10, controls);
    controlBox.setAlignment(Pos.CENTER);
    return controlBox;
  }

  private void addNewSites() {
    String[] sites = siteInput.getText().trim().split(",");
    for (String site : sites) {
      String siteAddress = site.trim();
      if (!siteAddress.isEmpty() && !activeSites.containsKey(siteAddress)) {
        addSite(siteAddress);
      }
    }
    siteInput.clear();
  }

  private void addSite(String siteAddress) {
    controller.restart();
    MonitoringSite site = new MonitoringSite(siteAddress, () -> stopSite(siteAddress));

    // Configure the site view for grid layout
    site.getView().setPrefWidth(550);
    site.getView().setMaxWidth(550);

    // Add to grid
    sitesGrid.add(site.getView(), currentCol, currentRow);

    // Update grid position
    currentCol++;
    if (currentCol >= MAX_COLUMNS) {
      currentCol = 0;
      currentRow++;
    }

    activeSites.put(siteAddress, site);
    controller.startMonitoring(siteAddress, site);
  }

  private void stopSite(String siteAddress) {
    MonitoringSite site = activeSites.remove(siteAddress);
    if (site != null) {
      controller.stopMonitoring(siteAddress);
      sitesGrid.getChildren().remove(site.getView());

      // Reorganize remaining sites
      reorganizeGrid();
    }
  }

  private void reorganizeGrid() {
    // Store all remaining sites
    var remainingSites = new HashMap<>(activeSites);

    // Clear the grid and reset counters
    sitesGrid.getChildren().clear();
    currentRow = 0;
    currentCol = 0;

    // Re-add all remaining sites
    remainingSites.forEach((address, site) -> {
      sitesGrid.add(site.getView(), currentCol, currentRow);
      currentCol++;
      if (currentCol >= MAX_COLUMNS) {
        currentCol = 0;
        currentRow++;
      }
    });
  }
}