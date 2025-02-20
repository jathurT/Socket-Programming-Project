package com.uor.eng.filetrasnfer;

import javax.swing.*;

public class ClientData {
    private String clientIP;
    private String fileName;
    private String status;
    private long ping;
    private long latency;
    private double throughput;
    private double packetLoss;
    private double jitter;
    private double bandwidthUtilization;
    private double uploadSpeed;

    public ClientData(String clientIP, String fileName, String status, long ping, long latency, double throughput,
                      double packetLoss, double jitter, double bandwidthUtilization, double uploadSpeed) {
        this.clientIP = clientIP;
        this.fileName = fileName;
        this.status = status;
        this.ping = ping;
        this.latency = latency;
        this.throughput = throughput;
        this.packetLoss = packetLoss;
        this.jitter = jitter;
        this.bandwidthUtilization = bandwidthUtilization;
        this.uploadSpeed = uploadSpeed;
    }

    // Getters and Setters
    public String getClientIP() { return clientIP; }
    public void setClientIP(String clientIP) { this.clientIP = clientIP; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getPing() { return ping; }
    public void setPing(long ping) { this.ping = ping; }

    public long getLatency() { return latency; }
    public void setLatency(long latency) { this.latency = latency; }

    public double getThroughput() { return throughput; }
    public void setThroughput(double throughput) { this.throughput = throughput; }

    public double getPacketLoss() { return packetLoss; }
    public void setPacketLoss(double packetLoss) { this.packetLoss = packetLoss; }

    public double getJitter() { return jitter; }
    public void setJitter(double jitter) { this.jitter = jitter; }

    public double getBandwidthUtilization() { return bandwidthUtilization; }
    public void setBandwidthUtilization(double bandwidthUtilization) { this.bandwidthUtilization = bandwidthUtilization; }

    public double getUploadSpeed() { return uploadSpeed; }
    public void setUploadSpeed(double uploadSpeed) { this.uploadSpeed = uploadSpeed; }
}
