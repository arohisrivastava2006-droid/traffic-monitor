package com.traffic.dao;

import com.traffic.model.Admin;
import com.traffic.model.TrafficOperator;
import com.traffic.model.User;
import com.traffic.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {

    /** Returns the matching user (Admin or TrafficOperator) or null if the login is wrong. */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, name, username, role FROM users "
                   + "WHERE username = ? AND password = SHA2(?, 256)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int id = rs.getInt("user_id");
                String name = rs.getString("name");
                String uname = rs.getString("username");
                return "ADMIN".equalsIgnoreCase(rs.getString("role"))
                        ? new Admin(id, name, uname)
                        : new TrafficOperator(id, name, uname);
            }
        }
    }

    /**
     * Open-login mode: a new username is registered automatically (as an OPERATOR) with the
     * password that was typed; an existing username must use its own password.
     */
    public User authenticateOrRegister(String username, String password) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO users (name, username, password, role) VALUES (?, ?, SHA2(?, 256), 'OPERATOR')")) {
                        ins.setString(1, username);
                        ins.setString(2, username);
                        ins.setString(3, password);
                        ins.executeUpdate();
                    }
                }
            }
        }
        return authenticate(username, password);
    }

    public List<Map<String, Object>> getAll() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id, name, username, role FROM users ORDER BY user_id")) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt(1));
                m.put("name", rs.getString(2));
                m.put("username", rs.getString(3));
                m.put("role", rs.getString(4));
                list.add(m);
            }
        }
        return list;
    }

    public void setRole(int id, String role) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE users SET role = ? WHERE user_id = ?")) {
            ps.setString(1, "ADMIN".equals(role) ? "ADMIN" : "OPERATOR");
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
