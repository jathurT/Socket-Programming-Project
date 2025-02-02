package com.uor.eng;

import javafx.application.Application;
import javafx.stage.Stage;

public class NetworkMonitorApplication extends Application {
  private static final long THRESHOLD_LATENCY = 1000;

  @Override
  public void start(Stage primaryStage) {
    MonitoringManager monitoringManager = new MonitoringManager(THRESHOLD_LATENCY);
    MainViewController mainViewController = new MainViewController(monitoringManager);
    mainViewController.initialize(primaryStage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
