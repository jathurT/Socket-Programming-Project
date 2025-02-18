package com.uor.eng.thread;

import java.io.*;
import java.net.Socket;

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private MultiThreadedServer server;

    public ClientHandler(Socket socket, MultiThreadedServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            String clientAddress = socket.getInetAddress().getHostAddress();
            server.logMessage("Client connected: " + clientAddress);

            String text;
            while ((text = reader.readLine()) != null) {
                server.logMessage("Received from " + clientAddress + ": " + text);

                // Echo the message back to the client
                writer.println("Server: " + text);

                // Exit if the client sends "exit"
                if ("exit".equalsIgnoreCase(text)) {
                    server.logMessage("Client " + clientAddress + " disconnected.");
                    break;
                }
            }
        } catch (IOException ex) {
            server.logMessage("Error handling client: " + ex.getMessage());
        }
    }
}

