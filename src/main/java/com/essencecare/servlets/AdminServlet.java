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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
        //String pathInfo = request.getPathInfo();
        
        try {
            // Parse pagination parameters
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");
            String sortBy = request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "name";
            String sortOrder = request.getParameter("sortOrder") != null ? request.getParameter("sortOrder") : "asc";
            String filterCategory = request.getParameter("category");
            String searchQuery = request.getParameter("search");

            List<Product> products = jsonDataManager.getAllProducts();

            // Apply filters
            if (filterCategory != null && !filterCategory.isEmpty()) {
                products = products.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(filterCategory))
                    .collect(Collectors.toList());
            }

            // Apply search
            if (searchQuery != null && !searchQuery.isEmpty()) {
                products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                p.getDescription().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
            }

            // Apply sorting
            products = sortProducts(products, sortBy, sortOrder);

            // Apply pagination
            int totalProducts = products.size();
            int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalProducts);

            List<Product> paginatedProducts = products.subList(start, end);

            // Create response object
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("products", gson.toJsonTree(paginatedProducts));
            jsonResponse.addProperty("totalPages", totalPages);
            jsonResponse.addProperty("currentPage", page);
            jsonResponse.addProperty("totalProducts", totalProducts);

            out.print(gson.toJson(jsonResponse));

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
            Product newProduct = gson.fromJson(request.getReader(), Product.class);
            
            // Validate product
            String validationError = validateProduct(newProduct);
            if (validationError != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Validation error: " + validationError));
                return;
            }

            // Generate sequential ID
            newProduct.setId(jsonDataManager.getNextProductId());
            
            // Save product
            jsonDataManager.addProduct(newProduct);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(newProduct));
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error adding product: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            // Get the ID from the path
            String pathId = request.getPathInfo().substring(1);
            Long productId = Long.parseLong(pathId);
            
            // Parse the updated product from request body
            Product updatedProduct = gson.fromJson(request.getReader(), Product.class);
            
            // Force the ID to match the path parameter
            updatedProduct.setId(productId);
            
            // Validate product
            String validationError = validateProduct(updatedProduct);
            if (validationError != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("Validation error: " + validationError));
                return;
            }

            // Check if product exists
            List<Product> products = jsonDataManager.getAllProducts();
            boolean productExists = products.stream()
                    .anyMatch(p -> p.getId().equals(productId));

            if (!productExists) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Product not found with ID: " + productId));
                return;
            }

            // Update product
            jsonDataManager.updateProduct(updatedProduct);
            
            out.print(gson.toJson(updatedProduct));
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("Invalid product ID format"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error updating product: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String productId = request.getPathInfo().substring(1);
            System.out.println("AdminServlet: Attempting to delete product with ID: " + productId);
            
            // Delete product directly without cart manipulation
            jsonDataManager.deleteProduct(productId);
            
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            System.out.println("AdminServlet: Successfully deleted product with ID: " + productId);
            
        } catch (Exception e) {
            System.out.println("AdminServlet: Error deleting product - " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Error deleting product: " + e.getMessage()));
        }
    }

    private String validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return "Name is required";
        }
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            return "Description is required";
        }
        if (product.getPrice() <= 0) {
            return "Price must be greater than 0";
        }
        if (product.getStockQuantity() < 0) {
            return "Stock quantity cannot be negative";
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            return "Category is required";
        }
        if (product.getImage() == null || product.getImage().trim().isEmpty()) {
            return "Image URL is required";
        }
        return null;
    }

    private List<Product> sortProducts(List<Product> products, String sortBy, String sortOrder) {
        Comparator<Product> comparator = switch (sortBy.toLowerCase()) {
            case "price" -> Comparator.comparing(Product::getPrice);
            case "stock" -> Comparator.comparing(Product::getStockQuantity);
            default -> Comparator.comparing(Product::getName);
        };

        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        return products.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }
} 