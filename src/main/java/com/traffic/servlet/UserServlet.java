package com.traffic.servlet;

import com.traffic.dao.UserDAO;
import com.traffic.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Admin-only user management: list users, change role, delete. */
@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    private boolean isAdmin(HttpServletRequest req) {
        User u = (User) req.getSession().getAttribute("user");
        return u != null && u.canManageJunctions();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        try {
            req.setAttribute("users", userDAO.getAll());
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        User me = (User) session.getAttribute("user");
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            if (id == me.getUserId()) {
                flash(session, "You cannot change or delete your own account here.", "error");
            } else if ("delete".equals(req.getParameter("action"))) {
                userDAO.delete(id);
                flash(session, "User deleted.", "ok");
            } else {
                userDAO.setRole(id, req.getParameter("role"));
                flash(session, "Role updated.", "ok");
            }
        } catch (NumberFormatException | SQLException e) {
            flash(session, "Could not update user: " + e.getMessage(), "error");
        }
        resp.sendRedirect(req.getContextPath() + "/users");
    }

    private void flash(HttpSession s, String msg, String type) {
        s.setAttribute("flash", msg);
        s.setAttribute("flashType", type);
    }
}
