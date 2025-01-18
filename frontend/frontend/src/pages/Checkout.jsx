import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { useOrders } from "../context/OrderContext";
import "../styles/checkout.css";

function OrderSuccessModal({ orderId, onClose }) {
  return (
    <div className="success-modal-overlay">
      <div className="success-modal">
        <div className="success-icon">✓</div>
        <h2>Order Placed Successfully!</h2>
        <p>Your order #{orderId} has been confirmed.</p>
        <div className="success-actions">
          <button className="view-orders-btn" onClick={onClose}>
            View Orders
          </button>
          <button
            className="continue-shopping-btn"
            onClick={() => (window.location.href = "/products")}
          >
            Continue Shopping
          </button>
        </div>
      </div>
    </div>
  );
}

function Checkout() {
  const { cart, removeItem } = useCart();
  const { user } = useAuth();
  const { addOrder } = useOrders();
  const navigate = useNavigate();
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState("cod");
  const [useCustomAddress, setUseCustomAddress] = useState(false);
  const [shippingAddress, setShippingAddress] = useState(user?.address || "");
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [currentOrderId, setCurrentOrderId] = useState(null);

  // Calculate total
  const total = cart.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsProcessing(true);

    const orderId = Date.now();
    // Create order object
    const order = {
      id: orderId,
      items: cart,
      total: total,
      status: "Processing",
      orderDate: new Date().toISOString(),
      shippingAddress: useCustomAddress ? shippingAddress : user.address,
      userEmail: user.email,
      paymentMethod: paymentMethod,
    };

    // Simulate payment processing
    setTimeout(async () => {
      // Add order to history
      addOrder(order);

      // Clear cart
      for (const item of cart) {
        await removeItem(item.product.id);
      }

      setIsProcessing(false);
      setCurrentOrderId(orderId);
      setShowSuccessModal(true);
    }, 2000);
  };

  const handleSuccessModalClose = () => {
    setShowSuccessModal(false);
    navigate("/profile");
  };

  if (!user) {
    navigate("/login");
    return null;
  }

  return (
    <div className="checkout-page">
      <div className="container">
        <div className="checkout-content">
          <div className="order-summary">
            <h2>Order Summary</h2>
            <div className="cart-items">
              {cart.map((item) => (
                <div key={item.product.id} className="cart-item">
                  <img
                    src={item.product.image}
                    alt={item.product.name}
                    className="item-image"
                  />
                  <div className="item-details">
                    <h3>{item.product.name}</h3>
                    <p>Quantity: {item.quantity}</p>
                    <p className="item-price">
                      RM {(item.product.price * item.quantity).toFixed(2)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
            <div className="order-total">
              <h3>Total: RM {total.toFixed(2)}</h3>
            </div>
          </div>

          <div className="checkout-form">
            <h2>Shipping Information</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" value={user.fullName} disabled />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" value={user.email} disabled />
              </div>
              <div className="form-group">
                <label>Shipping Address</label>
                <div className="address-options">
                  <label className="address-toggle">
                    <input
                      type="checkbox"
                      checked={useCustomAddress}
                      onChange={(e) => {
                        setUseCustomAddress(e.target.checked);
                        if (!e.target.checked) {
                          setShippingAddress(user.address);
                        }
                      }}
                    />
                    Use different shipping address
                  </label>
                </div>
                <textarea
                  value={useCustomAddress ? shippingAddress : user.address}
                  onChange={(e) => setShippingAddress(e.target.value)}
                  disabled={!useCustomAddress}
                  rows="3"
                  placeholder={useCustomAddress ? "Enter shipping address" : ""}
                />
              </div>
              <div className="form-group">
                <label>Payment Method</label>
                <select
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  className="payment-select"
                >
                  <option value="cod">Cash on Delivery (COD)</option>
                  <option value="bank">Bank Transfer</option>
                  <option value="ewallet">
                    E-Wallet (Touch n Go, Grab Pay)
                  </option>
                </select>
                {paymentMethod === "bank" && (
                  <div className="payment-info">
                    <p>Bank Transfer Details:</p>
                    <p>Bank: Maybank</p>
                    <p>Account: 1234 5678 9012</p>
                    <p>Name: Essence Care Sdn Bhd</p>
                    <p className="payment-note">
                      Please transfer RM {total.toFixed(2)}
                    </p>
                  </div>
                )}
                {paymentMethod === "ewallet" && (
                  <div className="payment-info">
                    <p>Scan QR code to pay RM {total.toFixed(2)}:</p>
                    <div className="qr-container">
                      <img
                        src="/tng_qr.png"
                        alt="Touch n Go QR Code"
                        className="qr-code"
                      />
                    </div>
                    <p className="supported-wallets">Supported e-wallets:</p>
                    <p>- Touch n Go eWallet</p>
                    <p>- Grab Pay</p>
                  </div>
                )}
              </div>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isProcessing || cart.length === 0}
              >
                {isProcessing ? "Processing..." : "Place Order"}
              </button>
            </form>
          </div>
        </div>
      </div>
      {showSuccessModal && (
        <OrderSuccessModal
          orderId={currentOrderId}
          onClose={handleSuccessModalClose}
        />
      )}
    </div>
  );
}

export default Checkout;
