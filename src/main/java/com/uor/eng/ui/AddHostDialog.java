package com.uor.eng.ui;

import com.uor.eng.model.Host;
import com.uor.eng.model.HostType;
import com.uor.eng.model.User;
import com.uor.eng.service.DatabaseService;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AddHostDialog extends Dialog<Void> {
  private final DatabaseService databaseService;
  private final User currentUser;

  public AddHostDialog(DatabaseService databaseService, User currentUser) {
    this.databaseService = databaseService;
    this.currentUser = currentUser;

    setTitle("Add New Host");
    setHeaderText("Enter details for the new host");

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    TextField nameField = new TextField();
    TextField hostnameField = new TextField();
    ComboBox<HostType> typeComboBox = new ComboBox<>();
    typeComboBox.getItems().setAll(HostType.values());

    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Hostname:"), 0, 1);
    grid.add(hostnameField, 1, 1);
    grid.add(new Label("Type:"), 0, 2);
    grid.add(typeComboBox, 1, 2);

    getDialogPane().setContent(grid);

    ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

    setResultConverter(dialogButton -> {
      if (dialogButton == saveButton) {
        Host host = Host.builder()
            .name(nameField.getText())
            .hostname(hostnameField.getText())
            .type(typeComboBox.getValue())
            .userId(currentUser.getId())
            .status("UNKNOWN")
            .build();
        try {
          databaseService.addHost(host);
          return null;
        } catch (Exception e) {
          log.error("Error adding host", e);
          new Alert(Alert.AlertType.ERROR, "Failed to add host.").show();
        }
      }
      return null;
    });
  }
}
