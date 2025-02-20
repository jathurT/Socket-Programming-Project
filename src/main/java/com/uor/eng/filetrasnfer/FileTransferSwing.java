package com.uor.eng.filetrasnfer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Vector;

public class FileTransferSwing extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField clientIPField;
    private File selectedFile;

    public FileTransferSwing() {
        setTitle("Multi-Client File Transfer");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table Setup
        String[] columnNames = {"Client IP", "File Name", "Status", "Ping (ms)", "Latency (ms)",
                "Throughput (MB/s)", "Packet Loss (%)", "Jitter (ms)", "Bandwidth (%)", "Upload Speed (MB/s)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Input & Buttons
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        clientIPField = new JTextField(15);
        JButton addClientBtn = new JButton("Add Client");
        JButton browseFileBtn = new JButton("Browse File");
        JButton startTransferBtn = new JButton("Start Transfer");
        JButton removeRowBtn = new JButton("Remove Selected");

        inputPanel.add(new JLabel("Client IP: "));
        inputPanel.add(clientIPField);
        inputPanel.add(addClientBtn);
        inputPanel.add(browseFileBtn);
        inputPanel.add(startTransferBtn);
        inputPanel.add(removeRowBtn);

        add(inputPanel, BorderLayout.SOUTH);

        // Button Actions
        addClientBtn.addActionListener(this::addClient);
        browseFileBtn.addActionListener(this::selectFile);
        startTransferBtn.addActionListener(this::startFileTransfer);
        removeRowBtn.addActionListener(this::removeSelectedRow);
    }

    private void addClient(ActionEvent e) {
        String clientIP = clientIPField.getText().trim();
        if (!clientIP.isEmpty()) {
            tableModel.addRow(new Object[]{clientIP, "", "Pending", 0, 0, 0, 0, 0, 0, 0});
            clientIPField.setText("");
        }
    }

    private void selectFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            int rowCount = tableModel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                tableModel.setValueAt(selectedFile.getName(), i, 1);
            }
        }
    }

    private void startFileTransfer(ActionEvent e) {
        if (selectedFile == null || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add clients and select a file before starting the transfer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            final int rowIndex = i;
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Simulated transfer delay
                    tableModel.setValueAt("Success", rowIndex, 2);
                    tableModel.setValueAt((long) (Math.random() * 100), rowIndex, 3); // Ping
                    tableModel.setValueAt((long) (Math.random() * 50), rowIndex, 4); // Latency
                    tableModel.setValueAt(Math.random() * 10, rowIndex, 5); // Throughput
                    tableModel.setValueAt(Math.random() * 5, rowIndex, 6); // Packet Loss
                    tableModel.setValueAt(Math.random() * 10, rowIndex, 7); // Jitter
                    tableModel.setValueAt(Math.random() * 100, rowIndex, 8); // Bandwidth
                    tableModel.setValueAt(Math.random() * 50, rowIndex, 9); // Upload Speed
                } catch (InterruptedException ignored) {
                    tableModel.setValueAt("Failed", rowIndex, 2);
                }
            }).start();
        }
    }

    private void removeSelectedRow(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileTransferSwing().setVisible(true));
    }
}
