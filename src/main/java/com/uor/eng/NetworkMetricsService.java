package com.uor.eng;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class NetworkMetricsService {
  private final long thresholdLatency;
  private final BlockingQueue<Long> latencyHistory;
  private final ExecutorService executorService;

  private static final int HISTORY_SIZE = 10;
  private static final int BUFFER_SIZE = 8192;
  private static final int TIMEOUT_MS = 5000;
  private static final int PING_COUNT = 10;
  private static final int CONCURRENT_THREADS = 4;

  public NetworkMetricsService(long thresholdLatency) {
    this.thresholdLatency = thresholdLatency;
    this.latencyHistory = new ArrayBlockingQueue<>(HISTORY_SIZE);
    this.executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
  }

  public NetworkMetrics measureMetrics(String address) {
    String normalizedAddress = normalizeAddress(address);
    long startTime = System.nanoTime();

    try {
      URL url = new URL(normalizedAddress);

      // Parallel execution of measurements
      CompletableFuture<Double> dnsFuture = CompletableFuture.supplyAsync(
          () -> measureDNSTime(url.getHost()), executorService);

      CompletableFuture<ConnectionTiming> connectionFuture = CompletableFuture.supplyAsync(
          () -> measureConnectionTime(url), executorService);

      CompletableFuture<MetricsResult> httpFuture = CompletableFuture.supplyAsync(
          () -> measureHttpMetrics(url), executorService);

      // Wait for all measurements to complete
      double dnsTime = dnsFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
      ConnectionTiming connTiming = connectionFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
      MetricsResult metricsResult = httpFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

      double latency = (System.nanoTime() - startTime) / 1_000_000.0;
      updateLatencyHistory((long) latency);

      if (latency > thresholdLatency) {
        AlertService.showLatencyAlert(address, latency);
      }

      double jitter = calculateJitter();
      double mos = calculateMOS(latency, jitter, metricsResult.packetLoss);

      return NetworkMetrics.success(
          latency,
          dnsTime,
          connTiming.tcpTime,
          connTiming.tlsTime,
          metricsResult.timeToFirstByte,
          metricsResult.packetLoss,
          metricsResult.throughput,
          jitter,
          metricsResult.quality,
          mos,
          metricsResult.errorRate,
          metricsResult.downloadSpeed,
          metricsResult.uploadSpeed,
          metricsResult.minPing,
          metricsResult.avgPing,
          metricsResult.maxPing
      );

    } catch (Exception e) {
      AlertService.showError("Connection Error", "Failed to measure metrics: " + e.getMessage());
      return NetworkMetrics.error(e.getMessage());
    }
  }

  private static class ConnectionTiming {
    double tcpTime;
    double tlsTime;
  }

  private static class MetricsResult {
    double timeToFirstByte;
    double packetLoss;
    double throughput;
    double quality;
    double errorRate;
    double downloadSpeed;
    double uploadSpeed;
    double minPing;
    double avgPing;
    double maxPing;
  }

  private double measureDNSTime(String hostname) {
    long dnsStart = System.nanoTime();
    try {
      InetAddress address = InetAddress.getByName(hostname);
      if (address == null) {
        throw new UnknownHostException("DNS resolution failed for " + hostname);
      }
      return Math.max((System.nanoTime() - dnsStart) / 1_000_000.0, 1.0);
    } catch (Exception e) {
      return 0.0;
    }
  }

  private ConnectionTiming measureConnectionTime(URL url) {
    ConnectionTiming timing = new ConnectionTiming();
    int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();

    try (Socket socket = new Socket()) {
      socket.setTcpNoDelay(true);
      socket.setPerformancePreferences(0, 1, 0);

      long tcpStart = System.nanoTime();
      socket.connect(new InetSocketAddress(url.getHost(), port), TIMEOUT_MS);
      timing.tcpTime = (System.nanoTime() - tcpStart) / 1_000_000.0;

      if (url.getProtocol().equalsIgnoreCase("https")) {
        timing.tlsTime = measureTLSHandshake(socket, url.getHost(), port);
      }
    } catch (Exception e) {
      timing.tcpTime = 0;
      timing.tlsTime = 0;
    }
    return timing;
  }

  private double measureTLSHandshake(Socket socket, String host, int port) {
    long tlsStart = System.nanoTime();
    try {
      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      try (SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, host, port, true)) {
        sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        sslSocket.startHandshake();
        return (System.nanoTime() - tlsStart) / 1_000_000.0;
      }
    } catch (Exception e) {
      return 0;
    }
  }

  private MetricsResult measureHttpMetrics(URL url) {
    MetricsResult result = new MetricsResult();
    HttpURLConnection conn = null;

    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(TIMEOUT_MS);
      conn.setReadTimeout(TIMEOUT_MS);
      conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

      long ttfbStart = System.nanoTime();
      conn.connect();
      try (InputStream in = new BufferedInputStream(getInputStream(conn), BUFFER_SIZE)) {
        byte[] buffer = new byte[BUFFER_SIZE];
        long bytesRead = 0;
        int errors = 0;
        long startRead = System.nanoTime();
        boolean firstByte = true;

        int read;
        while ((read = in.read(buffer)) != -1) {
          if (firstByte) {
            result.timeToFirstByte = (System.nanoTime() - ttfbStart) / 1_000_000.0;
            firstByte = false;
          }

          bytesRead += read;
          if (read < buffer.length) {
            errors++;
          }
        }

        calculateMetrics(result, bytesRead, errors, buffer.length, startRead);
        measurePingStatistics(result, url.getHost());
      }
    } catch (Exception e) {
      result.quality = 0;
      result.errorRate = 100;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }

    return result;
  }

  private InputStream getInputStream(HttpURLConnection conn) throws IOException {
    String encoding = conn.getContentEncoding();
    InputStream inputStream = conn.getInputStream();

    if ("gzip".equalsIgnoreCase(encoding)) {
      return new GZIPInputStream(inputStream);
    } else if ("deflate".equalsIgnoreCase(encoding)) {
      return new InflaterInputStream(inputStream);
    }
    return inputStream;
  }

  private void calculateMetrics(MetricsResult result, long bytesRead, int errors,
                                int bufferSize, long startRead) {
    double duration = (System.nanoTime() - startRead) / 1_000_000_000.0;
    result.throughput = duration > 0 ? bytesRead / duration : 0;
    result.downloadSpeed = (result.throughput * 8) / (1024 * 1024); // Mbps
    result.uploadSpeed = result.downloadSpeed * 0.3; // Estimate upload as 30% of download
    result.errorRate = calculateErrorRate(bytesRead, errors, bufferSize);
    result.quality = calculateQuality(result);
  }

  private double calculateErrorRate(long bytesRead, int errors, int bufferSize) {
    return bytesRead > 0 ? (errors * bufferSize * 100.0) / bytesRead : 100.0;
  }

  private void measurePingStatistics(MetricsResult result, String host) {
    List<PingResult> pings = new ArrayList<>();
    int successfulPings = 0;
    int totalAttempts = PING_COUNT;
    boolean anyPingSucceeded = false;

    for (int i = 0; i < totalAttempts; i++) {
      try {
        long start = System.nanoTime();
        boolean reachable = InetAddress.getByName(host).isReachable(1000);
        double pingTime = (System.nanoTime() - start) / 1_000_000.0;

        if (reachable) {
          anyPingSucceeded = true;
          successfulPings++;
          pings.add(new PingResult(true, pingTime));
          System.out.println("Ping " + i + ": " + pingTime + " ms");
        } else {
          pings.add(new PingResult(false, 0));
          System.out.println("Ping " + i + ": Failed");
        }

        // Only sleep between attempts if we haven't detected total failure
        if (i < totalAttempts - 1 && anyPingSucceeded) {
          Thread.sleep(50);
        }

      } catch (Exception e) {
        System.err.println("Ping " + i + " error: " + e.getMessage());
        pings.add(new PingResult(false, 0));
      }

      // If we've tried 3 times and none succeeded, assume total connection loss
      if (i >= 2 && !anyPingSucceeded) {
        result.packetLoss = 100.0;
        result.minPing = 0.0;
        result.maxPing = 0.0;
        result.avgPing = 0.0;
        return; // Exit early - no point continuing
      }
    }

    // Calculate final statistics
    result.packetLoss = ((totalAttempts - successfulPings) * 100.0) / totalAttempts;
    System.out.println("Packet loss: " + result.packetLoss + "%");

    if (successfulPings > 0) {
      List<Double> successfulPingTimes = pings.stream()
          .filter(p -> p.successful)
          .map(p -> p.time)
          .toList();

      result.minPing = successfulPingTimes.stream().mapToDouble(d -> d).min().getAsDouble();
      result.maxPing = successfulPingTimes.stream().mapToDouble(d -> d).max().getAsDouble();
      result.avgPing = successfulPingTimes.stream().mapToDouble(d -> d).average().getAsDouble();
    } else {
      result.minPing = 0;
      result.maxPing = 0;
      result.avgPing = 0;
    }
  }

  private static class PingResult {
    final boolean successful;
    final double time;

    PingResult(boolean successful, double time) {
      this.successful = successful;
      this.time = time;
    }
  }

  private double calculateQuality(MetricsResult metrics) {
    double speedScore = Math.min(100, metrics.downloadSpeed * 10);
    double pingScore = Math.max(0, 100 - (metrics.avgPing / 10));
    double lossScore = Math.max(0, 100 - (metrics.packetLoss * 10));
    double throughputScore = Math.min(100, (metrics.throughput / (1024 * 1024)) * 20);

    return (speedScore + pingScore + lossScore + throughputScore) / 4.0;
  }

  private double calculateMOS(double latency, double jitter, double packetLoss) {
    double R0 = 93.2;
    double Is = jitter * 0.1;
    double Id = 0.024 * latency + 0.11 * (latency - 177.3) * (latency > 177.3 ? 1 : 0);
    double Ie = 30 * (packetLoss / 100);

    double R = R0 - Is - Id - Ie;
    if (R < 0) return 1.0;
    if (R > 100) return 4.5;

    double mos = 1 + 0.035 * R + R * (R - 60) * (100 - R) * 7e-6;
    return Math.min(5.0, Math.max(1.0, mos));
  }

  private String normalizeAddress(String address) {
    if (!address.startsWith("http://") && !address.startsWith("https://")) {
      return "https://" + address;
    }
    return address;
  }

  private void updateLatencyHistory(long latency) {
    latencyHistory.offer(latency);
    while (HISTORY_SIZE < latencyHistory.size()) {
      latencyHistory.poll();
    }
  }

  private double calculateJitter() {
    if (latencyHistory.size() < 2) return 0.0;

    double sum = 0.0;
    Long previous = null;
    for (Long current : latencyHistory) {
      if (previous != null) {
        sum += Math.abs(current - previous);
      }
      previous = current;
    }
    return sum / (latencyHistory.size() - 1);
  }

  public void shutdown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}