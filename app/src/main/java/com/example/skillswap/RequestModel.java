package com.example.skillswap;

public class RequestModel {
    private String id;
    private String postId;
    private String senderId;
    private String receiverId;
    private String senderEmail;
    private String receiverEmail;
    private String senderName;
    private String receiverName;
    private String offered;
    private String required;
    private String msg;
    private String status;

    public RequestModel() {} // Required for Firebase

    // Getters
    public String getId() { return id; }
    public String getPostId() { return postId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getSenderEmail() { return senderEmail; }
    public String getReceiverEmail() { return receiverEmail; }
    public String getSenderName() { return senderName; }
    public String getReceiverName() { return receiverName; }
    public String getOffered() { return offered; }
    public String getRequired() { return required; }
    public String getMsg() { return msg; }
    public String getStatus() { return status; }

    // Setter (Only for status update)
    public void setStatus(String status) { this.status = status; }
}