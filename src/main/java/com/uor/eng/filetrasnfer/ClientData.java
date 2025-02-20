package com.uor.eng.filetrasnfer;

import javafx.beans.property.*;

public class ClientData {
    private final StringProperty clientIP;
    private final StringProperty fileName;
    private final StringProperty status;
    private final LongProperty ping;
    private final LongProperty latency;
    private final DoubleProperty throughput;
    private final DoubleProperty packetLoss;
    private final DoubleProperty jitter;
    private final DoubleProperty bandwidthUtilization;
    private final DoubleProperty uploadSpeed;

    public ClientData(String clientIP, String fileName, String status, long ping, long latency, double throughput,
                      double packetLoss, double jitter, double bandwidthUtilization, double uploadSpeed) {
        this.clientIP = new SimpleStringProperty(clientIP);
        this.fileName = new SimpleStringProperty(fileName);
        this.status = new SimpleStringProperty(status);
        this.ping = new SimpleLongProperty(ping);
        this.latency = new SimpleLongProperty(latency);
        this.throughput = new SimpleDoubleProperty(throughput);
        this.packetLoss = new SimpleDoubleProperty(packetLoss);
        this.jitter = new SimpleDoubleProperty(jitter);
        this.bandwidthUtilization = new SimpleDoubleProperty(bandwidthUtilization);
        this.uploadSpeed = new SimpleDoubleProperty(uploadSpeed);
    }

    // Getters and Setters
    public String getClientIP() { return clientIP.get(); }
    public void setClientIP(String value) { clientIP.set(value); }
    public StringProperty clientIPProperty() { return clientIP; }

    public String getFileName() { return fileName.get(); }
    public void setFileName(String value) { fileName.set(value); }
    public StringProperty fileNameProperty() { return fileName; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public long getPing() { return ping.get(); }
    public void setPing(long value) { ping.set(value); }
    public LongProperty pingProperty() { return ping; }

    public long getLatency() { return latency.get(); }
    public void setLatency(long value) { latency.set(value); }
    public LongProperty latencyProperty() { return latency; }

    public double getThroughput() { return throughput.get(); }
    public void setThroughput(double value) { throughput.set(value); }
    public DoubleProperty throughputProperty() { return throughput; }

    public double getPacketLoss() { return packetLoss.get(); }
    public void setPacketLoss(double value) { packetLoss.set(value); }
    public DoubleProperty packetLossProperty() { return packetLoss; }

    public double getJitter() { return jitter.get(); }
    public void setJitter(double value) { jitter.set(value); }
    public DoubleProperty jitterProperty() { return jitter; }

    public double getBandwidthUtilization() { return bandwidthUtilization.get(); }
    public void setBandwidthUtilization(double value) { bandwidthUtilization.set(value); }
    public DoubleProperty bandwidthUtilizationProperty() { return bandwidthUtilization; }

    public double getUploadSpeed() { return uploadSpeed.get(); }
    public void setUploadSpeed(double value) { uploadSpeed.set(value); }
    public DoubleProperty uploadSpeedProperty() { return uploadSpeed; }
}

