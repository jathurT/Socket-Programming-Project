package com.uor.eng.thread;

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

    public MultiThreadedServer() {
        // Setup UI
        setTitle("MultiThreaded Server");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        add(scrollPane, BorderLayout.CENTER);

        JButton startButton = new JButton("Start Server");
        startButton.addActionListener(e -> startServer());

        add(startButton, BorderLayout.SOUTH);

        setVisible(true);
    }


    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                logMessage("Server is listening on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    logMessage("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Start a new thread for this client
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

//    public static void main(String[] args) {
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("Server is listening on port " + PORT);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
//
//                // Start a new thread for this client
//                ClientHandler clientHandler = new ClientHandler(clientSocket);
//                Thread clientThread = new Thread(clientHandler);
//                clientThread.start();
//
//            }
//        } catch (IOException ex) {
//            System.out.println("Server exception: " + ex.getMessage());
//            ex.printStackTrace();
//        }
//    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiThreadedServer::new);
    }

}

