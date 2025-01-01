package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ProductServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private static final List<Product> products = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize with some sample products
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Hydrating Face Cream");
        p1.setDescription("A rich, moisturizing face cream for all skin types");
        p1.setPrice(29.99);
        p1.setCategory("skincare");
        p1.setImage("https://images.unsplash.com/photo-1556228720-195a672e8a03?w=500&auto=format");
        products.add(p1);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Nourishing Shampoo");
        p2.setDescription("Gentle, sulfate-free shampoo for daily use");
        p2.setPrice(19.99);
        p2.setCategory("haircare");
        p2.setImage("https://images.unsplash.com/photo-1585232351009-aa87416fca90?w=500&auto=format");
        products.add(p2);

        Product p3 = new Product();
        p3.setId(3L);
        p3.setName("Body Lotion");
        p3.setDescription("Ultra-moisturizing body lotion with natural ingredients");
        p3.setPrice(24.99);
        p3.setCategory("bodycare");
        p3.setImage("https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=500&auto=format");
        products.add(p3);
    }

    public static Product getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // Return all products
            out.print(gson.toJson(products));
        } else {
            try {
                // Get product by ID
                Long productId = Long.parseLong(pathInfo.substring(1));
                Product product = getProductById(productId);
                if (product != null) {
                    out.print(gson.toJson(product));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Product not found"));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Invalid product ID format"));
            }
        }
        out.flush();
    }
} 