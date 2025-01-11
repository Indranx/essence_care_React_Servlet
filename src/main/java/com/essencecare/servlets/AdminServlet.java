package com.essencecare.servlets;

import com.essencecare.models.Product;
import com.essencecare.utils.JsonDataManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

// Jakarta EE imports
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final JsonDataManager jsonDataManager = new JsonDataManager();

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("AdminServlet initialized");
        System.out.println("Context path: " + getServletContext().getContextPath());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        System.out.println("AdminServlet: Received " + req.getMethod() + " request to " + req.getRequestURI());
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.equals("/products")) {
                // Get pagination and sorting parameters
                int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
                int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");
                String sortBy = request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "name";
                String sortOrder = request.getParameter("sortOrder") != null ? request.getParameter("sortOrder") : "asc";
                String category = request.getParameter("category");

                List<Product> products = jsonDataManager.getAllProducts();

                // Apply category filter
                if (category != null && !category.equals("all")) {
                    products = products.stream()
                        .filter(p -> p.getCategory().equals(category))
                        .collect(Collectors.toList());
                }

                // Apply sorting
                Comparator<Product> comparator = switch (sortBy) {
                    case "id" -> Comparator.comparingLong(p -> Long.parseLong(p.getId().toString()));
                    case "price" -> Comparator.comparing(Product::getPrice);
                    case "category" -> Comparator.comparing(Product::getCategory);
                    case "name" -> Comparator.comparing(Product::getName);
                    default -> Comparator.comparingLong(p -> Long.parseLong(p.getId().toString())); // Default to ID sorting
                };

                // Always sort in ascending order
                products = products.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());

                // Apply pagination
                int totalProducts = products.size();
                int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
                int start = (page - 1) * pageSize;
                int end = Math.min(start + pageSize, totalProducts);

                List<Product> paginatedProducts = products.subList(start, end);

                JsonObject jsonResponse = new JsonObject();
                jsonResponse.add("products", gson.toJsonTree(paginatedProducts));
                jsonResponse.addProperty("totalPages", totalPages);
                jsonResponse.addProperty("currentPage", page);
                jsonResponse.addProperty("totalProducts", totalProducts);

                out.print(gson.toJson(jsonResponse));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Invalid endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.equals("/products")) {
                Product newProduct = gson.fromJson(request.getReader(), Product.class);
                
                // Validate product
                String validationError = validateProduct(newProduct);
                if (validationError != null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson("Validation error: " + validationError));
                    return;
                }

                // Set sequential ID
                newProduct.setId(jsonDataManager.getNextProductId());
                
                // Save product
                jsonDataManager.addProduct(newProduct);
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(newProduct));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Invalid endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/products/")) {
                Long productId = Long.parseLong(pathInfo.substring("/products/".length()));
                Product updatedProduct = gson.fromJson(request.getReader(), Product.class);
                updatedProduct.setId(productId);

                // Validate product
                String validationError = validateProduct(updatedProduct);
                if (validationError != null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson("Validation error: " + validationError));
                    return;
                }

                jsonDataManager.updateProduct(updatedProduct);
                out.print(gson.toJson(updatedProduct));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Invalid endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/products/")) {
                String productId = pathInfo.substring("/products/".length());
                jsonDataManager.deleteProduct(productId);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Invalid endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error: " + e.getMessage()));
        }
    }

    private String validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return "Name is required";
        }
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            return "Description is required";
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "Price must be greater than 0";
        }
        if (product.getStockQuantity() < 0) {
            return "Stock quantity cannot be negative";
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            return "Category is required";
        }
        return null;
    }

} 