package com.uor.eng.ui;

import com.uor.eng.thread.ClientUI;
import com.uor.eng.thread.MultiThreadedServer;
import com.uor.eng.thread.Client;
import com.uor.eng.Main;
import com.uor.eng.filetransfer.FileTransferSwing;
import com.uor.eng.filetransfer.FileTransferServer;
import com.uor.eng.filetransfer.FileTransferClient;

//new part
import com.uor.eng.filetransfervisual.FileTransferServerSingle;
import com.uor.eng.filetransfervisual.FileTransferApp;

import javax.swing.*;
import java.awt.*;

public class MainUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainUI::createUI);
    }

    public static void createUI() {
        JFrame frame = new JFrame("Network Monitoring Tool");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(240, 240, 240));

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Device Monitoring Panel (Block for server & client buttons)
        JPanel deviceMonitoringPanel = new JPanel();
        deviceMonitoringPanel.setLayout(new GridLayout(2, 1, 5, 5));
        deviceMonitoringPanel.setBorder(BorderFactory.createTitledBorder("Device Monitoring When Messaging"));
        deviceMonitoringPanel.setBackground(Color.WHITE);

        JButton serverButton = new JButton("Start Server");
        serverButton.setFont(new Font("Arial", Font.BOLD, 14));
        serverButton.setFocusPainted(false);
        serverButton.addActionListener(e -> {
            new Thread(() -> {
                MultiThreadedServer server = new MultiThreadedServer();
                server.startServer();
            }).start();
            JOptionPane.showMessageDialog(frame, "Server Started!");
        });

        JButton clientButton = new JButton("Start Client");
        clientButton.setFont(new Font("Arial", Font.BOLD, 14));
        clientButton.setBackground(Color.WHITE);
        clientButton.setFocusPainted(false);
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

        deviceMonitoringPanel.add(serverButton);
        deviceMonitoringPanel.add(clientButton);

        // Public IP Monitoring Panel (Block for public IP monitoring button)
        JPanel publicIPPanel = new JPanel();
        publicIPPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        publicIPPanel.setBorder(BorderFactory.createTitledBorder("Public IP Monitoring"));
        publicIPPanel.setBackground(Color.WHITE);

        JButton publicIPButton = new JButton("Public IP Monitoring");
        publicIPButton.setFont(new Font("Arial", Font.BOLD, 14));
        publicIPButton.setBackground(new Color(50, 150, 250));
        publicIPButton.setForeground(Color.BLACK);
        publicIPButton.setFocusPainted(false);
        publicIPButton.addActionListener(e -> {
            new Thread(() -> Main.main(new String[]{})).start();
            JOptionPane.showMessageDialog(frame, "Public IP Monitoring Started!");
        });

        publicIPPanel.add(publicIPButton);
/*
        JPanel fileTransferPanel = new JPanel();
        fileTransferPanel.setLayout(new GridLayout(3, 1, 5, 5));
        fileTransferPanel.setBorder(BorderFactory.createTitledBorder("Device Monitoring When File Transfer"));
        fileTransferPanel.setBackground(Color.WHITE);

        JButton fileTransferSwingButton = new JButton("Open File Transfer UI");
        fileTransferSwingButton.setFont(new Font("Arial", Font.BOLD, 14));
        fileTransferSwingButton.setFocusPainted(false);
        fileTransferSwingButton.addActionListener(e -> new Thread(() -> new FileTransferSwing().setVisible(true)).start());

        JButton fileTransferServerButton = new JButton("Start File Transfer Server");
        fileTransferServerButton.setFont(new Font("Arial", Font.BOLD, 14));
        fileTransferServerButton.setFocusPainted(false);
        fileTransferServerButton.addActionListener(e -> new Thread(() -> FileTransferServer.main(new String[]{})).start());

        JButton fileTransferClientButton = new JButton("Start File Transfer Client");
        fileTransferClientButton.setFont(new Font("Arial", Font.BOLD, 14));
        fileTransferClientButton.setFocusPainted(false);
        fileTransferClientButton.addActionListener(e -> new Thread(() -> FileTransferClient.main(new String[]{})).start());

        fileTransferPanel.add(fileTransferSwingButton);
        fileTransferPanel.add(fileTransferServerButton);
        fileTransferPanel.add(fileTransferClientButton);
*/
        //Venujan part
        JPanel venujanPanel = new JPanel();
        venujanPanel.setLayout(new GridLayout(2, 1, 5, 5));
        venujanPanel.setBorder(BorderFactory.createTitledBorder("Device Monitoring when File Transfer"));
        venujanPanel.setBackground(Color.WHITE);

        // venujan newly added file_transfer visual
        JButton VenujanButton1 = new JButton("Start File Transfer Client");
        VenujanButton1.setFont(new Font("Arial", Font.BOLD, 14));
        VenujanButton1.setFocusPainted(false);
        VenujanButton1.addActionListener(e -> new Thread(() -> FileTransferApp.main(new String[]{})).start());

        // venujan newly added file_transfer visual
        JButton venujanButton2 = new JButton("Start File Transfer server");
        venujanButton2.setFont(new Font("Arial", Font.BOLD, 14));
        venujanButton2.setFocusPainted(false);
        venujanButton2.addActionListener(e -> new Thread(() -> FileTransferServerSingle.main(new String[]{})).start());

        venujanPanel.add(VenujanButton1);
        venujanPanel.add(venujanButton2);
        //venujan



        mainPanel.add(deviceMonitoringPanel);
        mainPanel.add(publicIPPanel);
        //mainPanel.add(fileTransferPanel);
        mainPanel.add(venujanPanel);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
