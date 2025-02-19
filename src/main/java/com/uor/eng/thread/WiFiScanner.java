package com.uor.eng.thread;

import java.io.IOException;
import java.net.InetAddress;

public class WiFiScanner {
    public static void main(String[] args) {
        String subnet = "192.168.43."; // Adjust based on your network (Check `ipconfig` or `ifconfig`)
        System.out.println("Scanning for devices on " + subnet + "0/24...");

        for (int i = 1; i < 255; i++) { // Scan 192.168.1.1 to 192.168.1.254
            String host = subnet + i;
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address.isReachable(100)) { // 100ms timeout
                    System.out.println("Device found: " + host);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
