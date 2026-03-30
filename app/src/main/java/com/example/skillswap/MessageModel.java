package com.example.skillswap;

public class MessageModel {
    private String senderEmail;
    private String receiverEmail;
    private String messageText;

    public MessageModel(String senderEmail, String receiverEmail, String messageText) {
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.messageText = messageText;
    }

    public String getSenderEmail() { return senderEmail; }
    public String getReceiverEmail() { return receiverEmail; }
    public String getMessageText() { return messageText; }
}