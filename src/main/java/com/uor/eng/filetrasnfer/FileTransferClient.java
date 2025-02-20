package com.uor.eng.filetrasnfer;

import java.io.*;
import java.net.*;

public class FileTransferClient {
    private static final int PORT = 5000;  // Server port
    private static final String SAVE_DIR = "C:/ReceivedFiles";  // Folder where files are saved

    public static void main(String[] args) {
        String serverIP = "192.168.232.72";  // Change this to match the server's IP

        try (Socket socket = new Socket(serverIP, PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            // Read file name and size from server
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            // Ensure directory exists
            File saveDirectory = new File(SAVE_DIR);
            if (!saveDirectory.exists()) {
                saveDirectory.mkdirs();
            }

            File receivedFile = new File(saveDirectory, fileName);
            try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                System.out.println("File received and saved at: " + receivedFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
        }
    }
}























/*
import java.io.*;
import java.net.*;

public class FileTransferClient {
    private static final String SERVER_IP = "192.168.232.72"; // Change to server IP
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            System.out.println("Connected to server...");

            // Receive file name and size
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            File file = new File("received_" + fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }

            System.out.println("File received: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

 */

