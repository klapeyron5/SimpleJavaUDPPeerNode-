package com.company.udptest.udpconnection.udpsocket;


import com.company.udptest.Main;
import com.company.udptest.udpconnection.Message;

import java.io.IOException;
import java.net.*;

/**
 * Виртуальный udp сокет: поток прослушки порта и
 * метод отправки сообщений с этого же порта.
 */
public class UDPSocket {
    /**Сюда кидаем все события, происходящие в сокете.*/
    private UDPSocketListener udpSocketListener;

    /**
     * Через эту переменную осуществляется непосредственно доступ к порту.
     * Доступ рекомендуется только с помощью synchronized (специально для этого есть getLocalSocket()).
     */
    private static DatagramSocket localSocket;

    /**Поток прослушки входящих сообщений (на заданном порту).*/
    private Thread listeningThread;

    /**
     * Основная переменная, контролирующая жизнь сокета. Нельзя извне обратно включить сокет,
     * если его выключали. Только с помощью создания нового через конструктор.
     */
    private boolean alive;

    /**
     * Запускает прослушку заданного порта.
     */
    public UDPSocket(int inPort, UDPSocketListener udpSocketListener) {
        this.udpSocketListener = udpSocketListener;
        try {
            localSocket = new DatagramSocket(inPort);//SocketExc
            startNode();
        } catch (SocketException e) { //localSocket == null; listeningThread == null
            udpSocketListener.onSocketCreationException(this,e.toString());
            setAlive(false);
        }
    }

    /**
     * Запускает прослушку заданного порта.
     */
    private void startNode() {
        listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] receiveData;
                while(!getLocalSocket().isClosed()) {
                    receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        getLocalSocket().receive(receivePacket); //IOExc
                    } catch (SocketException e) {
                        if (!getLocalSocket().isClosed()) //else: закрыть сокет можно только вручную .close() => был вызван closeNode() => это специально
                            udpSocketListener.onSocketReceivingException(UDPSocket.this,"Socket isn't closed, but exception is occurred: "+e.toString());
                        continue;
                    } catch (IOException e) {
                        udpSocketListener.onSocketReceivingException(UDPSocket.this,e.toString());
                        continue;
                    }
                    String receivedString = new String(receivePacket.getData());
                    udpSocketListener.onSocketMessageReceived(receivePacket.getAddress(),receivePacket.getPort(),receivedString);
                }
                if (isAlive()) {
                    udpSocketListener.onSocketListeningException(UDPSocket.this);
                }
            }
        });
        listeningThread.start();
        if (!getLocalSocket().isClosed()) { //localSocket != null; также на всякий убеждаемся, что порт открыт, т.е. цикл прослушки заработает 100%
            setAlive(true);
            udpSocketListener.onSocketListeningReady(UDPSocket.this);
        } else { //сюда, вроде, не реально попасть
            setAlive(false);
            udpSocketListener.onSocketCreationException(this,"Magic error, localSocket is closed when nobody has closed it");
        }
    }

    /**
     * Останавливает прослушку заданного порта, а также закрывает сокет. Для старта новых коммуникаций, надо создавать класс заново.
     */
    public void closeNode() {
        if (isAlive()) {
            Main.writeLine("Closing UDP listening..."); //TODO just log
            while (!getLocalSocket().isClosed())
                //SocketException если в этот же момент идет socket.receive, но у нас поток прослушки и поток, из которого вызываем closeNode разные,
                //а доступ к сокету только через synchronized метод
                    getLocalSocket().close(); //localSocket != null
            setAlive(false);
            try {
                listeningThread.join(); //listeningThread != null
                udpSocketListener.onSocketListeningClosed(UDPSocket.this);
            } catch (InterruptedException e) {
                udpSocketListener.onSocketListeningClosed(UDPSocket.this);
            }
        } else {
            Main.writeLine("Tried to close node udp socket, but it's already closed"); //TODO just log
        }
    }

    /**
     * Отправляет сообщение заданному адресату.
     * @param outIP - IP адресата
     * @param outPort - порт адресата
     * @param message - форматированное сообщение
     */
    public void sendMessage(InetAddress outIP, int outPort, Message message) {
        byte[] sendData;
        sendData = message.toString().getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,outIP,outPort);
            if (isAlive()) {
                getLocalSocket().send(sendPacket);
                udpSocketListener.onSocketMessageSent(outIP, outPort, message);
            } else {
                udpSocketListener.onSocketMessageIsNotSentNodeIsClosed();
            }
        } catch (IOException e) {
            udpSocketListener.onSocketMessageIsNotSentIOException();
            e.printStackTrace();
        }
    }

    private synchronized DatagramSocket getLocalSocket() {
        return localSocket;
    }

    /**
     * Значение прослушивающегося порта.
     * Возвращает -1 если сокет закрыт.
     */
    public int getLocalPort() {
        return getLocalSocket().getLocalPort();
    }

    /**
     * Если true, то работает прослушка заданного порта и возможность отправки сообщений.
     * Иначе не работает ничего из вышеперечисленного и надо создавать экземпляр класса заново, если хотите коммуницировать.
     */
    public synchronized boolean isAlive() {
        return alive;
    }

    private synchronized void setAlive(boolean isAlive) {
        alive = isAlive;
    }
}
