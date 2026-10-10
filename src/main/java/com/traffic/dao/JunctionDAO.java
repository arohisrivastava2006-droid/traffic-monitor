package com.traffic.dao;

import com.traffic.model.Junction;
import com.traffic.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JunctionDAO {

    public List<Junction> getAll() throws SQLException {
        List<Junction> list = new ArrayList<>();
        String sql = "SELECT junction_id, junction_name, location FROM junction ORDER BY junction_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Junction(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return list;
    }

    public void add(String name, String location) throws SQLException {
        String sql = "INSERT INTO junction (junction_name, location) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location);
            ps.executeUpdate();
        }
    }

    public void update(int id, String name, String location) throws SQLException {
        String sql = "UPDATE junction SET junction_name = ?, location = ? WHERE junction_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, location);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM junction WHERE junction_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static final String[][] DEMO = {
        {"Silk Board Junction", "Hosur Road & Outer Ring Road"},
        {"Hebbal Flyover", "Bellary Road & Outer Ring Road"},
        {"Marathahalli Bridge", "Old Airport Road"},
        {"KR Puram Bridge", "Old Madras Road"},
        {"Majestic Bus Stand", "Kempegowda Road"},
        {"Electronic City Toll", "Hosur Road, Phase 1"},
        {"Indiranagar 100ft Road", "100 Feet Road & 12th Main"},
        {"Koramangala Sony Signal", "80 Feet Road & Inner Ring Road"},
        {"Whitefield Main Road", "ITPL Main Road"},
        {"Jayanagar 4th Block", "11th Main & 30th Cross"}
    };

    /** Adds ten more sample junctions (skips any that already exist). Returns how many were added. */
    public int addDemoJunctions() throws SQLException {
        int added = 0;
        try (Connection con = DBConnection.getConnection()) {
            for (String[] d : DEMO) {
                try (PreparedStatement chk = con.prepareStatement("SELECT 1 FROM junction WHERE junction_name = ?")) {
                    chk.setString(1, d[0]);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) continue;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO junction (junction_name, location) VALUES (?, ?)")) {
                    ps.setString(1, d[0]);
                    ps.setString(2, d[1]);
                    ps.executeUpdate();
                    added++;
                }
            }
        }
        return added;
    }
}
