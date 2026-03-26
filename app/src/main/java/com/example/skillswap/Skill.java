package com.example.skillswap;

public class Skill {
    String title, teacher;

    public Skill(String title, String teacher) {
        this.title = title;
        this.teacher = teacher;
    }

    public String getTitle() { return title; }
    public String getTeacher() { return teacher; }
}
