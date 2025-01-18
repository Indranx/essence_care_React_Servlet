const BASE_URL = "http://localhost:8080/essence-care/api";

// Products
export const getProducts = async () => {
  try {
    console.log("Fetching products from:", `${BASE_URL}/products`);
    const response = await fetch(`${BASE_URL}/products`, {
      credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("API Error Response:", errorText);
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Products data:", data);
    return data;
  } catch (error) {
    console.error("Error fetching products:", error);
    throw error;
  }
};

export const getProduct = (id) =>
  fetch(`${BASE_URL}/products/${id}`, {
    credentials: "include",
  }).then((res) => res.json());

// Cart
export const getCart = () =>
  fetch(`${BASE_URL}/cart`, {
    credentials: "include",
  }).then(async (res) => {
    if (!res.ok) {
      const error = await res.text();
      throw new Error(error);
    }
    return res.json();
  });

export const addToCart = (product, quantity = 1) =>
  fetch(`${BASE_URL}/cart`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ id: product.id, quantity }),
  }).then(async (res) => {
    if (!res.ok) {
      const error = await res.text();
      throw new Error(error);
    }
    return res.json();
  });

export const updateCartItem = (itemId, quantity) =>
  fetch(`${BASE_URL}/cart/${itemId}`, {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ quantity }),
  }).then(async (res) => {
    if (!res.ok) {
      const error = await res.text();
      throw new Error(error);
    }
    return res.json();
  });

export const removeFromCart = (itemId) =>
  fetch(`${BASE_URL}/cart/${itemId}`, {
    method: "DELETE",
    credentials: "include",
  }).then((res) => res.json());

// Auth
export const login = (credentials) =>
  fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  }).then((res) => {
    if (!res.ok) {
      throw new Error("Invalid credentials");
    }
    return res.json();
  });

export const register = (userData) =>
  fetch(`${BASE_URL}/auth/register`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  }).then((res) => {
    if (!res.ok) {
      throw new Error("Registration failed");
    }
    return res.json();
  });

export const logout = () =>
  fetch(`${BASE_URL}/auth/logout`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
  }).then((res) => {
    if (!res.ok) {
      throw new Error("Logout failed");
    }
    return res.json();
  });

// Export all functions as an object
export const api = {
  getProducts,
  getProduct,
  getCart,
  addToCart,
  updateCartItem,
  removeFromCart,
  login,
  register,
  logout,
  getAdminProducts: async (params = {}) => {
    try {
      const {
        page = 1,
        sortBy = "name",
        sortOrder = "asc",
        category = "all",
      } = params;

      const queryParams = new URLSearchParams({
        page,
        sortBy,
        sortOrder,
        ...(category !== "all" && { category }),
      });

      const response = await fetch(
        `${BASE_URL}/admin/products?${queryParams}`,
        {
          credentials: "include",
        }
      );

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "Failed to fetch products");
      }

      return response.json();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },
  createProduct: async (productData) => {
    const response = await fetch(`${BASE_URL}/admin/products`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(productData),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Failed to create product");
    }

    return response.json();
  },
  updateProduct: async ({ id, ...productData }) => {
    const response = await fetch(`${BASE_URL}/admin/products/${id}`, {
      method: "PUT",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(productData),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Failed to update product");
    }

    return response.json();
  },
  deleteProduct: async (productId) => {
    const response = await fetch(`${BASE_URL}/admin/products/${productId}`, {
      method: "DELETE",
      credentials: "include",
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Failed to delete product");
    }

    return true;
  },
  getAdminActivities: async () => {
    const response = await fetch(`${BASE_URL}/admin/activities`, {
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to fetch activities");
    }

    return response.json();
  },
};
