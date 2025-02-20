package com.uor.eng.filetransfervisual;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServerSingle {
    private static final int PORT = 12347;
    private static final String SAVE_DIR = "received_files/"; // Directory to save received files

    public static void main(String[] args) {
        // Create directory if it doesn't exist
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\nClient connected: " + socket.getInetAddress());

                // Handle file reception in a new thread
                new Thread(() -> receiveFile(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    private static void receiveFile(Socket socket) {
        long connectionTime = System.nanoTime(); // Time when client first connects
        long firstByteTime = 0;

        try (InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             OutputStream outputStream = socket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            // Send ACK to Client (To ensure actual network delay is measured)
            dataOutputStream.writeUTF("ACK");
            dataOutputStream.flush();

            // Wait for client's file transfer start
            String fileName = dataInputStream.readUTF();
            firstByteTime = System.nanoTime(); // Time when first byte of file data is received

            File file = new File(SAVE_DIR + fileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesReceived = 0;

                System.out.println("Receiving file: " + fileName);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    totalBytesReceived += bytesRead;
                }

                long endTime = System.nanoTime(); // End time after full file reception

                // Calculate Latency (Time from ACK sent to first byte arrival)
                double latencyMs = (firstByteTime - connectionTime) / 1_000_000.0; // Convert to milliseconds

                // Calculate Transfer Time
                double transferTime = (endTime - firstByteTime) / 1_000_000_000.0; // Convert to seconds

                // Calculate Throughput (Mbps)
                double throughput = (totalBytesReceived * 8) / (transferTime * 1_000_000.0); // Mbps

                System.out.println("\nFile received successfully: " + file.getAbsolutePath());
                System.out.println("Total Bytes Received: " + totalBytesReceived + " bytes");
                System.out.println("Latency: " + String.format("%.2f ms", latencyMs));
                System.out.println("Transfer Time: " + String.format("%.2f seconds", transferTime));
                System.out.println("Throughput: " + String.format("%.2f Mbps", throughput));

            }

        } catch (IOException e) {
            System.err.println("Error receiving file: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}


