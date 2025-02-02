package com.uor.eng;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkClient {
  private final String serverAddress;
  private long thresholdLatency;

  private int totalRequests = 0;
  private int successfulRequests = 0;
  private int packetLossCount = 0;

  private long totalSentData = 0;
  private long totalReceivedData = 0;

  public NetworkClient(String serverAddress, long thresholdLatency) {
    // Add http:// prefix if no protocol specified
    if (!serverAddress.startsWith("http://") && !serverAddress.startsWith("https://")) {
      serverAddress = "https://" + serverAddress;
    }
    this.serverAddress = serverAddress;
    this.thresholdLatency = thresholdLatency;
  }

  public long monitorLatency() {
    long startTime = System.nanoTime();
    HttpURLConnection connection = null;

    try {
      URL url = new URL(serverAddress);
      if (serverAddress.startsWith("https://")) {
        connection = (HttpsURLConnection) url.openConnection();
      } else {
        connection = (HttpURLConnection) url.openConnection();
      }

      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      // Connect and get response code
      int responseCode = connection.getResponseCode();

      // Track data size
      totalSentData += connection.getRequestProperty("Content-Length") != null ?
          Integer.parseInt(connection.getRequestProperty("Content-Length")) : 0;
      totalReceivedData += connection.getContentLength() > 0 ? connection.getContentLength() : 0;

      long latency = (System.nanoTime() - startTime) / 1000000; // Convert to milliseconds

      if (responseCode == HttpURLConnection.HTTP_OK) {
        successfulRequests++;
        totalRequests++;
        System.out.println("Response Code: " + responseCode);
        System.out.println("Latency: " + latency + " ms");
        return latency;
      } else {
        packetLossCount++;
        totalRequests++;
        return -1;
      }

    } catch (IOException e) {
      System.out.println("Connection error: " + e.getMessage());
      packetLossCount++;
      totalRequests++;
      return -1;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public double getPacketLossPercentage() {
    if (totalRequests == 0) {
      return 0.0;
    }
    return ((double) packetLossCount / totalRequests) * 100;
  }

  public double getBandwidthUsage() {
    double totalDataSent = totalSentData / 1024.0;  // Convert bytes to KB
    double totalDataReceived = totalReceivedData / 1024.0;  // Convert bytes to KB
    return totalDataSent + totalDataReceived;  // Total bandwidth usage in KB
  }
}