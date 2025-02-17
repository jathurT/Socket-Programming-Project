package com.uor.eng;

/**
 * A background task that continuously measures metrics for a single site.
 * It stops when the site is no longer running or the thread is interrupted.
 */
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
        // Fetch fresh metrics
        NetworkMetrics metrics = metricsService.measureMetrics(siteAddress);
        // Update the site's UI
        site.updateMetrics(metrics);

        // Sleep 1 second before the next measurement
        Thread.sleep(1000);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break; // Stop gracefully if interrupted

      } catch (Exception e) {
        // If there's any exception, show error metrics and keep going
        site.updateMetrics(NetworkMetrics.error(e.getMessage()));
      }
    }
  }
}
