package com.essencecare.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.essencecare.models.Product;
import com.essencecare.models.User;
import com.essencecare.models.Order;
import jakarta.servlet.ServletContext;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

public class JsonDataManager {
    private static final Gson gson = new Gson();
    private static ServletContext servletContext;

    public static void setServletContext(ServletContext context) {
        System.out.println("JsonDataManager: Setting ServletContext");
        servletContext = context;
    }

    // Product Management Methods
    public List<Product> getAllProducts() {
        System.out.println("JsonDataManager: Getting all products");
        return loadProducts();
    }

    public void addProduct(Product product) {
        System.out.println("JsonDataManager: Adding new product");
        List<Product> products = loadProducts();
        products.add(product);
        saveProducts(products);
    }

    public void updateProduct(Product updatedProduct) {
        System.out.println("JsonDataManager: Updating product with ID: " + updatedProduct.getId());
        List<Product> products = loadProducts();
        products = products.stream()
            .map(p -> p.getId().equals(updatedProduct.getId()) ? updatedProduct : p)
            .collect(Collectors.toList());
        saveProducts(products);
    }

    public void deleteProduct(String productId) {
        System.out.println("JsonDataManager: Deleting product with ID: " + productId);
        List<Product> products = loadProducts();
        products = products.stream()
            .filter(p -> !p.getId().toString().equals(productId))
            .collect(Collectors.toList());
        saveProducts(products);
    }

    public List<Product> searchProducts(String query) {
        System.out.println("JsonDataManager: Searching products with query: " + query);
        return loadProducts().stream()
            .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }

    private void saveProducts(List<Product> products) {
        System.out.println("JsonDataManager: Saving products");
        try {
            String filePath = servletContext.getRealPath("/WEB-INF/classes/data/products.json");
            System.out.println("JsonDataManager: Writing to file: " + filePath);
            
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(new ProductList(products), writer);
                System.out.println("JsonDataManager: Successfully saved products");
            }
        } catch (Exception e) {
            System.out.println("JsonDataManager: Error saving products - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Product> loadProducts() {
        System.out.println("JsonDataManager: Loading products");
        try {
            String filePath = "/WEB-INF/classes/data/products.json";
            System.out.println("JsonDataManager: Reading from file: " + filePath);
            
            if (servletContext == null) {
                System.out.println("JsonDataManager: ServletContext is null!");
                return new ArrayList<>();
            }

            InputStream is = servletContext.getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("JsonDataManager: Could not find products.json!");
                return new ArrayList<>();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                Type productListType = new TypeToken<ProductList>(){}.getType();
                ProductList productList = gson.fromJson(reader, productListType);
                System.out.println("JsonDataManager: Successfully loaded products: " + 
                    (productList != null && productList.products != null ? productList.products.size() : 0));
                return productList != null ? productList.products : new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("JsonDataManager: Error loading products - " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Helper class for JSON structure
    private static class ProductList {
        List<Product> products;

        ProductList(List<Product> products) {
            this.products = products;
        }
    }

    public static List<User> loadUsers() {
        System.out.println("JsonDataManager: Loading users");
        try {
            String filePath = "/WEB-INF/classes/data/users.json";
            System.out.println("JsonDataManager: Reading from file: " + filePath);
            
            if (servletContext == null) {
                System.out.println("JsonDataManager: ServletContext is null!");
                return new ArrayList<>();
            }

            InputStream is = servletContext.getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("JsonDataManager: Could not find users.json!");
                return new ArrayList<>();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String content = reader.lines().collect(Collectors.joining());
                List<User> users;
                
                // Try parsing as direct list first
                try {
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    users = gson.fromJson(content, listType);
                } catch (Exception e) {
                    // If that fails, try parsing as wrapped object
                    Type wrappedType = new TypeToken<Map<String, List<User>>>(){}.getType();
                    Map<String, List<User>> wrapped = gson.fromJson(content, wrappedType);
                    users = wrapped.get("users");
                }
                
                System.out.println("JsonDataManager: Successfully loaded users: " + 
                    (users != null ? users.size() : 0));
                return users != null ? users : new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("JsonDataManager: Error loading users - " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        System.out.println("JsonDataManager: Saving users");
        try {
            String filePath = servletContext.getRealPath("/WEB-INF/classes/data/users.json");
            System.out.println("JsonDataManager: Writing to file: " + filePath);
            
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(users, writer);
                System.out.println("JsonDataManager: Successfully saved users");
            }
        } catch (Exception e) {
            System.out.println("JsonDataManager: Error saving users - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Order> loadOrders() {
        System.out.println("JsonDataManager: Loading orders");
        try {
            String filePath = "/WEB-INF/classes/data/orders.json";
            System.out.println("JsonDataManager: Reading from file: " + filePath);
            
            if (servletContext == null) {
                System.out.println("JsonDataManager: ServletContext is null!");
                return new ArrayList<>();
            }

            InputStream is = servletContext.getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("JsonDataManager: Could not find orders.json!");
                return new ArrayList<>();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                Type orderListType = new TypeToken<List<Order>>(){}.getType();
                List<Order> orders = gson.fromJson(reader, orderListType);
                System.out.println("JsonDataManager: Successfully loaded orders: " + 
                    (orders != null ? orders.size() : 0));
                return orders != null ? orders : new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("JsonDataManager: Error loading orders - " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveOrders(List<Order> orders) {
        System.out.println("JsonDataManager: Saving orders");
        try {
            String filePath = servletContext.getRealPath("/WEB-INF/classes/data/orders.json");
            System.out.println("JsonDataManager: Writing to file: " + filePath);
            
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(orders, writer);
                System.out.println("JsonDataManager: Successfully saved orders");
            }
        } catch (Exception e) {
            System.out.println("JsonDataManager: Error saving orders - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Long getNextProductId() {
        List<Product> products = loadProducts();
        if (products.isEmpty()) {
            return 1L;
        }
        
        // Find the highest existing ID and add 1
        return products.stream()
                .mapToLong(Product::getId)
                .max()
                .orElse(0L) + 1;
    }

} 