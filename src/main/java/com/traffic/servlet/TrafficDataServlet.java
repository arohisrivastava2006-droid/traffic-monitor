package com.traffic.servlet;

import com.traffic.dao.JunctionDAO;
import com.traffic.dao.TrafficDataDAO;
import com.traffic.exception.InvalidTrafficDataException;
import com.traffic.model.TrafficData;
import com.traffic.service.TrafficProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

@WebServlet("/traffic")
public class TrafficDataServlet extends HttpServlet {
    private final JunctionDAO junctionDAO = new JunctionDAO();
    private final TrafficDataDAO trafficDAO = new TrafficDataDAO();
    private final TrafficProcessor processor = new TrafficProcessor();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Integer junctionId = null;
            String jp = req.getParameter("junctionId");
            if (jp != null && !jp.isBlank()) junctionId = Integer.parseInt(jp);
            req.setAttribute("junctions", junctionDAO.getAll());
            req.setAttribute("records", trafficDAO.getRecent(junctionId, 100));
            req.setAttribute("selectedJunction", junctionId);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid junction filter.");
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/traffic.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        String action = req.getParameter("action");
        try {
            if ("delete".equals(action)) {
                trafficDAO.delete(Integer.parseInt(req.getParameter("id")));
                flash(session, "Record deleted.", "ok");
            } else if ("clearAll".equals(action)) {
                trafficDAO.deleteAll();
                flash(session, "All traffic records deleted.", "ok");
            } else if ("simulate".equals(action)) {
                String mode = req.getParameter("mode");
                boolean history = "history".equals(mode) || "history30".equals(mode);
                int days = "history30".equals(mode) ? 30 : 7;
                int stored = processor.simulate(junctionDAO.getAll(), history ? (days == 30 ? 150 : 40) : 6, history, days);
                flash(session, stored + " sample readings generated and processed using multiple threads.", "ok");
            } else {
                TrafficData d = new TrafficData(
                        Integer.parseInt(req.getParameter("junctionId")),
                        Integer.parseInt(req.getParameter("vehicleCount").trim()),
                        Double.parseDouble(req.getParameter("density").trim()),
                        Double.parseDouble(req.getParameter("speed").trim()),
                        LocalDateTime.now());
                String level = processor.processReading(d, true, null);
                flash(session, "Record saved. Traffic level: " + level, "ok");
            }
        } catch (NumberFormatException | NullPointerException e) {
            flash(session, "Please enter valid numbers in every field.", "error");
        } catch (InvalidTrafficDataException e) {
            flash(session, e.getMessage(), "error");
        } catch (SQLException e) {
            flash(session, "Database error: " + e.getMessage(), "error");
        } catch (Exception e) {
            flash(session, "Processing error: " + e.getMessage(), "error");
        }
        resp.sendRedirect(req.getContextPath() + "/traffic");
    }

    private void flash(HttpSession s, String msg, String type) {
        s.setAttribute("flash", msg);
        s.setAttribute("flashType", type);
    }
}
