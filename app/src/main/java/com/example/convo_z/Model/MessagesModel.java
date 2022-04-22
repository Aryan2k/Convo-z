package com.example.convo_z.Model;

public class MessagesModel {

    String uId,message;
    Long Timestamp;
    String messageID;         //message id of the message for sender
    String messageID_receiver; //message id of the same message for receiver to facilitate deletion for all
    String message_status;     //to tell if a message is active or deleted

    public MessagesModel(String uId, String message, Long timestamp, String messageID, String messageID_receiver, String message_status) {
        this.uId = uId;
        this.message = message;
        Timestamp = timestamp;
        this.messageID = messageID;
        this.messageID_receiver = messageID_receiver;
        this.message_status = message_status;
    }

    public String getMessageID_receiver() {
        return messageID_receiver;
    }

    public void setMessageID_receiver(String messageID_receiver) {
        this.messageID_receiver = messageID_receiver;
    }

    public String getMessage_status() {
        return message_status;
    }

    public void setMessage_status(String message_status) {
        this.message_status = message_status;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public MessagesModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public MessagesModel()
    {

    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(Long timestamp) {
        Timestamp = timestamp;
    }
}
