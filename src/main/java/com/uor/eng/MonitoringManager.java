package com.uor.eng;

import java.util.HashMap;
import java.util.Map;

public class MonitoringManager {
  private final Map<String, MonitoringTask> monitoringTasks;
  private final long thresholdLatency;

  public MonitoringManager(long thresholdLatency) {
    this.monitoringTasks = new HashMap<>();
    this.thresholdLatency = thresholdLatency;
  }

  public void startMonitoring(String siteAddress, MonitoringView monitoringView) {
    stopMonitoring(siteAddress);
    MonitoringTask task = new MonitoringTask(siteAddress, thresholdLatency, monitoringView);
    monitoringTasks.put(siteAddress, task);
    task.start();
  }

  public void stopMonitoring(String siteAddress) {
    MonitoringTask task = monitoringTasks.get(siteAddress);
    if (task != null) {
      task.stop();
      monitoringTasks.remove(siteAddress);
    }
  }

  public void stopAllMonitoring() {
    monitoringTasks.forEach((site, task) -> task.stop());
    monitoringTasks.clear();
  }
}
