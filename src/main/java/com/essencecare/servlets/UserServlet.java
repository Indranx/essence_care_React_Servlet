package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.User;
import com.essencecare.utils.JsonDataManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private static List<User> users;

    @Override
    public void init() throws ServletException {
        super.init();
        JsonDataManager.setServletContext(getServletContext());
        users = JsonDataManager.loadUsers();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid endpoint"));
            return;
        }

        try {
            switch (pathInfo) {
                case "/login":
                    handleLogin(request, response);
                    break;
                case "/register":
                    handleRegistration(request, response);
                    break;
                case "/logout":
                    handleLogout(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Endpoint not found"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Server error: " + e.getMessage()));
        }
        out.flush();
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User credentials = gson.fromJson(request.getReader(), User.class);
        PrintWriter out = response.getWriter();

        Optional<User> userOpt = users.stream()
                .filter(u -> u.getEmail().equals(credentials.getEmail()))
                .findFirst();

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(credentials.getPassword())) {
            User user = userOpt.get();
            // Invalidate any existing session
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }

            // Create new session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user.getEmail());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Create a response object without sensitive data
            User safeUser = new User();
            safeUser.setEmail(user.getEmail());
            safeUser.setFullName(user.getFullName());
            safeUser.setAddress(user.getAddress());

            out.print(gson.toJson(safeUser));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("Invalid credentials"));
        }
    }

    private void handleRegistration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User newUser = gson.fromJson(request.getReader(), User.class);
        PrintWriter out = response.getWriter();

        if (users.stream().anyMatch(u -> u.getEmail().equals(newUser.getEmail()))) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(gson.toJson("Email already exists"));
            return;
        }

        // Add the new user
        users.add(newUser);
        JsonDataManager.saveUsers(users);

        // Create new session
        HttpSession session = request.getSession(true);
        session.setAttribute("user", newUser.getEmail());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        response.setStatus(HttpServletResponse.SC_CREATED);

        // Create a response object without sensitive data
        User safeUser = new User();
        safeUser.setEmail(newUser.getEmail());
        safeUser.setFullName(newUser.getFullName());
        safeUser.setAddress(newUser.getAddress());

        out.print(gson.toJson(safeUser));
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session != null) {
            session.invalidate();
        }

        out.print(gson.toJson(Map.of("message", "Successfully logged out")));
    }
} 