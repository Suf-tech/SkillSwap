package com.example.skillswap;

public class AdminUser {
    // UID add karna zaroori hai details aur edit ke liye
    public String uid;
    public String email;
    public String name;
    public int avatarId;

    // Constructor mein ab 4 arguments honge
    public AdminUser(String uid, String email, String name, int avatarId) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.avatarId = avatarId;
    }
}