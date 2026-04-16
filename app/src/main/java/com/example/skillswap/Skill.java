package com.example.skillswap;

public class Skill {
    String id;
    String title, teacher, email, want, have, message; // 'message' add kiya
    int avatarId;
    boolean isOpen; // 'isOpen' add kiya

    public Skill() {}

    public Skill(String id, String title, String teacher, String email, int avatarId, String want, String have, String message, boolean isOpen) {
        this.id = id;
        this.title = title;
        this.teacher = teacher;
        this.email = email;
        this.avatarId = avatarId;
        this.want = want;
        this.have = have;
        this.message = message;
        this.isOpen = isOpen;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getTeacher() { return teacher; }
    public String getEmail() { return email; }
    public int getAvatarId() { return avatarId; }
    public String getWant() { return want; }
    public String getHave() { return have; }

    // In dono methods ki wajah se Adapter mein error aa raha tha
    public String getMessage() { return message; }
    public boolean isIsOpen() { return isOpen; }
}