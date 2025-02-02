package com.uor.eng;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class MonitoringView {
  private final MetricLabels metricLabels;
  private final XYChart.Series<Number, Number> latencySeries;
  private final XYChart.Series<Number, Number> throughputSeries;
  private final XYChart.Series<Number, Number> qualitySeries;
  private final VBox container;

  public MonitoringView(String siteAddress) {
    this.metricLabels = new MetricLabels();
    this.latencySeries = new XYChart.Series<>();
    this.throughputSeries = new XYChart.Series<>();
    this.qualitySeries = new XYChart.Series<>();
    this.container = createLayout(siteAddress);
  }

  private VBox createLayout(String siteAddress) {
    GridPane metricsGrid = createMetricsGrid();
    LineChart<Number, Number> chart = createChart();
    return new VBox(10, metricsGrid, chart);
  }

  private GridPane createMetricsGrid() {
    GridPane grid = new GridPane();
    grid.setVgap(5);
    grid.setHgap(10);
    grid.add(metricLabels.latencyLabel, 0, 0);
    grid.add(metricLabels.packetLossLabel, 1, 0);
    grid.add(metricLabels.throughputLabel, 0, 1);
    grid.add(metricLabels.jitterLabel, 1, 1);
    grid.add(metricLabels.qualityLabel, 0, 2);
    grid.add(metricLabels.errorRateLabel, 1, 2);
    grid.add(metricLabels.downloadSpeedLabel, 0, 3);
    grid.add(metricLabels.uploadSpeedLabel, 1, 3);
    grid.add(metricLabels.connectionStatusLabel, 0, 4, 2, 1);
    return grid;
  }

  private LineChart<Number, Number> createChart() {
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.getData().addAll(latencySeries, throughputSeries, qualitySeries);
    return chart;
  }

  public void updateMetrics(NetworkMetrics metrics, int sampleCount) {
    updateLabels(metrics);
    updateCharts(metrics, sampleCount);
  }

  private void updateLabels(NetworkMetrics metrics) {
    metricLabels.updateFromMetrics(metrics);
  }

  private void updateCharts(NetworkMetrics metrics, int sampleCount) {
    latencySeries.getData().add(new XYChart.Data<>(sampleCount, metrics.getLatency()));
    throughputSeries.getData().add(new XYChart.Data<>(sampleCount, metrics.getThroughput() / 1024));
    qualitySeries.getData().add(new XYChart.Data<>(sampleCount, metrics.getConnectionQuality()));
  }

  public void showError(String message) {
    metricLabels.latencyLabel.setText("Error: " + message);
  }

  public void showStopped() {
    metricLabels.setAllStopped();
  }

  public VBox getContainer() {
    return container;
  }
}
