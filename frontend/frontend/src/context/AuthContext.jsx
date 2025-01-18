import { createContext, useContext, useState } from "react";
import { api } from "../services/api";
import { toast } from "react-toastify";

const AuthContext = createContext(null);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem("user");
      return savedUser ? JSON.parse(savedUser) : null;
    } catch (error) {
      console.error("Error parsing user from localStorage:", error);
      localStorage.removeItem("user");
      return null;
    }
  });

  const login = async (credentials) => {
    try {
      const data = await api.login(credentials);
      if (!data) throw new Error("No data received from login");
      setUser(data);
      localStorage.setItem("user", JSON.stringify(data));
      toast.success("Welcome back!", {
        position: "bottom-right",
        autoClose: 1500,
      });
      return data;
    } catch (error) {
      const message = error.message || "Invalid credentials";
      toast.error(message, {
        position: "bottom-right",
        autoClose: 2000,
      });
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const data = await api.register(userData);
      if (!data) throw new Error("No data received from registration");
      setUser(data);
      localStorage.setItem("user", JSON.stringify(data));
      toast.success("Welcome to Essence Care!", {
        position: "bottom-right",
        autoClose: 1500,
      });
      return data;
    } catch (error) {
      const message = error.message || "Registration failed";
      toast.error(message, {
        position: "bottom-right",
        autoClose: 2000,
      });
      throw error;
    }
  };

  const logout = async () => {
    try {
      await api.logout();
      setUser(null);
      localStorage.removeItem("user");
      toast.success("Successfully logged out", {
        position: "bottom-right",
        autoClose: 1500,
      });
    } catch (error) {
      console.error("Logout error:", error);
      setUser(null);
      localStorage.removeItem("user");
    }
  };

  const value = {
    user,
    login,
    register,
    logout,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
