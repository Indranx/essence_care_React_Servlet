package com.essencecare.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.essencecare.models.CartItem;
import com.essencecare.models.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/essence-care/api/cart/*")
public class CartServlet extends HttpServlet {
    private static CartServlet instance;
    private static final Gson gson = new Gson();
    private final Map<String, List<CartItem>> userCarts = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        instance = this;
    }

    public static CartServlet getInstance() {
        return instance;
    }

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
        List<CartItem> cart = getUserCart(userEmail);
        out.print(gson.toJson(cart));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        System.out.println("CartServlet: Received POST request");
        System.out.println("CartServlet: Request URI: " + request.getRequestURI());

        HttpSession session = request.getSession(false);
        System.out.println("CartServlet: Session: " + (session != null ? session.getId() : "null"));
        
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("CartServlet: User not authenticated");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson("User not authenticated"));
            return;
        }

        String userEmail = (String) session.getAttribute("user");
        System.out.println("CartServlet: User email: " + userEmail);

        // Log request body
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        System.out.println("CartServlet: Request body: " + requestBody.toString());

        Map<String, Object> requestData = gson.fromJson(requestBody.toString(), new TypeToken<Map<String, Object>>(){}.getType());
        System.out.println("CartServlet: Parsed request data: " + gson.toJson(requestData));
        
        if (requestData == null || requestData.get("id") == null) {
            System.out.println("CartServlet: Invalid product data");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product data"));
            return;
        }

        try {
            // Get the complete product data from ProductServlet
            Long productId = ((Double) requestData.get("id")).longValue();
            System.out.println("CartServlet: Looking for product with ID: " + productId);
            
            Product product = ProductServlet.getInstance().getProductById(productId);
            System.out.println("CartServlet: Found product: " + (product != null ? gson.toJson(product) : "null"));
            
            if (product == null) {
                System.out.println("CartServlet: Product not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Product not found"));
                return;
            }

            List<CartItem> cart = userCarts.computeIfAbsent(userEmail, k -> new ArrayList<>());
            System.out.println("CartServlet: Current cart size: " + cart.size());
            System.out.println("CartServlet: Current cart items: " + gson.toJson(cart));
            
            // Get quantity from request, default to 1 if not provided
            int quantity = 1;
            if (requestData.get("quantity") != null) {
                quantity = ((Double) requestData.get("quantity")).intValue();
            }
            System.out.println("CartServlet: Adding quantity: " + quantity);

            // Check if product already exists in cart
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Add the new quantity to existing quantity
                int newQuantity = existingItem.get().getQuantity() + quantity;
                System.out.println("CartServlet: Updating existing item quantity from " + existingItem.get().getQuantity() + " to " + newQuantity);
                existingItem.get().setQuantity(newQuantity);
            } else {
                // Add new item with specified quantity
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cart.add(newItem);
                System.out.println("CartServlet: Added new item to cart");
            }

            System.out.println("CartServlet: Final cart size: " + cart.size());
            System.out.println("CartServlet: Final cart items: " + gson.toJson(cart));
            out.print(gson.toJson(cart));
        } catch (ClassCastException e) {
            System.out.println("CartServlet: Error - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product ID format"));
        } catch (Exception e) {
            System.out.println("CartServlet: Unexpected error - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("An unexpected error occurred"));
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
            Map<String, Object> requestBody = gson.fromJson(request.getReader(), new TypeToken<Map<String, Object>>(){}.getType());
            
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

    public List<CartItem> getUserCart(String userEmail) {
        return userCarts.getOrDefault(userEmail, new ArrayList<>());
    }

    public void clearUserCart(String userEmail) {
        userCarts.remove(userEmail);
    }
} 