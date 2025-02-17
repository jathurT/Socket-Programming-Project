package com.uor.eng;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

public class MonitoringController {
  private final Map<String, MonitoringSite> monitoringSites = new ConcurrentHashMap<>();
  private ExecutorService executorService;
  private final NetworkMetricsService metricsService;
  private static final long DEFAULT_THRESHOLD_LATENCY = 1000;
  private volatile boolean isShutdown = false;

  public MonitoringController() {
    this.executorService = Executors.newCachedThreadPool();
    this.metricsService = new NetworkMetricsService(DEFAULT_THRESHOLD_LATENCY);
  }

  public void startMonitoring(String siteAddress, MonitoringSite site) {
    synchronized (this) {
      if (isShutdown) {
        executorService = Executors.newCachedThreadPool();
        isShutdown = false;
      }

      if (!monitoringSites.containsKey(siteAddress)) {
        site.setMonitoringTask(createMonitoringTask(siteAddress, site));
        executorService.submit(site.getMonitoringTask());
        monitoringSites.put(siteAddress, site);
      }
    }
  }

  private Runnable createMonitoringTask(String siteAddress, MonitoringSite site) {
    return new MonitoringTask(siteAddress, metricsService, site);
  }

  public void stopMonitoring(String siteAddress) {
    synchronized (this) {
      MonitoringSite site = monitoringSites.get(siteAddress);
      if (site != null) {
        site.stop();
        monitoringSites.remove(siteAddress);
      }
    }
  }

  public void stopAllMonitoring() {
    synchronized (this) {
      new ArrayList<>(monitoringSites.keySet()).forEach(this::stopMonitoring);

      if (executorService != null && !executorService.isShutdown()) {
        executorService.shutdown();
        isShutdown = true;
      }
    }
  }

  public void restart() {
    synchronized (this) {
      if (isShutdown) {
        executorService = Executors.newCachedThreadPool();
        isShutdown = false;
      }
    }
  }
}