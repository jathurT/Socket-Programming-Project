package com.uor.eng.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NetworkScanner {
    public static void main(String[] args) {
        try {
            // Execute the 'arp -a' command
            Process process = Runtime.getRuntime().exec("arp -a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            System.out.println("Actively Connected Devices (IP Addresses):");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".")) { // Filter lines containing IPs
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3 && !parts[2].equalsIgnoreCase("static")) {
                        // Only display devices that are dynamically connected
                        System.out.println("IP Address: " + parts[0] + " - MAC: " + parts[1]);
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
