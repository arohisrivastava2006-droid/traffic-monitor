package com.traffic.dao;

import com.traffic.model.TrafficData;
import com.traffic.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrafficDataDAO {

    public void add(TrafficData d) throws SQLException {
        String sql = "INSERT INTO traffic_data (junction_id, vehicle_count, density, average_speed, timestamp) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getJunctionId());
            ps.setInt(2, d.getVehicleCount());
            ps.setDouble(3, d.getTrafficDensity());
            ps.setDouble(4, d.getAverageSpeed());
            ps.setTimestamp(5, Timestamp.valueOf(d.getTimestamp()));
            ps.executeUpdate();
        }
    }

    public void delete(int recordId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM traffic_data WHERE record_id = ?")) {
            ps.setInt(1, recordId);
            ps.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM traffic_data");
        }
    }

    /** Recent records, optionally for a single junction (pass null for all). */
    public List<TrafficData> getRecent(Integer junctionId, int limit) throws SQLException {
        String sql = "SELECT t.*, j.junction_name FROM traffic_data t "
                   + "JOIN junction j ON t.junction_id = j.junction_id "
                   + (junctionId != null ? "WHERE t.junction_id = ? " : "")
                   + "ORDER BY t.timestamp DESC, t.record_id DESC LIMIT ?";
        List<TrafficData> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            if (junctionId != null) ps.setInt(i++, junctionId);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Latest reading for every junction, keyed by junction id. */
    public Map<Integer, TrafficData> getLatestPerJunction() throws SQLException {
        String sql = "SELECT t.*, j.junction_name FROM traffic_data t "
                   + "JOIN junction j ON t.junction_id = j.junction_id "
                   + "WHERE t.record_id IN (SELECT MAX(record_id) FROM traffic_data "
                   + "                      GROUP BY junction_id)";
        Map<Integer, TrafficData> map = new HashMap<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                TrafficData d = map(rs);
                map.put(d.getJunctionId(), d);
            }
        }
        return map;
    }

    public long totalVehiclesToday() throws SQLException {
        return scalar("SELECT COALESCE(SUM(vehicle_count), 0) FROM traffic_data WHERE DATE(timestamp) = CURDATE()");
    }

    public long totalRecords() throws SQLException {
        return scalar("SELECT COUNT(*) FROM traffic_data");
    }

    /* ---------- analytics queries ---------- */

    public List<Map<String, Object>> junctionSummary() throws SQLException {
        return queryMaps("SELECT j.junction_name AS name, COUNT(t.record_id) AS records, "
                + "ROUND(AVG(t.vehicle_count),1) AS avgVehicles, ROUND(AVG(t.density),1) AS avgDensity, "
                + "ROUND(AVG(t.average_speed),1) AS avgSpeed, MAX(t.vehicle_count) AS peakVehicles "
                + "FROM junction j LEFT JOIN traffic_data t ON t.junction_id = j.junction_id "
                + "GROUP BY j.junction_id, j.junction_name ORDER BY avgVehicles DESC");
    }

    public List<Map<String, Object>> hourlyAverages() throws SQLException {
        return queryMaps("SELECT HOUR(timestamp) AS hr, ROUND(AVG(vehicle_count),1) AS avgVehicles, "
                + "ROUND(AVG(density),1) AS avgDensity FROM traffic_data GROUP BY HOUR(timestamp) ORDER BY hr");
    }

    public List<Map<String, Object>> periodAverages() throws SQLException {
        return queryMaps("SELECT CASE WHEN HOUR(timestamp) < 12 THEN 'Morning' "
                + "WHEN HOUR(timestamp) < 17 THEN 'Afternoon' ELSE 'Evening' END AS period, "
                + "ROUND(AVG(vehicle_count),1) AS avgVehicles, ROUND(AVG(density),1) AS avgDensity, "
                + "ROUND(AVG(average_speed),1) AS avgSpeed FROM traffic_data GROUP BY period "
                + "ORDER BY FIELD(period, 'Morning', 'Afternoon', 'Evening')");
    }

    public List<Map<String, Object>> dailyStats() throws SQLException {
        return queryMaps("SELECT DATE_FORMAT(DATE(timestamp), '%d %b %Y') AS day, "
                + "SUM(vehicle_count) AS totalVehicles, ROUND(AVG(density),1) AS avgDensity, "
                + "ROUND(AVG(average_speed),1) AS avgSpeed, COUNT(*) AS records "
                + "FROM traffic_data GROUP BY DATE(timestamp) ORDER BY DATE(timestamp) DESC LIMIT 14");
    }

    public Map<String, Object> keyIndicators() throws SQLException {
        List<Map<String, Object>> rows = queryMaps(
                "SELECT ROUND(AVG(density),1) AS avgDensity, ROUND(AVG(vehicle_count),1) AS avgVehicles, "
              + "ROUND(AVG(average_speed),1) AS avgSpeed, "
              + "SUM(CASE WHEN density >= 60 AND average_speed < 30 THEN 1 ELSE 0 END) AS congestionEvents "
              + "FROM traffic_data");
        return rows.isEmpty() ? new HashMap<>() : rows.get(0);
    }

    /* ---------- helpers ---------- */

    private long scalar(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private List<Map<String, Object>> queryMaps(String sql) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int c = 1; c <= md.getColumnCount(); c++) {
                    row.put(md.getColumnLabel(c), rs.getObject(c));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private TrafficData map(ResultSet rs) throws SQLException {
        TrafficData d = new TrafficData();
        d.setRecordId(rs.getInt("record_id"));
        d.setJunctionId(rs.getInt("junction_id"));
        d.setJunctionName(rs.getString("junction_name"));
        d.setVehicleCount(rs.getInt("vehicle_count"));
        d.setTrafficDensity(rs.getDouble("density"));
        d.setAverageSpeed(rs.getDouble("average_speed"));
        d.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return d;
    }
}
