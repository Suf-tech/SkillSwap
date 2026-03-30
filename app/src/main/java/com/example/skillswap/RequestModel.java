package com.example.skillswap;

public class RequestModel {
    private int id;
    private int postId;
    private String sender;
    private String receiver;
    private String offered;
    private String required;
    private String msg;
    private String status;
    private int senderRated;
    private int receiverRated;

    public RequestModel(int id, int postId, String sender, String receiver, String offered, String required, String msg, String status, int senderRated, int receiverRated) {
        this.id = id;
        this.postId = postId;
        this.sender = sender;
        this.receiver = receiver;
        this.offered = offered;
        this.required = required;
        this.msg = msg;
        this.status = status;
        this.senderRated = senderRated;
        this.receiverRated = receiverRated;
    }

    public int getId() { return id; }
    public int getPostId() { return postId; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getOffered() { return offered; }
    public String getRequired() { return required; }
    public String getMsg() { return msg; }
    public String getStatus() { return status; }
    public int getSenderRated() { return senderRated; }
    public int getReceiverRated() { return receiverRated; }

    public void setStatus(String status) { this.status = status; }
    public void setSenderRated(int senderRated) { this.senderRated = senderRated; }
    public void setReceiverRated(int receiverRated) { this.receiverRated = receiverRated; }
}