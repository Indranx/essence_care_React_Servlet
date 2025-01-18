import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useOrders } from "../context/OrderContext";
import "../styles/profile.css";

function Profile() {
  const { user, isAuthenticated, logout } = useAuth();
  const { orders } = useOrders();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("profile");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
    }
  }, [isAuthenticated, navigate]);

  const handleLogout = async () => {
    try {
      await logout();
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  // Filter orders for current user
  const userOrders = orders.filter((order) => order.userEmail === user?.email);

  if (!user) {
    return null;
  }

  return (
    <div className="profile-page">
      <div className="container">
        <div className="profile-content">
          <aside className="profile-sidebar">
            <div className="user-info">
              <div className="avatar">
                {user.fullName ? user.fullName.charAt(0).toUpperCase() : "?"}
              </div>
              <h2>{user.fullName || "User"}</h2>
              <p>{user.email || "No email provided"}</p>
            </div>

            <nav className="profile-nav">
              <button
                className={`nav-item ${
                  activeTab === "profile" ? "active" : ""
                }`}
                onClick={() => setActiveTab("profile")}
              >
                Profile Information
              </button>
              <button
                className={`nav-item ${activeTab === "orders" ? "active" : ""}`}
                onClick={() => setActiveTab("orders")}
              >
                Order History
              </button>
              <button className="nav-item logout" onClick={handleLogout}>
                Logout
              </button>
            </nav>
          </aside>

          <main className="profile-main">
            {activeTab === "profile" && (
              <div className="profile-section">
                <h1>Profile Information</h1>
                <div className="info-card">
                  <div className="info-row">
                    <span className="label">Name</span>
                    <span className="value">
                      {user.fullName || "Not provided"}
                    </span>
                  </div>
                  <div className="info-row">
                    <span className="label">Email</span>
                    <span className="value">
                      {user.email || "Not provided"}
                    </span>
                  </div>
                  <div className="info-row">
                    <span className="label">Address</span>
                    <span className="value">
                      {user.address || "Not provided"}
                    </span>
                  </div>
                </div>
              </div>
            )}

            {activeTab === "orders" && (
              <div className="profile-section">
                <h1>Order History</h1>
                {userOrders.length === 0 ? (
                  <div className="empty-state">No orders found</div>
                ) : (
                  <div className="orders-list">
                    {userOrders.map((order) => (
                      <div key={order.id} className="order-card">
                        <div className="order-header">
                          <div>
                            <h3>Order #{order.id}</h3>
                            <p className="order-date">
                              {new Date(order.orderDate).toLocaleDateString()}
                            </p>
                          </div>
                          <span
                            className={`order-status ${order.status.toLowerCase()}`}
                          >
                            {order.status}
                          </span>
                        </div>
                        <div className="order-items">
                          {order.items.map((item) => (
                            <div key={item.product.id} className="order-item">
                              <span>{item.product.name}</span>
                              <span>x{item.quantity}</span>
                              <span>
                                RM{" "}
                                {(item.product.price * item.quantity).toFixed(
                                  2
                                )}
                              </span>
                            </div>
                          ))}
                        </div>
                        <div className="order-info">
                          <div className="shipping-address">
                            <span className="info-label">
                              Shipping Address:
                            </span>
                            <p>{order.shippingAddress}</p>
                          </div>
                          <div className="payment-method">
                            <span className="info-label">Payment Method:</span>
                            <p>
                              {order.paymentMethod === "cod"
                                ? "Cash on Delivery"
                                : order.paymentMethod === "bank"
                                ? "Bank Transfer"
                                : "E-Wallet"}
                            </p>
                          </div>
                        </div>
                        <div className="order-footer">
                          <span>Total:</span>
                          <span className="order-total">
                            RM {order.total.toFixed(2)}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}

export default Profile;
