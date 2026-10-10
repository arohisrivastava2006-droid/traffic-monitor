package com.traffic.servlet;

import com.traffic.dao.JunctionDAO;
import com.traffic.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/junctions")
public class JunctionServlet extends HttpServlet {
    private final JunctionDAO junctionDAO = new JunctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("junctions", junctionDAO.getAll());
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/junctions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !user.canManageJunctions()) {
            flash(session, "Only administrators can change junctions.", "error");
            resp.sendRedirect(req.getContextPath() + "/junctions");
            return;
        }
        String action = req.getParameter("action");
        String name = req.getParameter("name");
        String location = req.getParameter("location");
        try {
            if ("seed".equals(action)) {
                int n = junctionDAO.addDemoJunctions();
                flash(session, n + " sample junctions added.", "ok");
            } else if ("delete".equals(action)) {
                junctionDAO.delete(Integer.parseInt(req.getParameter("id")));
                flash(session, "Junction deleted (its traffic records and alerts were removed too).", "ok");
            } else {
                if (name == null || name.isBlank() || location == null || location.isBlank()) {
                    flash(session, "Name and location are required.", "error");
                } else if ("update".equals(action)) {
                    junctionDAO.update(Integer.parseInt(req.getParameter("id")), name.trim(), location.trim());
                    flash(session, "Junction updated.", "ok");
                } else {
                    junctionDAO.add(name.trim(), location.trim());
                    flash(session, "Junction added.", "ok");
                }
            }
        } catch (NumberFormatException e) {
            flash(session, "Invalid junction id.", "error");
        } catch (SQLException e) {
            flash(session, "Database error: " + e.getMessage(), "error");
        }
        resp.sendRedirect(req.getContextPath() + "/junctions");
    }

    private void flash(HttpSession s, String msg, String type) {
        s.setAttribute("flash", msg);
        s.setAttribute("flashType", type);
    }
}
