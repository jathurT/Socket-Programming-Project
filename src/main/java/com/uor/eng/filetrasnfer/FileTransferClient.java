package com.uor.eng.filetrasnfer;


import java.io.*;
import java.net.*;

public class FileTransferClient {
    private static final String SERVER_IP = "127.0.0.1"; // Change to server IP
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

