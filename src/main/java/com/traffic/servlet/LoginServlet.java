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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Please enter both username and password.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        try {
            User user = userDAO.authenticateOrRegister(username.trim(), password);
            if (user == null) {
                req.setAttribute("error", "Wrong password for this username.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();           // avoid session fixation
            req.getSession(true).setAttribute("user", user);
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage()
                    + " (check db.properties and that schema.sql was run)");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
