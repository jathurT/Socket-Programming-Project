package com.uor.eng.ui;

import com.uor.eng.thread.ClientUI;
import com.uor.eng.thread.MultiThreadedServer;
import com.uor.eng.thread.Client;

import javax.swing.*;

public class MainUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainUI::createUI);
    }

    public static void createUI() {
        JFrame frame = new JFrame("Network Monitoring Tool");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null); // Absolute positioning

        // Button to Start MultiThreadedServer
        JButton serverButton = new JButton("Start Server");
        serverButton.setBounds(100, 50, 200, 40);
        serverButton.addActionListener(e -> {
            new Thread(() -> {
                MultiThreadedServer server = new MultiThreadedServer();
                server.startServer();
            }).start();
            JOptionPane.showMessageDialog(frame, "Server Started!");
        });

        // Button to Start Client
        JButton clientButton = new JButton("Start Client");
        clientButton.setBounds(100, 100, 200, 40);
        clientButton.addActionListener(e -> {
            String serverIP = JOptionPane.showInputDialog(frame, "Enter Server IP Address:", "Server Connection", JOptionPane.QUESTION_MESSAGE);
            if (serverIP != null && !serverIP.isEmpty()) {
                new Thread(() -> {
                    Client client = new Client(serverIP);
                    new ClientUI(client);
                }).start();
                JOptionPane.showMessageDialog(frame, "Client Started!");
            }
        });

        // Button to Start Both Server & Client
        JButton mainButton = new JButton("Start Main (Both)");
        mainButton.setBounds(100, 150, 200, 40);
        mainButton.addActionListener(e -> {
            new Thread(() -> {
                MultiThreadedServer server = new MultiThreadedServer();
                server.startServer();
                Client client = new Client("127.0.0.1");
                new ClientUI(client);
            }).start();
            JOptionPane.showMessageDialog(frame, "Server & Client Started!");
        });

        // Adding buttons to the frame
        frame.add(serverButton);
        frame.add(clientButton);
        frame.add(mainButton);

        frame.setVisible(true);
    }
}
