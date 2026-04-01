package com.example.skillswap;

public class AdminSkillItem {
    private final String name;
    private final int count;
    private final boolean requested;
    private final boolean wanted;
    private final boolean offered;

    public AdminSkillItem(String name, int count, boolean requested, boolean wanted, boolean offered) {
        this.name = name;
        this.count = count;
        this.requested = requested;
        this.wanted = wanted;
        this.offered = offered;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public boolean isRequested() {
        return requested;
    }

    public boolean isWanted() {
        return wanted;
    }

    public boolean isOffered() {
        return offered;
    }
}

