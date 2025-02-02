package com.uor.eng;

import lombok.Data;

@Data
public class NetworkMetrics {
  private double latency;
  private double packetLoss;
  private double throughput;
  private double jitter;
  private double connectionQuality;
  private double errorRate;
  private double downloadSpeed;
  private double uploadSpeed;
  private boolean successful;
  private String errorMessage;

  public static NetworkMetrics error(String message) {
    NetworkMetrics metrics = new NetworkMetrics();
    metrics.setSuccessful(false);
    metrics.setErrorMessage(message);
    return metrics;
  }

  public static NetworkMetrics success(double latency, double packetLoss, double throughput,
                                       double jitter, double quality, double errorRate,
                                       double downloadSpeed, double uploadSpeed) {
    NetworkMetrics metrics = new NetworkMetrics();
    metrics.setSuccessful(true);
    metrics.setLatency(latency);
    metrics.setPacketLoss(packetLoss);
    metrics.setThroughput(throughput);
    metrics.setJitter(jitter);
    metrics.setConnectionQuality(quality);
    metrics.setErrorRate(errorRate);
    metrics.setDownloadSpeed(downloadSpeed);
    metrics.setUploadSpeed(uploadSpeed);
    return metrics;
  }
}