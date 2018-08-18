package com.company.udptest.udpconnection.messagescash;

import com.company.udptest.udpconnection.Message;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Здесь хранятся все waiting messages, отправленные на IP:port.
 */
class AddressCash {
    /**IP адресата.*/
    private InetAddress inetAddress;
    /**Порт адресата.*/
    private int port;
    /**Количество сообщений, отправленных на этот адрес.*/
    private long messageId;
    /**Сообщения, которые ожидают подтверждения доставки.*/
    private ArrayList<Message> waitingMessages;

    /**
     * Создает список сообщений, отправленных на заданные IP и port, и ожидающих доставку.
     * @param inetAddress - IP адресата
     * @param port - порт адресата
     */
    AddressCash(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
        messageId = 0;
        waitingMessages = new ArrayList<>();
    }

    /**
     * Возвращает true, если этот контейнер хранит сообщения для введенного адресата, иначе false
     * @param inetAddress - IP адресата
     * @param port - порт адресата
     */
    public boolean isEqual(InetAddress inetAddress, int port) {
        if ((this.inetAddress.equals(inetAddress))&&(this.port == port))
            return true;
        else
            return false;
    }

    /**
     * Добавляет сообщение в список ожидания.
     * Сообщение не будет добавлено, если его ID не равен количеству сообщений,
     * отправленных на заданного в контейнере AddressCash адресата.
     */
    public void addMessage(Message message) {
        if (message.getId() == messageId) {
            if (message.isRequest()) {
                waitingMessages.add(message);
                messageId += 1;
            }
        }
    }

    /**
     * Удаляет сообщение из списка ожидания по его ответу.
     * @param answerMessage - Ответ к тому Message, который хотите удалить
     */
    public void removeMessageByAnswer(Message answerMessage) {
        for (int i = 0; i < waitingMessages.size(); i++)
            if (answerMessage.isAnswerTo(waitingMessages.get(i)))
                waitingMessages.remove(i);
    }


    /**
     * Удаляет сообщение из списка ожидания по его оригиналу.
     * @param originalMessage - Ответ к тому Message, который хотите удалить
     */
    public void removeMessageByOrigin(Message originalMessage) {
        for (int i = 0; i < waitingMessages.size(); i++)
            if (originalMessage.isEqual(waitingMessages.get(i)))
                waitingMessages.remove(i);
    }

    /**IP адресата.*/
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**Порт адресата.*/
    public int getPort() {
        return port;
    }

    /**Количество сообщений, отправленных на заданный адрес.*/
    public long getMessageId() {
        return messageId;
    }

    /**Возвращает ожидающее доставки сообщение по индексу из списка всех ожидающих доставки на заданный адрес сообщений.*/
    public Message getWaitingMessage(int i) {
        return waitingMessages.get(i);
    }

    /**Возвращает размер списка ожидающих доставки сообщений.*/
    public int getWaitingMessagesSize() {
        return waitingMessages.size();
    }
}
