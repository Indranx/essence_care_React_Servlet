package com.essencecare.servlets;

import com.google.gson.Gson;
import com.essencecare.models.Product;
import com.essencecare.utils.JsonDataManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ProductServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private static List<Product> products;

    @Override
    public void init() throws ServletException {
        super.init();
        products = JsonDataManager.loadProducts();
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