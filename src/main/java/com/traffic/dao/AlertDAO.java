package com.traffic.dao;

import com.traffic.model.Alert;
import com.traffic.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    public void add(Alert a) throws SQLException {
        String sql = "INSERT INTO alerts (junction_id, alert_type, severity, message, timestamp, resolved) "
                   + "VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, a.getJunctionId());
            ps.setString(2, a.getAlertType());
            ps.setString(3, a.getSeverity());
            ps.setString(4, a.getMessage());
            ps.setTimestamp(5, Timestamp.valueOf(a.getTimestamp()));
            ps.executeUpdate();
        }
    }

    public List<Alert> getAll(int limit) throws SQLException {
        String sql = "SELECT a.*, j.junction_name FROM alerts a JOIN junction j ON a.junction_id = j.junction_id "
                   + "ORDER BY a.resolved ASC, a.timestamp DESC LIMIT ?";
        List<Alert> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alert a = new Alert();
                    a.setAlertId(rs.getInt("alert_id"));
                    a.setJunctionId(rs.getInt("junction_id"));
                    a.setJunctionName(rs.getString("junction_name"));
                    a.setAlertType(rs.getString("alert_type"));
                    a.setSeverity(rs.getString("severity"));
                    a.setMessage(rs.getString("message"));
                    a.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    a.setResolved(rs.getBoolean("resolved"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    public int countOpen() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM alerts WHERE resolved = 0")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countAll() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM alerts")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void resolve(int alertId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE alerts SET resolved = 1 WHERE alert_id = ?")) {
            ps.setInt(1, alertId);
            ps.executeUpdate();
        }
    }

    public void resolveAll() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE alerts SET resolved = 1 WHERE resolved = 0");
        }
    }

    public void deleteAll() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM alerts");
        }
    }
}
