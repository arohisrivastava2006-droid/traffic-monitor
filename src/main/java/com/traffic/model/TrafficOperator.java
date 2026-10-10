package com.traffic.model;

public class TrafficOperator extends User {
    public TrafficOperator(int userId, String name, String username) {
        super(userId, name, username, "OPERATOR");
    }

    @Override
    public boolean canManageJunctions() { return false; }

    @Override
    public String getAccessLevel() { return "Traffic Operator (monitor and report)"; }
}
