

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

