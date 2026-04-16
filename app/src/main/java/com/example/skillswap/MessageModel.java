package com.example.skillswap;

public class MessageModel {
    private String sender;    // UID base
    private String receiver;  // UID base
    private String messageText;

    // Firebase requires an empty constructor
    public MessageModel() {}

    public MessageModel(String sender, String receiver, String messageText) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageText = messageText;
    }

    // Getters
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getMessageText() { return messageText; }
}