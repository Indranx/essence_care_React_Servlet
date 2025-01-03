package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.Order;
import com.essencecare.models.CartItem;
import com.essencecare.utils.JsonDataManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import com.google.gson.reflect.TypeToken;

@WebServlet("/essence-care/api/orders/*")
public class OrderServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private List<Order> orders;
    private long nextOrderId = 1;

    @Override
    public void init() throws ServletException {
        super.init();
        loadOrders();
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
        List<Order> userOrders = orders.stream()
                .filter(order -> order.getUserEmail().equals(userEmail))
                .toList();

        out.print(gson.toJson(userOrders));
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
        
        try {
            // Get cart items from CartServlet
            List<CartItem> cartItems = CartServlet.getInstance().getUserCart(userEmail);
            if (cartItems == null || cartItems.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Cart is empty"));
                return;
            }

            // Calculate total
            double total = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            // Create new order
            Order order = new Order();
            order.setId(nextOrderId++);
            order.setUserEmail(userEmail);
            order.setItems(new ArrayList<>(cartItems));
            order.setTotal(total);
            order.setStatus("Pending");
            order.setOrderDate(new Date());

            // Add shipping address from request
            Map<String, String> requestData = gson.fromJson(request.getReader(), new TypeToken<Map<String, String>>(){}.getType());
            order.setShippingAddress(requestData.get("shippingAddress"));

            // Save order
            orders.add(order);
            saveOrders(orders);

            // Clear the cart
            CartServlet.getInstance().clearUserCart(userEmail);

            out.print(gson.toJson(order));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error creating order: " + e.getMessage()));
        }
        out.flush();
    }

    private void loadOrders() {
        orders = JsonDataManager.loadOrders();
        if (!orders.isEmpty()) {
            nextOrderId = orders.stream()
                    .mapToLong(Order::getId)
                    .max()
                    .getAsLong() + 1;
        }
    }

    private void saveOrders(List<Order> orders) {
        JsonDataManager.saveOrders(orders);
    }
} 