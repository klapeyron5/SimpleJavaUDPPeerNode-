package com.company.udptest.udpconnection.messagescash;

import com.company.udptest.udpconnection.Message;

import java.net.InetAddress;

/**События, относящиеся к механизму подтверждения доставки собщений.*/
public interface MessageDeliveryListener {
    /**Повторная отправка сообщения.*/
    void onDeliveryRepeatSending(InetAddress inetAddress, int port, Message message);
    /**Истек лимит повторных отправок сообщения.*/
    void onDeliveryTimeout(InetAddress inetAddress, int port, Message message);
}
