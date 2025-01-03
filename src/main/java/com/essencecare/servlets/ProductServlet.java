package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.Product;
import com.essencecare.utils.JsonDataManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/essence-care/api/products/*")
public class ProductServlet extends HttpServlet {
    private static ProductServlet instance;
    private static final Gson gson = new Gson();
    private List<Product> products;

    @Override
    public void init() throws ServletException {
        super.init();
        instance = this;
        System.out.println("ProductServlet initialized");
        System.out.println("Context path: " + getServletContext().getContextPath());
        JsonDataManager.setServletContext(getServletContext());
        loadProducts();
    }

    public static ProductServlet getInstance() {
        return instance;
    }

    public Product getProductById(Long id) {
        System.out.println("ProductServlet: Getting product by ID: " + id);
        Product product = products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        System.out.println("ProductServlet: Found product: " + (product != null ? gson.toJson(product) : "null"));
        return product;
    }

    private void loadProducts() {
        System.out.println("ProductServlet: Loading products from JsonDataManager");
        products = JsonDataManager.loadProducts();
        if (products != null) {
            System.out.println("ProductServlet: Successfully loaded " + products.size() + " products");
            products.forEach(p -> System.out.println("Product: " + gson.toJson(p)));
        } else {
            System.out.println("ProductServlet: Failed to load products, list is null");
            products = new ArrayList<>();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // Add debug logging
            System.out.println("Loading all products...");
            System.out.println("Number of products: " + products.size());
            out.print(gson.toJson(products));
            return;
        }

        try {
            // Extract product ID from path
            String idStr = pathInfo.substring(1);
            long id = Long.parseLong(idStr);

            // Find product by ID
            Product product = products.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (product != null) {
                out.print(gson.toJson(product));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Product not found"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product ID format"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Server error: " + e.getMessage()));
        }
        out.flush();
    }
} 