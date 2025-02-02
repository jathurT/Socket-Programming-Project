package com.uor.eng;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NetworkMetrics {
  private double latency;
  private double packetLoss;
  private double throughput;
  private double bandwidthUsage;
  private double jitter;
  private double errorRate;
  private double connectionQuality;
  private boolean successful;
  private String errorMessage;
  private double downloadSpeed;
  private double uploadSpeed;
}