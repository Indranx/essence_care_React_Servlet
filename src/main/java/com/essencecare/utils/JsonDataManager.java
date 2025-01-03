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

public class JsonDataManager {
    private static final Gson gson = new Gson();
    private static ServletContext servletContext;

    public static void setServletContext(ServletContext context) {
        System.out.println("JsonDataManager: Setting ServletContext");
        servletContext = context;
    }

    public static List<Product> loadProducts() {
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

    private static class ProductList {
        List<Product> products;
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
                Type userListType = new TypeToken<List<User>>(){}.getType();
                List<User> users = gson.fromJson(reader, userListType);
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
} 