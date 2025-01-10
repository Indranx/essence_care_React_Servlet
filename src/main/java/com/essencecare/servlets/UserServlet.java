package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.User;
import com.essencecare.utils.JsonDataManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/essence-care/api/auth/*")
public class UserServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private List<User> users;

    @Override
    public void init() throws ServletException {
        super.init();
        JsonDataManager.setServletContext(getServletContext());
        loadUsers();
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
        PrintWriter out = response.getWriter();
        try {
            // Log request body
            BufferedReader reader = request.getReader();
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            System.out.println("Login request body: " + requestBody.toString());
            
            User credentials = gson.fromJson(requestBody.toString(), User.class);
            System.out.println("Login attempt for email: " + credentials.getEmail());
            
            // Debug current users
            System.out.println("Current users in system: " + users.size());
            users.forEach(u -> System.out.println("User: " + u.getEmail()));

            Optional<User> userOpt = users.stream()
                    .filter(u -> u.getEmail().equals(credentials.getEmail()))
                    .findFirst();

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("User found, checking password");
                
                if (user.getPassword().equals(credentials.getPassword())) {
                    System.out.println("Password matches");
                    
                    // Invalidate any existing session
                    HttpSession existingSession = request.getSession(false);
                    if (existingSession != null) {
                        existingSession.invalidate();
                    }

                    // Create new session
                    HttpSession session = request.getSession(true);
                    session.setAttribute("user", user.getEmail());
                    session.setAttribute("role", user.getRole());
                    session.setMaxInactiveInterval(30 * 60); // 30 minutes

                    // Create a response object without sensitive data
                    User safeUser = new User();
                    safeUser.setEmail(user.getEmail());
                    safeUser.setFullName(user.getFullName());
                    safeUser.setAddress(user.getAddress());
                    safeUser.setRole(user.getRole());

                    out.print(gson.toJson(safeUser));
                    System.out.println("Login successful. Role set to: " + user.getRole());
                } else {
                    System.out.println("Password mismatch");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print(gson.toJson("Invalid credentials"));
                }
            } else {
                System.out.println("User not found");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson("Invalid credentials"));
            }
        } catch (Exception e) {
            System.out.println("Error in login: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error during login: " + e.getMessage()));
        }
    }

    private void handleRegistration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try {
            // Add debug logging
            System.out.println("Handling registration request");
            
            // Log the request body
            BufferedReader reader = request.getReader();
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            System.out.println("Request body: " + requestBody.toString());
            
            User newUser = gson.fromJson(requestBody.toString(), User.class);
            System.out.println("Parsed user: " + gson.toJson(newUser));

            // Add default role for regular users
            newUser.setRole("USER");

            if (users == null) {
                users = new ArrayList<>();
                System.out.println("Created new users list");
            }

            if (users.stream().anyMatch(u -> u.getEmail().equals(newUser.getEmail()))) {
                System.out.println("Email already exists: " + newUser.getEmail());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson("Email already exists"));
                return;
            }

            // Add the new user
            users.add(newUser);
            System.out.println("Added new user. Total users: " + users.size());
            
            try {
                saveUsers(users);
                System.out.println("Saved users successfully");
            } catch (Exception e) {
                System.out.println("Error saving users: " + e.getMessage());
                e.printStackTrace();
            }

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
            System.out.println("Registration completed successfully");
            
        } catch (Exception e) {
            System.out.println("Error in registration: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error registering user: " + e.getMessage()));
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session != null) {
            session.invalidate();
        }

        out.print(gson.toJson(Map.of("message", "Successfully logged out")));
    }

    private void loadUsers() {
        users = JsonDataManager.loadUsers();
    }

    private void saveUsers(List<User> userList) {
        JsonDataManager.saveUsers(userList);
        this.users = userList;  // Update local cache
    }
} 