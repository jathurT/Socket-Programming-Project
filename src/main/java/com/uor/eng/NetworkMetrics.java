package com.uor.eng;

import lombok.Data;

/**
 * Holds all measured fields for network metrics.
 */
@Data
public class NetworkMetrics {
  private boolean successful;
  private String errorMessage;

  private double latency;         // ms
  private double dnsTime;         // ms
  private double tcpTime;         // ms
  private double tlsTime;         // ms
  private double ttfb;            // ms
  private double packetLoss;      // %
  private double throughput;      // B/s
  private double jitter;          // ms
  private double connectionQuality; // %
  private double mos;             // mean opinion score
  private double errorRate;       // %
  private double downloadSpeed;   // Mbps
  private double uploadSpeed;     // Mbps
  private double minPing;         // ms
  private double avgPing;         // ms
  private double maxPing;         // ms

  /**
   * Creates a failed metrics object with a given error message.
   */
  public static NetworkMetrics error(String message) {
    NetworkMetrics metrics = new NetworkMetrics();
    metrics.setSuccessful(false);
    metrics.setErrorMessage(message);
    return metrics;
  }

  /**
   * Creates a successful metrics object with all fields.
   */
  public static NetworkMetrics success(
      double latency,
      double dnsTime,
      double tcpTime,
      double tlsTime,
      double ttfb,
      double packetLoss,
      double throughput,
      double jitter,
      double connectionQuality,
      double mos,
      double errorRate,
      double downloadSpeed,
      double uploadSpeed,
      double minPing,
      double avgPing,
      double maxPing
  ) {
    NetworkMetrics metrics = new NetworkMetrics();
    metrics.setSuccessful(true);

    metrics.setLatency(latency);
    metrics.setDnsTime(dnsTime);
    metrics.setTcpTime(tcpTime);
    metrics.setTlsTime(tlsTime);
    metrics.setTtfb(ttfb);
    metrics.setPacketLoss(packetLoss);
    metrics.setThroughput(throughput);
    metrics.setJitter(jitter);
    metrics.setConnectionQuality(connectionQuality);
    metrics.setMos(mos);
    metrics.setErrorRate(errorRate);
    metrics.setDownloadSpeed(downloadSpeed);
    metrics.setUploadSpeed(uploadSpeed);
    metrics.setMinPing(minPing);
    metrics.setAvgPing(avgPing);
    metrics.setMaxPing(maxPing);

    return metrics;
  }
}
