package com.uor.eng;

public class MonitoringTask implements Runnable {
  private final String siteAddress;
  private final NetworkMetricsService metricsService;
  private final MonitoringSite site;

  public MonitoringTask(String siteAddress, NetworkMetricsService metricsService, MonitoringSite site) {
    this.siteAddress = siteAddress;
    this.metricsService = metricsService;
    this.site = site;
  }

  @Override
  public void run() {
    while (site.isRunning() && !Thread.currentThread().isInterrupted()) {
      try {
        NetworkMetrics metrics = metricsService.measureMetrics(siteAddress);
        site.updateMetrics(metrics);
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        site.updateMetrics(NetworkMetrics.error(e.getMessage()));
      }
    }
  }
}