package com.traffic.servlet;

import com.traffic.dao.AlertDAO;
import com.traffic.dao.TrafficDataDAO;
import com.traffic.model.TrafficData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {
    private final TrafficDataDAO trafficDAO = new TrafficDataDAO();
    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String csv = req.getParameter("csv");
        try {
            if (csv != null) {
                exportCsv(csv, resp);
                return;
            }
            Map<String, Object> kpi = trafficDAO.keyIndicators();
            kpi.put("alerts", alertDAO.countAll());
            req.setAttribute("kpi", kpi);
            List<Map<String, Object>> summary = trafficDAO.junctionSummary();
            req.setAttribute("summary", summary);
            req.setAttribute("periods", trafficDAO.periodAverages());
            req.setAttribute("hourly", trafficDAO.hourlyAverages());
            req.setAttribute("daily", trafficDAO.dailyStats());
            if (!summary.isEmpty() && summary.get(0).get("records") != null
                    && ((Number) summary.get(0).get("records")).intValue() > 0) {
                req.setAttribute("busiest", summary.get(0));
                req.setAttribute("quietest", summary.get(summary.size() - 1));
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/reports.jsp").forward(req, resp);
    }

    private void exportCsv(String type, HttpServletResponse resp) throws SQLException, IOException {
        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment; filename=\"traffic-" + type + ".csv\"");
        PrintWriter out = resp.getWriter();
        if ("records".equals(type)) {
            out.println("record_id,junction,vehicle_count,density,average_speed,level,timestamp");
            for (TrafficData d : trafficDAO.getRecent(null, 5000)) {
                out.println(d.getRecordId() + ",\"" + d.getJunctionName() + "\"," + d.getVehicleCount() + ","
                        + d.getTrafficDensity() + "," + d.getAverageSpeed() + "," + d.getLevel() + ",\""
                        + d.getTimestampText() + "\"");
            }
        } else if ("daily".equals(type)) {
            out.println("day,total_vehicles,avg_density,avg_speed,records");
            for (Map<String, Object> r : trafficDAO.dailyStats()) {
                out.println(r.get("day") + "," + r.get("totalVehicles") + "," + r.get("avgDensity") + ","
                        + r.get("avgSpeed") + "," + r.get("records"));
            }
        } else {
            out.println("junction,records,avg_vehicles,avg_density,avg_speed,peak_vehicles");
            for (Map<String, Object> r : trafficDAO.junctionSummary()) {
                out.println("\"" + r.get("name") + "\"," + r.get("records") + "," + r.get("avgVehicles") + ","
                        + r.get("avgDensity") + "," + r.get("avgSpeed") + "," + r.get("peakVehicles"));
            }
        }
        out.flush();
    }
}
