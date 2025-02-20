package com.uor.eng.thread;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class MultiThreadedServer extends JFrame {
    private static final int PORT = 9876;
    private JTextArea logArea;
    private static final Set<PrintWriter> clientWriters = new HashSet<>();
    private TimeSeries clientCountSeries;
    private int connectedClients = 0;

    public MultiThreadedServer() {
        setTitle("MultiThreaded Server");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JButton startButton = new JButton("Start Server");
        startButton.addActionListener(e -> startServer());

        clientCountSeries = new TimeSeries("Connected Clients");
        TimeSeriesCollection dataset = new TimeSeriesCollection(clientCountSeries);
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Client Connections Over Time", "Time", "Clients", dataset, false, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(580, 250));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(scrollPane, BorderLayout.CENTER);
        topPanel.add(chartPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        setVisible(true);

        startServer();
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                logMessage("Server is listening on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    logMessage("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                    updateClientCount(1);

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                }
            } catch (IOException ex) {
                logMessage("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public synchronized void updateClientCount(int change) {
        connectedClients += change;
        SwingUtilities.invokeLater(() -> clientCountSeries.addOrUpdate(new Second(), connectedClients));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiThreadedServer::new);
    }
}


