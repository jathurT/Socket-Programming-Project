package com.uor.eng;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
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

  private int totalRequests = 0;
  private int successfulRequests = 0;
  private int packetLossCount = 0;
  private long totalSentData = 0;
  private long totalReceivedData = 0;
  private long lastMeasurementTime = Instant.now().toEpochMilli();
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

      int responseCode = connection.getResponseCode();
      long currentTime = Instant.now().toEpochMilli();

      // Calculate data transfer
      long sentData = connection.getRequestProperty("Content-Length") != null ?
          Integer.parseInt(connection.getRequestProperty("Content-Length")) : 0;
      long receivedData = Math.max(connection.getContentLength(), 0);

      totalSentData += sentData;
      totalReceivedData += receivedData;

      // Calculate throughput (bytes per second)
      long timeDiff = currentTime - lastMeasurementTime;
      if (timeDiff > 0) {
        currentThroughput = ((double) (sentData + receivedData) / timeDiff) * 1000; // Convert to bytes/second
      }
      lastMeasurementTime = currentTime;

      // Calculate latency
      long latency = (System.nanoTime() - startTime) / 1000000; // Convert to milliseconds
      latencyHistory.offer(latency);
      if (latencyHistory.size() > HISTORY_SIZE) {
        latencyHistory.poll();
      }

      if (responseCode == HttpURLConnection.HTTP_OK) {
        successfulRequests++;
        metrics.setLatency(latency);
      } else {
        packetLossCount++;
        metrics.setLatency(-1);
      }
      totalRequests++;

      // Set all metrics
      metrics.setPacketLoss(getPacketLossPercentage());
      metrics.setThroughput(currentThroughput);
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

    public double getLatency() { return (double) latency; }  // Convert to double

  }
}