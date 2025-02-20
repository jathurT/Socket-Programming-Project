package com.uor.eng.filetrasnfer;

import java.io.*;
import java.net.*;
import java.util.*;

public class FileTransferServer {
    private static final int PORT = 5000;
    private static List<Socket> clientSockets = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");

            // Accept multiple clients in a separate thread
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        synchronized (clientSockets) {
                            clientSockets.add(clientSocket);
                        }
                        System.out.println("Client connected: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Wait for user input to send file
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter file path to send: ");
                String filePath = scanner.nextLine();
                File file = new File(filePath);

                if (file.exists()) {
                    sendFileToClients(file);
                } else {
                    System.out.println("File not found. Try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileToClients(File file) {
        synchronized (clientSockets) {
            for (Socket clientSocket : clientSockets) {
                new Thread(() -> sendFile(clientSocket, file)).start();
            }
        }
    }

    private static void sendFile(Socket clientSocket, File file) {
        try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            long startTime = System.nanoTime();

            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;
            int packetsSent = 0;
            List<Long> latencyList = new ArrayList<>();

            while ((bytesRead = fis.read(buffer)) != -1) {
                long latency = measureLatency(clientSocket);
                latencyList.add(latency);

                dos.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                packetsSent++;
            }

            long endTime = System.nanoTime();
            long transferTime = (endTime - startTime) / 1_000_000; // Convert to ms
            double throughput = calculateThroughput(totalBytesSent, transferTime);
            double jitter = calculateJitter(latencyList);
            double bandwidthUtilization = calculateBandwidthUtilization(totalBytesSent, transferTime, 100);
            double uploadSpeed = calculateUploadSpeed(totalBytesSent, transferTime);
            int packetsReceived = packetsSent; // Assuming all packets received for now
            double packetLoss = calculatePacketLoss(packetsSent, packetsReceived);
            long ping = measurePing(clientSocket.getInetAddress().getHostAddress());

            System.out.println("Metrics for Client " + clientSocket.getInetAddress());
            System.out.println("Ping: " + ping + " ms");
            System.out.println("Latency: " + latencyList.get(latencyList.size() - 1) + " ms");
            System.out.println("Throughput: " + throughput + " MB/s");
            System.out.println("Jitter: " + jitter + " ms");
            System.out.println("Packet Loss: " + packetLoss + " %");
            System.out.println("Bandwidth Utilization: " + bandwidthUtilization + " %");
            System.out.println("Upload Speed: " + uploadSpeed + " MB/s");

        } catch (IOException e) {
            System.out.println("Error sending file to client: " + e.getMessage());
        }
    }

    public static long measurePing(String clientIP) {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 " + clientIP); // Linux/Mac
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (line.contains("time=")) {
                    String time = line.substring(line.indexOf("time=") + 5).split(" ")[0];
                    return Long.parseLong(time.replace(".", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static long measureLatency(Socket socket) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            long startTime = System.nanoTime();
            dos.writeUTF("PING");
            dos.flush();
            dis.readUTF();
            long endTime = System.nanoTime();

            return (endTime - startTime) / 1_000_000;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static double calculateThroughput(long fileSize, long transferTime) {
        return (fileSize / (1024.0 * 1024)) / (transferTime / 1000.0);
    }

    public static double calculatePacketLoss(int packetsSent, int packetsReceived) {
        if (packetsSent == 0) return 0;
        return ((double) (packetsSent - packetsReceived) / packetsSent) * 100;
    }

    public static double calculateJitter(List<Long> latencies) {
        if (latencies.size() < 2) return 0;
        double sum = 0;
        for (int i = 1; i < latencies.size(); i++) {
            sum += Math.abs(latencies.get(i) - latencies.get(i - 1));
        }
        return sum / (latencies.size() - 1);
    }

    public static double calculateBandwidthUtilization(long fileSize, long transferTime, double availableBandwidth) {
        double fileSizeInBits = fileSize * 8;
        double bandwidthUsed = fileSizeInBits / (transferTime / 1000.0);
        return (bandwidthUsed / (availableBandwidth * 1_000_000)) * 100;
    }

    public static double calculateUploadSpeed(long fileSize, long transferTime) {
        return (fileSize / (1024.0 * 1024)) / (transferTime / 1000.0);
    }
}



