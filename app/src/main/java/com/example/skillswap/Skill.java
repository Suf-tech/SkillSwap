package com.example.skillswap;

public class Skill {
    int id; // New ID field
    String title, teacher, email;
    int avatarId;

    public Skill(int id, String title, String teacher, String email, int avatarId) {
        this.id = id;
        this.title = title;
        this.teacher = teacher;
        this.email = email;
        this.avatarId = avatarId;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getTeacher() { return teacher; }
    public String getEmail() { return email; }
    public int getAvatarId() { return avatarId; }
}