package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class UserServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void init() throws ServletException {
        // Add a sample user
        User sampleUser = new User();
        sampleUser.setEmail("test@example.com");
        sampleUser.setPassword("test123");
        sampleUser.setFullName("Test User");
        sampleUser.setAddress("123 Test St");
        users.put(sampleUser.getEmail(), sampleUser);
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
        User user = users.get(credentials.getEmail());
        PrintWriter out = response.getWriter();

        if (user != null && user.getPassword().equals(credentials.getPassword())) {
            // Invalidate any existing session
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }

            // Create new session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user.getEmail());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Create a response object without the password
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("email", user.getEmail());
            responseData.put("fullName", user.getFullName());
            responseData.put("address", user.getAddress());

            out.print(gson.toJson(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("Invalid credentials"));
        }
    }

    private void handleRegistration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User newUser = gson.fromJson(request.getReader(), User.class);
        PrintWriter out = response.getWriter();

        if (users.containsKey(newUser.getEmail())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(gson.toJson("Email already exists"));
            return;
        }

        users.put(newUser.getEmail(), newUser);

        // Create new session
        HttpSession session = request.getSession(true);
        session.setAttribute("user", newUser.getEmail());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        response.setStatus(HttpServletResponse.SC_CREATED);

        // Create a response object without the password
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("email", newUser.getEmail());
        responseData.put("fullName", newUser.getFullName());
        responseData.put("address", newUser.getAddress());

        out.print(gson.toJson(responseData));
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session != null) {
            session.invalidate();
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "Successfully logged out");
        out.print(gson.toJson(responseData));
    }
} 