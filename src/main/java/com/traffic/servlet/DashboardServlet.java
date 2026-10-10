package com.traffic.servlet;

import com.traffic.dao.AlertDAO;
import com.traffic.dao.JunctionDAO;
import com.traffic.dao.TrafficDataDAO;
import com.traffic.model.Junction;
import com.traffic.model.TrafficData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final JunctionDAO junctionDAO = new JunctionDAO();
    private final TrafficDataDAO trafficDAO = new TrafficDataDAO();
    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Junction> junctions = junctionDAO.getAll();
            Map<Integer, TrafficData> latest = trafficDAO.getLatestPerJunction();
            int severe = 0, high = 0;
            for (Junction j : junctions) {
                TrafficData d = latest.get(j.getJunctionId());
                j.setLatest(d);
                if (d != null) {
                    String level = d.getLevel();
                    if ("SEVERE".equals(level)) severe++;
                    else if ("HIGH".equals(level)) high++;
                }
            }
            req.setAttribute("junctions", junctions);
            req.setAttribute("totalJunctions", junctions.size());
            req.setAttribute("vehiclesToday", trafficDAO.totalVehiclesToday());
            req.setAttribute("openAlerts", alertDAO.countOpen());
            req.setAttribute("totalRecords", trafficDAO.totalRecords());
            req.setAttribute("severeCount", severe);
            req.setAttribute("highCount", high);
            req.setAttribute("hourly", trafficDAO.hourlyAverages());
            req.setAttribute("recentAlerts", alertDAO.getAll(5));
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
