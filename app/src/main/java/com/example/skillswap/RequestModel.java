package com.example.skillswap;

public class RequestModel {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_REJECTED = "Rejected";

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
    private long timestamp;

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
    public long getTimestamp() { return timestamp; }

    // Setters used by Firebase updates and model compatibility
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}