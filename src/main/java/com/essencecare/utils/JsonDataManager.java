package com.essencecare.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.essencecare.models.Product;
import com.essencecare.models.User;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonDataManager {
    private static final Gson gson = new Gson();
    private static final String RESOURCE_PATH = "/data/";
    private static final String DATA_DIR = "data";
    
    static {
        // Create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            System.out.println("Data directory created/exists at: " + new File(DATA_DIR).getAbsolutePath());
            
            // Copy initial data files from resources if they don't exist
            copyInitialDataIfNeeded("users.json");
            copyInitialDataIfNeeded("products.json");
            copyInitialDataIfNeeded("cart.json");
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void copyInitialDataIfNeeded(String filename) {
        File targetFile = new File(DATA_DIR, filename);
        if (!targetFile.exists()) {
            try (InputStream is = JsonDataManager.class.getResourceAsStream(RESOURCE_PATH + filename)) {
                if (is != null) {
                    Files.copy(is, targetFile.toPath());
                    System.out.println("Copied initial " + filename + " to: " + targetFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("Failed to copy initial " + filename + ": " + e.getMessage());
            }
        }
    }

    private static String getDataFilePath(String filename) {
        return DATA_DIR + File.separator + filename;
    }

    public static List<Product> loadProducts() {
        String filePath = getDataFilePath("products.json");
        System.out.println("Loading products from: " + filePath);
        try (Reader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, List<Product>>>(){}.getType();
            Map<String, List<Product>> data = gson.fromJson(reader, type);
            List<Product> productList = data.get("products");
            System.out.println("Loaded " + (productList != null ? productList.size() : 0) + " products");
            return productList != null ? productList : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<User> loadUsers() {
        String filePath = getDataFilePath("users.json");
        System.out.println("Loading users from: " + filePath);
        try (Reader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, List<User>>>(){}.getType();
            Map<String, List<User>> data = gson.fromJson(reader, type);
            List<User> userList = data.get("users");
            System.out.println("Loaded " + (userList != null ? userList.size() : 0) + " users");
            return userList != null ? userList : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        String filePath = getDataFilePath("users.json");
        System.out.println("Saving " + users.size() + " users to: " + filePath);
        try (Writer writer = new FileWriter(filePath)) {
            Map<String, List<User>> data = Map.of("users", users);
            gson.toJson(data, writer);
            System.out.println("Successfully saved users");
        } catch (Exception e) {
            System.out.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveProducts(List<Product> products) {
        String filePath = getDataFilePath("products.json");
        System.out.println("Saving " + products.size() + " products to: " + filePath);
        try (Writer writer = new FileWriter(filePath)) {
            Map<String, List<Product>> data = Map.of("products", products);
            gson.toJson(data, writer);
            System.out.println("Successfully saved products");
        } catch (Exception e) {
            System.out.println("Error saving products: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 