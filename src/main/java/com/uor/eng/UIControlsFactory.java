package com.uor.eng;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class UIControlsFactory {
  private final VBox siteVBox;
  private final MonitoringManager monitoringManager;
  private final TextField siteTextField;

  public UIControlsFactory(VBox siteVBox, MonitoringManager monitoringManager) {
    this.siteVBox = siteVBox;
    this.monitoringManager = monitoringManager;
    this.siteTextField = new TextField();
  }

  public VBox createControlBox() {
    siteTextField.setPromptText("Enter website or IP address");

    Button addButton = new Button("Add Site");
    addButton.setOnAction(e -> addNewSite());

    Button stopAllButton = new Button("Stop All Monitoring");
    stopAllButton.setOnAction(e -> stopAllMonitoring());

    VBox controlBox = new VBox(10, siteTextField, addButton, stopAllButton);
    controlBox.setAlignment(Pos.CENTER);
    return controlBox;
  }

  private void addNewSite() {
    String siteAddress = siteTextField.getText().trim();
    if (!siteAddress.isEmpty()) {
      MonitoringView monitoringView = new MonitoringView(siteAddress);
      siteVBox.getChildren().add(monitoringView.getContainer());
      monitoringManager.startMonitoring(siteAddress, monitoringView);
      siteTextField.clear();
    }
  }

  private void stopAllMonitoring() {
    monitoringManager.stopAllMonitoring();
    siteVBox.getChildren().clear();
  }
}
