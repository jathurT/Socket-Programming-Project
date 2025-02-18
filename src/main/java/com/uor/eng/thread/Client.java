
package com.uor.eng.thread;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private static final int SERVER_PORT = 9876;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public Client(String serverIP) {
        try {
            socket = new Socket(serverIP, SERVER_PORT);
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            System.out.println("Connected to the server at: " + serverIP);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server: " + ex.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public String receiveMessage() {
        try {
            if (reader != null) {
                return reader.readLine();
            }
        } catch (IOException ex) {
            System.out.println("Error reading from server: " + ex.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientManagerUI::new);
    }
}

class ClientManagerUI extends JFrame {
    private JPanel clientPanel;

    public ClientManagerUI() {
        setTitle("Client Manager");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton addClientButton = new JButton("New Client");
        addClientButton.addActionListener(e -> createNewClient());

        clientPanel = new JPanel();
        clientPanel.setLayout(new BoxLayout(clientPanel, BoxLayout.Y_AXIS));

        add(addClientButton, BorderLayout.NORTH);
        add(new JScrollPane(clientPanel), BorderLayout.CENTER);

        setVisible(true);
    }

    private void createNewClient() {
        String serverIP = JOptionPane.showInputDialog(this, "Enter Server IP Address:",
                "Server Connection", JOptionPane.QUESTION_MESSAGE);

        if (serverIP != null && !serverIP.isEmpty()) {
            Client client = new Client(serverIP);
            new ClientUI(client);
        }
    }
}

class ClientUI extends JFrame {
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
            long latency = (endTime - startTime) / 1_000_000; // in milliseconds
            chatArea.append("You: " + message + " (Latency: " + latency + " ms)\n");

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
