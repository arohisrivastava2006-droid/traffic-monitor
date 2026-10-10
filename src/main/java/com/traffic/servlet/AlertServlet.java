package com.traffic.servlet;

import com.traffic.dao.AlertDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/alerts")
public class AlertServlet extends HttpServlet {
    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("alerts", alertDAO.getAll(200));
            req.setAttribute("openAlerts", alertDAO.countOpen());
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/alerts.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        try {
            if ("resolveAll".equals(req.getParameter("action"))) {
                alertDAO.resolveAll();
                session.setAttribute("flash", "All open alerts marked as resolved.");
            } else if ("clear".equals(req.getParameter("action"))) {
                alertDAO.deleteAll();
                session.setAttribute("flash", "All alerts cleared.");
            } else {
                alertDAO.resolve(Integer.parseInt(req.getParameter("id")));
                session.setAttribute("flash", "Alert marked as resolved.");
            }
            session.setAttribute("flashType", "ok");
        } catch (NumberFormatException | SQLException e) {
            session.setAttribute("flash", "Could not resolve alert: " + e.getMessage());
            session.setAttribute("flashType", "error");
        }
        resp.sendRedirect(req.getContextPath() + "/alerts");
    }
}
