package com.uor.eng.thread;

import javax.swing.*;
import java.awt.*;

public class ClientUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private Client client;
    private Thread listenerThread;  // Thread to listen to messages

    public ClientUI(Client client) {
        this.client = client;

        setTitle("Client Chat");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Start a dedicated thread for listening to messages
        listenerThread = new Thread(this::listenForMessages);
        listenerThread.start();

        setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            long startTime = System.nanoTime();
            client.sendMessage(message);

            if ("exit".equalsIgnoreCase(message)) {
                closeClient();
            }

            long endTime = System.nanoTime();
            long latency = (endTime - startTime) / 1_000; // in milliseconds
            chatArea.append("You: " + message + " (Latency: " + latency + " µs)\n");

            inputField.setText("");
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = client.receiveMessage()) != null) {
                // Ensuring updates happen on the Swing UI thread
                String finalMessage = message;
                SwingUtilities.invokeLater(() -> chatArea.append("Server: " + finalMessage + "\n"));
            }
        } catch (Exception ex) {
            System.out.println("Error in listener thread: " + ex.getMessage());
        }
    }

    private void closeClient() {
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
            dispose(); // Close the UI window
        } catch (Exception ex) {
            System.out.println("Error closing client: " + ex.getMessage());
        }
    }
}
