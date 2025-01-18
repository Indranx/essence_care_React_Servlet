import { useCart } from "../context/CartContext";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/cart.css";

export default function Cart() {
  const { cart, updateItem, removeItem } = useCart();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const handleQuantityChange = async (itemId, newQuantity) => {
    try {
      if (newQuantity < 1) {
        await removeItem(itemId);
      } else {
        await updateItem(itemId, newQuantity);
      }
    } catch (error) {
      console.error("Error updating quantity:", error);
    }
  };

  const handleRemove = async (itemId) => {
    try {
      await removeItem(itemId);
    } catch (error) {
      console.error("Error removing item:", error);
    }
  };

  const calculateTotal = () => {
    if (!Array.isArray(cart)) return 0;
    return cart.reduce(
      (total, item) => total + item.product.price * item.quantity,
      0
    );
  };

  if (!isAuthenticated) {
    return (
      <div className="cart-page">
        <div className="container">
          <div className="cart-empty">
            <h2>Please log in to view your cart</h2>
            <Link to="/login" className="btn-primary">
              Log In
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!Array.isArray(cart) || cart.length === 0) {
    return (
      <div className="cart-page">
        <div className="container">
          <div className="cart-empty">
            <h2>Your cart is empty</h2>
            <Link to="/products" className="btn-primary">
              Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <div className="container">
        <h1>Shopping Cart</h1>
        <div className="cart-content">
          <div className="cart-items">
            {cart.map((item) => (
              <div key={item.product.id} className="cart-item">
                <div className="item-image">
                  <img src={item.product.image} alt={item.product.name} />
                </div>
                <div className="item-details">
                  <h3>{item.product.name}</h3>
                  <p className="price">
                    RM {item.product.price.toFixed(2)} each
                  </p>
                </div>
                <div className="item-quantity">
                  <p className="quantity-label">Quantity:</p>
                  <div className="quantity-controls">
                    <button
                      className="quantity-btn"
                      onClick={() =>
                        handleQuantityChange(item.product.id, item.quantity - 1)
                      }
                    >
                      -
                    </button>
                    <span>{item.quantity}</span>
                    <button
                      className="quantity-btn"
                      onClick={() =>
                        handleQuantityChange(item.product.id, item.quantity + 1)
                      }
                    >
                      +
                    </button>
                  </div>
                </div>
                <div className="item-total">
                  <p className="total-label">Subtotal:</p>
                  <p className="total-amount">
                    RM {(item.product.price * item.quantity).toFixed(2)}
                  </p>
                </div>
                <button
                  className="remove-btn"
                  onClick={() => handleRemove(item.product.id)}
                >
                  ×
                </button>
              </div>
            ))}
          </div>
          <div className="cart-summary">
            <h2>Order Summary</h2>
            <div className="summary-row">
              <span>Subtotal</span>
              <span>RM {calculateTotal().toFixed(2)}</span>
            </div>
            <div className="summary-row">
              <span>Shipping</span>
              <span>Free</span>
            </div>
            <div className="summary-total">
              <span>Total</span>
              <span>RM {calculateTotal().toFixed(2)}</span>
            </div>
            <button
              className="btn-primary checkout-btn"
              onClick={() => navigate("/checkout")}
            >
              Proceed to Checkout
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
