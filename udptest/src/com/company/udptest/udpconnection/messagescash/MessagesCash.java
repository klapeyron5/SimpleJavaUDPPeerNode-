package com.company.udptest.udpconnection.messagescash;

import com.company.udptest.udpconnection.Message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Словарь: key = <String IP, int port>; value = array(waitingMessages)
 */
public class MessagesCash {
    /**Сюда кидаем все события по поводу доставки сообщений.*/
    private MessageDeliveryListener messageDeliveryListener;

    /**Непосредственно, данные словаря MessagesCash.*/
    private ArrayList<AddressCash> listOfAddresses = new ArrayList<>();

    /**
     * Таймер, который проверяет, пришел ли ответ на отправленное сообщение.
     * Если ответ не приходит слишком долго, дергает событие, которое сигнализирует о невозможности доставки сообщения.
     */
    private Timer waitingTimeoutTimer;

    public MessagesCash(MessageDeliveryListener messageDeliveryListener) {
        this.messageDeliveryListener = messageDeliveryListener;
        waitingTimeoutTimer = new Timer();
        long timerPeriod = 10000;//in milliseconds
        waitingTimeoutTimer.scheduleAtFixedRate(new TimerTask() {
            long timerPeriod = 10;//in seconds
            long timerTimeout = timerPeriod * 5;
            @Override
            public void run() {
                Date date = new Date();
                long currentTime = date.getTime()/1000; //UNIX in seconds
                int size1 = listOfAddresses.size();
                for (int i = 0; i < size1; i++) {
                    AddressCash addressCash = listOfAddresses.get(i);
                    for (int j = 0; j < addressCash.getWaitingMessagesSize(); j++) {
                        Message message = addressCash.getWaitingMessage(j);
                        if (currentTime - timerTimeout >= message.getTimestamp()) {
                            //истекло время ожидания подтверждения
                            int size2 = addressCash.getWaitingMessagesSize();
                            messageDeliveryListener.onDeliveryTimeout(addressCash.getInetAddress(), addressCash.getPort(), message);
                            int size3 = addressCash.getWaitingMessagesSize();
                            if (size2 > size3)
                                j -= size2 - size3; //если ожидаем, что сообщение удалили из списка ожидания
                        } else {
                            if (currentTime - timerPeriod >= message.getTimestamp()) {
                                //повторная отправка сообщения
                                messageDeliveryListener.onDeliveryRepeatSending(addressCash.getInetAddress(), addressCash.getPort(), message);
                            }
                        }
                    }
                }
            }
        },timerPeriod,timerPeriod);
    }

    /**
     * message.id равен текущему AddressCash(inetAddress,port).messageId.
     */
    public void addMessage(InetAddress inetAddress, int port, Message message) {
        getAddressCash(inetAddress,port).addMessage(message);
    }

    /**
     * Удаляет сообщение, если пришел его ответ.
     * @param inetAddress - IP отправителя
     * @param port - порт отправителя
     * @param message - пришедший ответ
     */
    public void removeMessageByAnswer(InetAddress inetAddress, int port, Message message) {
        getAddressCash(inetAddress,port).removeMessageByAnswer(message);
    }

    /**
     * Удаляет сообщение - копию (если оно было отправлено раньше и до сих пор не было подтверждения доставки).
     * @param inetAddress - IP адресата
     * @param port - порт адресата
     * @param message - отправленное сообщение
     */
    public void removeMessageByOrigin(InetAddress inetAddress, int port, Message message) {
        getAddressCash(inetAddress,port).removeMessageByOrigin(message);
    }

    /**
     * Возвращает минимальный ID сообщения, который еще не был использован в общении с текущим адресатом.
     * @param inetAddress - IP адресата
     * @param port - порт адресата
     */
    public long getCurrentID(InetAddress inetAddress, int port) {
        return getAddressCash(inetAddress,port).getMessageId();
    }

    /**
     * Возвращает waitingMessages для этого адреса. Если такого ключа нет, то сначала добавляет его.
     */
    private AddressCash getAddressCash(InetAddress inetAddress, int port) {
        for (int i = 0; i < listOfAddresses.size(); i++)
            if (listOfAddresses.get(i).isEqual(inetAddress,port))
                return listOfAddresses.get(i);
        listOfAddresses.add(new AddressCash(inetAddress,port));
        return listOfAddresses.get(listOfAddresses.size()-1);
    }
}
