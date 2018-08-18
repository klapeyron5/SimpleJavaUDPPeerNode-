package com.company.udptest;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Информация о приложении.
 */
public class AppInfo {
    public static final String AppName = "ClapeyronGoUDPp2pTest";
    public static final double AppVersion = 0.1;
    public static InetAddress LocalIP;

    static {
        try {
            LocalIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            Main.writeLine("Error: cant get a localhost info");
            e.printStackTrace();
        }
    }
}
