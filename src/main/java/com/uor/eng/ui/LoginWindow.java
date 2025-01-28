package com.uor.eng.ui;

import com.uor.eng.model.User;
import com.uor.eng.service.DatabaseService;
import com.uor.eng.service.MonitoringService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginWindow {
  private final DatabaseService databaseService;
  private final Stage primaryStage;

  public LoginWindow(DatabaseService databaseService, Stage primaryStage) {
    this.databaseService = databaseService;
    this.primaryStage = primaryStage;
  }

  public void show() {
    VBox root = new VBox(10);
    root.setPadding(new Insets(20));
    root.setAlignment(Pos.CENTER);

    // Login form
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setAlignment(Pos.CENTER);

    Label usernameLabel = new Label("Username:");
    TextField usernameField = new TextField();
    Label passwordLabel = new Label("Password:");
    PasswordField passwordField = new PasswordField();

    gridPane.add(usernameLabel, 0, 0);
    gridPane.add(usernameField, 1, 0);
    gridPane.add(passwordLabel, 0, 1);
    gridPane.add(passwordField, 1, 1);

    // Buttons
    Button loginButton = new Button("Login");
    Button registerButton = new Button("Register");
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(loginButton, registerButton);

    root.getChildren().addAll(gridPane, buttonBox);

    // Login button action
    loginButton.setOnAction(e -> {
      String username = usernameField.getText();
      String password = passwordField.getText();

      databaseService.loginUser(username, password).ifPresentOrElse(
          user -> {
            openMainWindow(user);
          },
          () -> showError("Invalid username or password")
      );
    });

    // Register button action
    registerButton.setOnAction(e -> showRegistrationDialog());

    Scene scene = new Scene(root, 400, 300);
    primaryStage.setTitle("Network Monitor - Login");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void showRegistrationDialog() {
    Dialog<User> dialog = new Dialog<>();
    dialog.setTitle("Register New User");
    dialog.setHeaderText("Enter your details");

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));

    TextField fullNameField = new TextField();
    TextField emailField = new TextField();
    TextField usernameField = new TextField();
    PasswordField passwordField = new PasswordField();

    grid.add(new Label("Full Name:"), 0, 0);
    grid.add(fullNameField, 1, 0);
    grid.add(new Label("Email:"), 0, 1);
    grid.add(emailField, 1, 1);
    grid.add(new Label("Username:"), 0, 2);
    grid.add(usernameField, 1, 2);
    grid.add(new Label("Password:"), 0, 3);
    grid.add(passwordField, 1, 3);

    dialog.getDialogPane().setContent(grid);

    ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == registerButtonType) {
        return User.builder()
            .fullName(fullNameField.getText())
            .email(emailField.getText())
            .username(usernameField.getText())
            .password(passwordField.getText())
            .build();
      }
      return null;
    });

    dialog.showAndWait().ifPresent(user -> {
      try {
        databaseService.registerUser(user);
        showInfo("Registration successful! Please login.");
      } catch (Exception e) {
        log.error("Error registering user", e);
        showError("Registration failed. Please try again.");
      }
    });
  }

  private void openMainWindow(User user) {
    MainWindow mainWindow = new MainWindow(databaseService, new MonitoringService(databaseService), user);
    mainWindow.show(primaryStage);
  }

  private void showError(String message) {
    new Alert(Alert.AlertType.ERROR, message).show();
  }

  private void showInfo(String message) {
    new Alert(Alert.AlertType.INFORMATION, message).show();
  }
}