package com.uor.eng;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainViewController {
  private final UIControlsFactory uiControlsFactory;
  private final MonitoringManager monitoringManager;
  private final VBox siteVBox;

  public MainViewController(MonitoringManager monitoringManager) {
    this.siteVBox = new VBox(10);
    this.monitoringManager = monitoringManager;
    this.uiControlsFactory = new UIControlsFactory(siteVBox, monitoringManager);
  }

  public void initialize(Stage primaryStage) {
    VBox controlBox = uiControlsFactory.createControlBox();
    ScrollPane scrollPane = createScrollPane();

    VBox mainLayout = new VBox(10, controlBox, scrollPane);
    Scene scene = new Scene(mainLayout, 800, 600);

    configureStage(primaryStage, scene);
  }

  private ScrollPane createScrollPane() {
    ScrollPane scrollPane = new ScrollPane(siteVBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    return scrollPane;
  }

  private void configureStage(Stage stage, Scene scene) {
    stage.setTitle("Network Monitoring Tool");
    stage.setScene(scene);
    stage.show();
  }
}
