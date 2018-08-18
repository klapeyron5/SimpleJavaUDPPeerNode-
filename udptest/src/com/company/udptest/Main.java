package com.company.udptest;

import com.company.udptest.udpconnection.UDPNode;

import java.net.*;
import java.util.Scanner;

public class Main {
    private static UDPNode udpNode;

    public static void main(String[] args) {
        writeLine("UDP peer node test");
        writeLine("Your IP is "+AppInfo.LocalIP.getHostAddress());
        try {
            writeLine("Your localhost is "+InetAddress.getByName("localhost"));
        } catch (UnknownHostException e) {
            writeLine("Can't detect your localhost");
            e.printStackTrace();
        }

        writeLine("Print <help> if u don't know what to do:");
        while(true) {
            consoleMenu(consoleReadLine());
        }
    }

    private static void consoleMenu(String task) {
        String[] splitTask = task.split(" ");
        switch (splitTask[0]) {
            case "help":
                writeLine( "startnode <port>: starts local port listening and opportunity for sending messages");
                writeLine( "send <IP> <port> <data>: sends data to IP: port. <data> must be without spaces");
                writeLine( "closenode: closes started UDP socket");
                writeLine( "exit: closes the app");
                break;
            case "startnode":
                try {
                    int inPort = Integer.parseInt(splitTask[1]);
                    if (udpNode != null) udpNode.closeNode();
                    udpNode = new UDPNode(inPort);
                } catch (Exception e) {
                    writeLine("Error formatting: no correct port");
                }
                break;
            case "send":
                try {
                    String outIP = splitTask[1];
                    int outPort = Integer.parseInt(splitTask[2]);
                    String outData = splitTask[3];
                    if (udpNode != null)
                        udpNode.sendNewString(outIP, outPort, outData);
                    else
                        Main.writeLine("Error: can't send message, please, start node");
                } catch (Exception e) {
                    writeLine("Error formatting: no correct IP or port or data");
                    e.printStackTrace();
                }
                break;
            case "closenode":
                if (udpNode != null)
                    udpNode.closeNode();
                else
                    Main.writeLine("Node hasn't started");
                break;
            case "exit":
                if (udpNode != null)
                    udpNode.closeNode();
                System.exit(0);
                break;
            default:
                writeLine("Command does not exist");
                break;
        }
    }

    public static void writeLine(String data) {
        System.out.println(data);
    }

    public static String consoleReadLine() {
        Scanner in = new Scanner(System.in);
        String read = in.nextLine();
        return read;
    }
}
