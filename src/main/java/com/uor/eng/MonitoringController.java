package com.uor.eng;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonitoringController {
  private final Map<String, MonitoringSite> monitoringSites = new HashMap<>();
  private ExecutorService executorService;
  private final NetworkMetricsService metricsService;
  private static final long DEFAULT_THRESHOLD_LATENCY = 1000;
  private boolean isShutdown = false;

  public MonitoringController() {
    this.executorService = Executors.newCachedThreadPool();
    this.metricsService = new NetworkMetricsService(DEFAULT_THRESHOLD_LATENCY);
  }

  public synchronized void startMonitoring(String siteAddress, MonitoringSite site) {
    // Check if the service is shutdown
    if (isShutdown) {
      // Recreate the ExecutorService if it was shutdown
      executorService = Executors.newCachedThreadPool();
      isShutdown = false;
    }

    if (!monitoringSites.containsKey(siteAddress)) {
      site.setMonitoringTask(createMonitoringTask(siteAddress, site));
      executorService.submit(site.getMonitoringTask());
      monitoringSites.put(siteAddress, site);
    }
  }

  private Runnable createMonitoringTask(String siteAddress, MonitoringSite site) {
    return new MonitoringTask(siteAddress, metricsService, site);
  }

  public synchronized void stopMonitoring(String siteAddress) {
    MonitoringSite site = monitoringSites.get(siteAddress);
    if (site != null) {
      site.stop();
      monitoringSites.remove(siteAddress);
    }
  }

  public synchronized void stopAllMonitoring() {
    // Create a new ArrayList with the keys to avoid ConcurrentModificationException
    new ArrayList<>(monitoringSites.keySet()).forEach(this::stopMonitoring);

    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdown();
      isShutdown = true;
    }
  }

  public synchronized void restart() {
    if (isShutdown) {
      executorService = Executors.newCachedThreadPool();
      isShutdown = false;
    }
  }
}