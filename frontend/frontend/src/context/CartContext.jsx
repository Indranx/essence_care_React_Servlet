import { createContext, useContext, useEffect, useState } from "react";
import { useAuth } from "./AuthContext";
import { api } from "../services/api";
import { toast } from "react-hot-toast";
import { useNavigate } from "react-router-dom";

const CartContext = createContext();

export function CartProvider({ children }) {
  const [cart, setCart] = useState([]);
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && user) {
      loadCart();
    } else {
      setCart([]);
    }
  }, [isAuthenticated, user]);

  const loadCart = async () => {
    try {
      const data = await api.getCart();
      if (!data) {
        console.error("Received null or undefined cart data");
        setCart([]);
        return;
      }
      setCart(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Failed to load cart:", error);
      setCart([]);
    }
  };

  const addItem = async (product, quantity = 1) => {
    try {
      if (!isAuthenticated) {
        console.log(
          "CartContext: User not authenticated, redirecting to login"
        );
        navigate("/login");
        return;
      }

      if (!product || !product.id) {
        console.error("CartContext: Invalid product data:", product);
        return;
      }

      console.log("CartContext: Current cart state:", cart);
      console.log("CartContext: Adding to cart:", { product, quantity });

      try {
        const updatedCart = await api.addToCart(product, quantity);
        console.log("CartContext: Response from addToCart:", updatedCart);

        if (!updatedCart) {
          console.error(
            "CartContext: Received null or undefined cart data after adding item"
          );
          return;
        }

        const newCart = Array.isArray(updatedCart) ? updatedCart : [];
        console.log("CartContext: Setting new cart state:", newCart);
        setCart(newCart);

        // Verify cart state was updated
        setTimeout(() => {
          console.log("CartContext: Verifying cart state after update:", cart);
        }, 0);

        toast.success("Added to cart", { duration: 1500 });
      } catch (apiError) {
        console.error("CartContext: API error:", apiError);
        if (apiError.response?.status === 401) {
          console.log("CartContext: Unauthorized, redirecting to login");
          navigate("/login");
        } else {
          toast.error("Failed to add item to cart", { duration: 2000 });
        }
      }
    } catch (error) {
      console.error("CartContext: Unexpected error:", error);
      toast.error("An unexpected error occurred", { duration: 2000 });
    }
  };

  const updateItem = async (productId, quantity) => {
    try {
      if (!isAuthenticated) {
        navigate("/login");
        return;
      }

      if (!productId) {
        console.error("Invalid product ID:", productId);
        return;
      }

      const updatedCart = await api.updateCartItem(productId, quantity);
      if (!updatedCart) {
        console.error(
          "Received null or undefined cart data after updating item"
        );
        return;
      }

      setCart(Array.isArray(updatedCart) ? updatedCart : []);
    } catch (error) {
      console.error("Failed to update item:", error);
      if (error.response?.status === 401) {
        navigate("/login");
      }
    }
  };

  const removeItem = async (productId) => {
    try {
      if (!isAuthenticated) {
        navigate("/login");
        return;
      }

      if (!productId) {
        console.error("Invalid product ID:", productId);
        return;
      }

      const updatedCart = await api.removeFromCart(productId);
      if (!updatedCart) {
        console.error(
          "Received null or undefined cart data after removing item"
        );
        return;
      }

      setCart(Array.isArray(updatedCart) ? updatedCart : []);
    } catch (error) {
      console.error("Failed to remove item:", error);
      if (error.response?.status === 401) {
        navigate("/login");
      }
    }
  };

  const syncCartWithBackend = async () => {
    try {
      const serverCart = await api.getCart();
      setCart(serverCart);
    } catch (error) {
      console.error("Failed to sync cart:", error);
    }
  };

  return (
    <CartContext.Provider value={{ cart, addItem, updateItem, removeItem }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
}
