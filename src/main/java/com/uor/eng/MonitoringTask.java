package com.uor.eng;

import javafx.application.Platform;

public class MonitoringTask {
  private final NetworkClient networkClient;
  private final MonitoringView monitoringView;
  private Thread monitorThread;
  private volatile boolean running;

  public MonitoringTask(String siteAddress, long thresholdLatency, MonitoringView monitoringView) {
    this.networkClient = new NetworkClient(siteAddress, thresholdLatency);
    this.monitoringView = monitoringView;
  }

  public void start() {
    running = true;
    monitorThread = new Thread(this::monitoringLoop);
    monitorThread.start();
  }

  public void stop() {
    running = false;
    if (monitorThread != null) {
      monitorThread.interrupt();
    }
  }

  private void monitoringLoop() {
    int sampleCount = 0;
    while (running && !Thread.currentThread().isInterrupted()) {
      try {
        NetworkMetrics metrics = networkClient.monitorNetwork();
        final int currentSample = ++sampleCount;

        Platform.runLater(() -> monitoringView.updateMetrics(metrics, currentSample));
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        Platform.runLater(() -> monitoringView.showError(e.getMessage()));
      }
    }
    Platform.runLater(() -> monitoringView.showStopped());
  }
}
