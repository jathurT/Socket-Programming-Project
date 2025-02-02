package com.uor.eng;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class NetworkMetricsService {
  private final long thresholdLatency;
  private final Queue<Long> latencyHistory;
  private static final int HISTORY_SIZE = 10;
  private static final int BUFFER_SIZE = 8192;
  private static final int TEST_FILE_SIZE = 1000000; // 1MB for testing download speed

  public NetworkMetricsService(long thresholdLatency) {
    this.thresholdLatency = thresholdLatency;
    this.latencyHistory = new LinkedList<>();
  }

  public NetworkMetrics measureMetrics(String address) throws IOException {
    long startTime = System.nanoTime();
    HttpURLConnection connection = createConnection(address);

    try {
      connection.connect();
      NetworkMetrics metrics = collectMetrics(connection, startTime);
      updateLatencyHistory((long) metrics.getLatency());

      if (metrics.getLatency() > thresholdLatency) {
        AlertService.showLatencyAlert(address, metrics.getLatency());
      }

      return metrics;
    } finally {
      connection.disconnect();
    }
  }

  private HttpURLConnection createConnection(String address) throws IOException {
    URL url = new URL(normalizeAddress(address));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
    return connection;
  }

  private String normalizeAddress(String address) {
    if (!address.startsWith("http://") && !address.startsWith("https://")) {
      return "https://" + address;
    }
    return address;
  }

  private NetworkMetrics collectMetrics(HttpURLConnection connection, long startTime) throws IOException {
    int responseCode = connection.getResponseCode();
    long latency = (System.nanoTime() - startTime) / 1000000;  // Convert to milliseconds

    if (responseCode != HttpURLConnection.HTTP_OK) {
      return NetworkMetrics.error("HTTP Error: " + responseCode);
    }

    // Calculate bandwidth metrics
    BandwidthMetrics bandwidthMetrics = calculateBandwidthMetrics(connection);

    // Calculate jitter from latency history
    double jitter = calculateJitter();

    // Calculate packet loss (simulated)
    double packetLoss = calculatePacketLoss(connection);

    // Calculate connection quality score (0-100)
    double quality = calculateConnectionQuality(latency, bandwidthMetrics.downloadSpeed,
        jitter, packetLoss);

    return NetworkMetrics.success(
        latency,
        packetLoss,
        bandwidthMetrics.throughput,
        jitter,
        quality,
        bandwidthMetrics.errorRate,
        bandwidthMetrics.downloadSpeed,
        bandwidthMetrics.uploadSpeed
    );
  }

  private static class BandwidthMetrics {
    double throughput;
    double errorRate;
    double downloadSpeed;
    double uploadSpeed;
  }

  private BandwidthMetrics calculateBandwidthMetrics(HttpURLConnection connection) throws IOException {
    BandwidthMetrics metrics = new BandwidthMetrics();

    // Measure download speed
    long startTime = System.nanoTime();
    long bytesRead = 0;
    int errors = 0;

    try (InputStream in = connection.getInputStream()) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int read;
      while ((read = in.read(buffer)) != -1) {
        bytesRead += read;
        if (read < BUFFER_SIZE) {
          errors++;
        }
      }
    }

    long duration = System.nanoTime() - startTime;
    double seconds = duration / 1_000_000_000.0;

    metrics.throughput = bytesRead / seconds;  // bytes per second
    metrics.downloadSpeed = (bytesRead / seconds) / (1024 * 1024);  // Convert to Mbps
    metrics.uploadSpeed = metrics.downloadSpeed * 0.2;  // Simulate upload speed (typically lower)
    metrics.errorRate = (errors * BUFFER_SIZE * 100.0) / bytesRead;  // Error rate percentage

    return metrics;
  }

  private double calculateJitter() {
    if (latencyHistory.size() < 2) {
      return 0.0;
    }

    double sum = 0;
    Long previous = null;
    for (Long current : latencyHistory) {
      if (previous != null) {
        sum += Math.abs(current - previous);
      }
      previous = current;
    }

    return sum / (latencyHistory.size() - 1);
  }

  private double calculatePacketLoss(HttpURLConnection connection) {
    // Simulate packet loss based on response headers and connection properties
    int contentLength = connection.getContentLength();
    return contentLength <= 0 ? 5.0 : 0.5;  // Higher loss if content length is invalid
  }

  private double calculateConnectionQuality(double latency, double downloadSpeed,
                                            double jitter, double packetLoss) {
    // Calculate quality score (0-100)
    double latencyScore = Math.max(0, 100 - (latency / 10));
    double speedScore = Math.min(100, downloadSpeed * 10);
    double jitterScore = Math.max(0, 100 - (jitter * 2));
    double packetLossScore = Math.max(0, 100 - (packetLoss * 10));

    return (latencyScore + speedScore + jitterScore + packetLossScore) / 4;
  }

  private void updateLatencyHistory(long latency) {
    latencyHistory.offer(latency);
    if (latencyHistory.size() > HISTORY_SIZE) {
      latencyHistory.poll();
    }
  }
}