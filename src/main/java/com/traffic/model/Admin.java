package com.traffic.model;

public class Admin extends User {
    public Admin(int userId, String name, String username) {
        super(userId, name, username, "ADMIN");
    }

    @Override
    public boolean canManageJunctions() { return true; }

    @Override
    public String getAccessLevel() { return "Administrator (full access)"; }
}
