package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.CartItem;
import com.essencecare.models.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CartServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private static final Map<String, List<CartItem>> userCarts = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("User not authenticated"));
            return;
        }

        String userEmail = (String) session.getAttribute("user");
        List<CartItem> cart = userCarts.getOrDefault(userEmail, new ArrayList<>());
        out.print(gson.toJson(cart));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("User not authenticated"));
            return;
        }

        String userEmail = (String) session.getAttribute("user");
        Map<String, Object> requestData = gson.fromJson(request.getReader(), Map.class);
        
        if (requestData == null || requestData.get("id") == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product data"));
            return;
        }

        try {
            // Get the complete product data from ProductServlet
            Long productId = ((Double) requestData.get("id")).longValue();
            Product product = ProductServlet.getProductById(productId);
            
            if (product == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Product not found"));
                return;
            }

            List<CartItem> cart = userCarts.computeIfAbsent(userEmail, k -> new ArrayList<>());
            
            // Check if product already exists in cart
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Increment quantity if product already exists
                existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            } else {
                // Add new item if product doesn't exist
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(1);
                cart.add(newItem);
            }

            out.print(gson.toJson(cart));
        } catch (ClassCastException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product ID format"));
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("User not authenticated"));
            return;
        }

        String userEmail = (String) session.getAttribute("user");
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Product ID is required"));
            return;
        }

        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            Map<String, Object> requestBody = gson.fromJson(request.getReader(), Map.class);
            
            if (requestBody == null || requestBody.get("quantity") == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Quantity is required"));
                return;
            }

            int quantity = ((Double) requestBody.get("quantity")).intValue();

            List<CartItem> cart = userCarts.get(userEmail);
            if (cart != null) {
                Optional<CartItem> item = cart.stream()
                        .filter(i -> i.getProduct().getId().equals(productId))
                        .findFirst();

                if (item.isPresent()) {
                    if (quantity <= 0) {
                        cart.remove(item.get());
                    } else {
                        item.get().setQuantity(quantity);
                    }
                    out.print(gson.toJson(cart));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Item not found in cart"));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Cart not found"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product ID format"));
        } catch (ClassCastException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid quantity format"));
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("User not authenticated"));
            return;
        }

        String userEmail = (String) session.getAttribute("user");
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Product ID is required"));
            return;
        }

        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            List<CartItem> cart = userCarts.get(userEmail);
            
            if (cart != null) {
                boolean removed = cart.removeIf(item -> item.getProduct().getId().equals(productId));
                if (removed) {
                    out.print(gson.toJson(cart));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Item not found in cart"));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Cart not found"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product ID format"));
        }
        out.flush();
    }
} 