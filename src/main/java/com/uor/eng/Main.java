package com.uor.eng;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main JavaFX Application entry point.
 */

public class Main extends Application {
  private final MonitoringController controller;

  public Main() {
    this.controller = new MonitoringController();
  }

  @Override
  public void start(Stage primaryStage) {
    MonitoringView view = new MonitoringView(controller);
    view.initialize(primaryStage);
  }

  @Override
  public void stop() {
    controller.stopAllMonitoring();
  }

  public static void main(String[] args) {
    launch(args);
  }
}


