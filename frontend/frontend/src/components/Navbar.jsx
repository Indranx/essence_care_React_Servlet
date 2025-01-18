import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import "../styles/navbar.css";

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  // Debug log for cart state
  console.log("Current cart in Navbar:", cart);

  const cartItemCount = Array.isArray(cart)
    ? cart.reduce((total, item) => total + item.quantity, 0)
    : 0;

  // Debug log for cart count
  console.log("Cart item count:", cartItemCount);

  const handleLogout = async () => {
    try {
      await logout();
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  return (
    <header className="navbar">
      <div className="container">
        <div className="navbar-content">
          <a className="logo" href="/">
            Essence&Care
          </a>

          <nav className="nav-links">
            <a className="nav-link" href="/">
              Home
            </a>
            <a className="nav-link" href="/products">
              Products
            </a>
          </nav>

          <div className="nav-actions">
            {isAuthenticated && user?.role !== "ADMIN" && (
              <a className="cart-link" href="/cart">
                Cart
                <span className="cart-badge">{cartItemCount || 0}</span>
              </a>
            )}
            {isAuthenticated && user ? (
              <div className="user-menu">
                <a className="profile-link" href="/profile">
                  <span className="avatar-small">
                    {user.fullName
                      ? user.fullName.charAt(0).toUpperCase()
                      : "?"}
                  </span>
                  <span className="username">{user.fullName || "User"}</span>
                </a>
                {isAuthenticated && user?.role === "ADMIN" && (
                  <Link to="/admin" className="admin-link">
                    Admin Dashboard
                  </Link>
                )}
                <button
                  className="btn btn-secondary logout-btn"
                  onClick={handleLogout}
                >
                  Logout
                </button>
              </div>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary">
                  Login
                </Link>
                <Link to="/register" className="btn btn-primary">
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
