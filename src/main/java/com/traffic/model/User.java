package com.traffic.model;

/** Abstract base class for all users (inheritance + abstraction + encapsulation). */
public abstract class User {
    private int userId;
    private String name;
    private String username;
    private String role;

    protected User(int userId, String name, String username, String role) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /** Polymorphic permission check. */
    public abstract boolean canManageJunctions();

    /** Polymorphic description shown in the navigation bar. */
    public abstract String getAccessLevel();
}
