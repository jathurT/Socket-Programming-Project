package com.uor.eng;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class NetworkClient {
  private final String serverAddress;
  private final long thresholdLatency;
  private final Queue<Long> latencyHistory = new LinkedList<>();
  private final int HISTORY_SIZE = 10;
  private final int BUFFER_SIZE = 8192;

  private int totalRequests = 0;
  private int successfulRequests = 0;
  private int packetLossCount = 0;
  private long totalSentData = 0;
  private long totalReceivedData = 0;
  private long lastMeasurementTime = Instant.now().toEpochMilli();
  private double downloadSpeed = 0.0;
  private double uploadSpeed = 0.0;
  private double currentThroughput = 0.0;

  public NetworkClient(String serverAddress, long thresholdLatency) {
    if (!serverAddress.startsWith("http://") && !serverAddress.startsWith("https://")) {
      serverAddress = "https://" + serverAddress;
    }
    this.serverAddress = serverAddress;
    this.thresholdLatency = thresholdLatency;
  }

  public NetworkMetrics monitorNetwork() {
    long startTime = System.nanoTime();
    HttpURLConnection connection = null;
    NetworkMetrics metrics = new NetworkMetrics();

    try {
      URL url = new URL(serverAddress);
      connection = serverAddress.startsWith("https://") ?
          (HttpsURLConnection) url.openConnection() :
          (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      // Start measuring download
      connection.connect();
      int responseCode = connection.getResponseCode();
      long receivedData = 0;

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Measure download speed
        try (InputStream in = connection.getInputStream()) {
          byte[] buffer = new byte[BUFFER_SIZE];
          int bytesRead;
          long downloadStartTime = System.currentTimeMillis();

          while ((bytesRead = in.read(buffer)) != -1) {
            receivedData += bytesRead;
          }

          long downloadTime = System.currentTimeMillis() - downloadStartTime;
          if (downloadTime > 0) {
            downloadSpeed = (receivedData * 1000.0) / (downloadTime * 1024 * 1024); // Convert to Mbps
          }
        }
      }

      // Simulate and measure upload speed (since we can't actually upload in this context)
      long uploadStartTime = System.currentTimeMillis();
      byte[] testData = new byte[BUFFER_SIZE * 100]; // 800KB test data
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Simulate upload by measuring how fast we can write to a ByteArrayOutputStream
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          out.write(testData);
        }
        long uploadTime = System.currentTimeMillis() - uploadStartTime;
        if (uploadTime > 0) {
          uploadSpeed = (testData.length * 1000.0) / (uploadTime * 1024 * 1024); // Convert to Mbps
        }
      }

      long currentTime = Instant.now().toEpochMilli();
      long latency = (System.nanoTime() - startTime) / 1000000; // Convert to milliseconds

      // Update metrics
      if (responseCode == HttpURLConnection.HTTP_OK) {
        successfulRequests++;
        metrics.setLatency(latency);
        metrics.setDownloadSpeed(downloadSpeed);
        metrics.setUploadSpeed(uploadSpeed);
        currentThroughput = ((downloadSpeed + uploadSpeed) * 1024 * 1024) / 8.0; // Convert Mbps to bytes/sec
        metrics.setThroughput(currentThroughput);
      } else {
        packetLossCount++;
        metrics.setLatency(-1);
      }
      totalRequests++;

      // Set all metrics
      latencyHistory.offer(latency);
      if (latencyHistory.size() > HISTORY_SIZE) {
        latencyHistory.poll();
      }

      metrics.setPacketLoss(getPacketLossPercentage());
      metrics.setBandwidthUsage(getBandwidthUsage());
      metrics.setJitter(calculateJitter());
      metrics.setErrorRate(getErrorRate());
      metrics.setConnectionQuality(calculateConnectionQuality());
      metrics.setSuccessful(responseCode == HttpURLConnection.HTTP_OK);

    } catch (IOException e) {
      packetLossCount++;
      totalRequests++;
      metrics.setSuccessful(false);
      metrics.setErrorMessage(e.getMessage());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

    return metrics;
  }

  private double calculateJitter() {
    if (latencyHistory.size() < 2) return 0.0;

    double sum = 0;
    long prev = latencyHistory.peek();
    for (long current : latencyHistory) {
      sum += Math.abs(current - prev);
      prev = current;
    }
    return sum / (latencyHistory.size() - 1);
  }

  private double getErrorRate() {
    return totalRequests == 0 ? 0 :
        ((double) (totalRequests - successfulRequests) / totalRequests) * 100;
  }

  private double calculateConnectionQuality() {
    // Score from 0-100 based on multiple factors
    double latencyScore = latencyHistory.isEmpty() ? 0 :
        Math.max(0, 100 - (latencyHistory.stream().mapToLong(l -> l).average().orElse(0) / 10));
    double packetLossScore = Math.max(0, 100 - getPacketLossPercentage() * 2);
    double errorScore = Math.max(0, 100 - getErrorRate() * 2);
    double jitterScore = Math.max(0, 100 - calculateJitter());

    return (latencyScore + packetLossScore + errorScore + jitterScore) / 4;
  }

  public double getPacketLossPercentage() {
    return totalRequests == 0 ? 0.0 : ((double) packetLossCount / totalRequests) * 100;
  }

  public double getBandwidthUsage() {
    return (totalSentData + totalReceivedData) / 1024.0;  // Convert to KB
  }

  @Setter
  public static class NetworkMetrics {
    // Setters remain the same
    private long latency;
    @Getter
    private double packetLoss;
    @Getter
    private double throughput;
    @Getter
    private double bandwidthUsage;
    @Getter
    private double jitter;
    @Getter
    private double errorRate;
    @Getter
    private double connectionQuality;
    @Getter
    private boolean successful;
    @Getter
    private String errorMessage;
    @Getter
    private double downloadSpeed;
    @Getter
    private double uploadSpeed;

    public double getLatency() {
      return (double) latency;
    }  // Convert to double

  }
}
