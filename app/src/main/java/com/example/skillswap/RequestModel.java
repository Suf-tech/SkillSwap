package com.example.skillswap;

public class RequestModel {
    int id;
    String sender, receiver, offered, required, msg, status;

    public RequestModel(int id, String sender, String receiver, String offered, String required, String msg, String status) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.offered = offered;
        this.required = required;
        this.msg = msg;
        this.status = status;
    }

    public int getId() { return id; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getOffered() { return offered; }
    public String getRequired() { return required; }
    public String getMsg() { return msg; }
    public String getStatus() { return status; }
}