package com.company.udptest.udpconnection;

import com.company.udptest.AppInfo;

import java.util.Date;

/**
 * Контейнер для сообщений.
 */
public class Message {
    //блоки DATA
    /**Имя программы, которая отправила это сообщение.*/
    private String appName;
    /**Версия программы, которая отправила это сообщение.*/
    private double appVersion;
    /**Тип сообщения: request или answer*/
    private String type;
    /**ID сообщения (нумеруется отправителем).*/
    private long id;
    /**UNIX time in seconds*/
    private long timestamp;
    /**Основные данные сообщения.*/
    private String data;

    /**Полный конструктор. Используется только при парсинге пришедших сообщений (при помощи Message.fromString())*/
    private Message(String appName,Double appVersion,String type,long id,long timestamp,String data) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.data = data;
    }

    /**
     * Создает сообщение - запрос (Message.type == REQ) с именем и версией текущей программы (AppInfo.java)
     * @param id - ID сообщения
     * @param data - данные сообщения
     */
    public Message(long id,String data) {
        this.appName = AppInfo.AppName;
        this.appVersion = AppInfo.AppVersion;
        this.type = CODE_REQUEST;
        this.id = id;
        Date date = new Date();
        this.timestamp = date.getTime()/1000;
        this.data = data;
    }

    /**
     * Превращает это сообщение-запрос в сообщение-ответ.
     */
    public void makeThisAnswer() {
        if (this.isRequest())
            this.type = CODE_ANSWER;
    }

    /**
     * Возвращает true, если это сообщения является ответом на originalMessage.
     * @param originalMessage - Предположительно оригинал (request) этого сообщения
     */
    public boolean isAnswerTo(Message originalMessage) {
        if ((this.isAnswer())
                &&(originalMessage.isRequest())
                &&(this.appName.equals(originalMessage.appName))
                &&(this.appVersion == originalMessage.appVersion)
                &&(this.id == originalMessage.id)
                &&(this.timestamp == originalMessage.timestamp)
                &&(this.data.equals(originalMessage.data)))
            return true;
        else
            return false;
    }

    /**
     * Возвращает true, если оба сообщения абсолютно одинаковы.
     */
    public boolean isEqual(Message message) {
        if ((this.appName.equals(message.appName))
                &&(this.appVersion == message.appVersion)
                &&(this.type.equals(message.type))
                &&(this.id == message.id)
                &&(this.timestamp == message.timestamp)
                &&(this.data.equals(message.data)))
            return true;
        else
            return false;
    }

    /**
     * Возвращает готовое сообщение для отправки.
     */
    @Override
    public String toString() {
        return "?"+AppInfo.AppName+"|"+AppInfo.AppVersion+"|"+type+"|"+id+"|"+timestamp+"|"+data+"?";
    }

    /**Возвращает true, если это сообщение является запросом по формату.*/
    public boolean isRequest() {
        if (type.equals(CODE_REQUEST))
            return true;
        else
            return false;
    }

    /**Возвращает true, если это сообщение является ответом по формату.*/
    public boolean isAnswer() {
        if (type.equals(CODE_ANSWER))
            return true;
        else
            return false;
    }

    private static final String CODE_REQUEST = "REQ";
    private static final String CODE_ANSWER = "ANS";
    private static final int NUMBER_OF_DATA_BLOCKS = 6;

    /**
     * Возвращает new Message, если строка строго форматирована под Message, иначе null.
     * @param receivedMessage полученное сообщение, которое предположительно содержит Message без обрамляющих знаков "?"
     */
    public static Message fromString(String receivedMessage) {
        String[] spltMsg = receivedMessage.split("\\|");
        if (spltMsg.length == NUMBER_OF_DATA_BLOCKS) {
            String appName;
            Double appVersion;
            String type;
            long id;
            long timestamp;
            String data;

            try {
                appName = spltMsg[0];
                if (!appName.equals(AppInfo.AppName))
                    return null;

                appVersion = Double.parseDouble(spltMsg[1]);
                if (!(appVersion == AppInfo.AppVersion))
                    return null;

                type = spltMsg[2];
                if (!((type.equals(CODE_REQUEST))||(type.equals(CODE_ANSWER))))
                    return null;

                id = Long.parseLong(spltMsg[3]);

                timestamp = Long.parseLong(spltMsg[4]);

                data = spltMsg[5];
            } catch (Exception e) {
                return null;
            }

            return new Message(appName,appVersion,type,id,timestamp,data);
        } else
            return null;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }
}