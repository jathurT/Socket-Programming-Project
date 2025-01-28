package com.uor.eng.service;

import com.uor.eng.model.Host;
import com.uor.eng.model.HostType;
import com.uor.eng.model.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MonitoringService {
  private final DatabaseService databaseService;
  private final ExecutorService executorService;
  private static final int TIMEOUT = 5000; // 5 seconds timeout

  public MonitoringService(DatabaseService databaseService) {
    this.databaseService = databaseService;
    this.executorService = Executors.newFixedThreadPool(10);
  }

  private final Set<Long> monitoredHostIds = ConcurrentHashMap.newKeySet();

  public void startMonitoring(Host host) {
    if (!monitoredHostIds.add(host.getId())) {
      return; // Host is already being monitored
    }
    executorService.submit(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        checkHost(host);
        try {
          TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      monitoredHostIds.remove(host.getId()); // Clean up on thread exit
    });
  }

  private void checkHost(Host host) {
    try {
      String oldStatus = host.getStatus();
      Double responseTime = null;

      if (host.getType() == HostType.ICMP) {
        responseTime = checkICMP(host);
      } else if (host.getType() == HostType.HTTPS) {
        responseTime = checkHTTPS(host);
      }

      Log log = new Log();
      log.setHostId(host.getId());
      log.setTime(LocalDateTime.now());
      log.setStatus(host.getStatus());
      log.setOldStatus(oldStatus);
      log.setResponseTime(responseTime);

      databaseService.addLog(log);
    } catch (Exception e) {
      log.error("Error checking host: {}", host.getName(), e);
    }
  }

  private Double checkICMP(Host host) throws IOException {
    InetAddress address = InetAddress.getByName(host.getHostname());
    long startTime = System.currentTimeMillis();
    boolean reachable = address.isReachable(TIMEOUT);
    long responseTime = System.currentTimeMillis() - startTime;

    host.setStatus(reachable ? "ONLINE" : "OFFLINE");
    return (double) responseTime;
  }

  private Double checkHTTPS(Host host) throws IOException {
    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(TIMEOUT)
        .setSocketTimeout(TIMEOUT)
        .build();

    try (CloseableHttpClient client = HttpClientBuilder.create()
        .setDefaultRequestConfig(config)
        .build()) {

      HttpGet request = new HttpGet(host.getHostname());
      long startTime = System.currentTimeMillis();
      int statusCode = client.execute(request).getStatusLine().getStatusCode();
      long responseTime = System.currentTimeMillis() - startTime;

      host.setStatus(statusCode == 200 ? "ONLINE" : "OFFLINE");
      return (double) responseTime;
    }
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