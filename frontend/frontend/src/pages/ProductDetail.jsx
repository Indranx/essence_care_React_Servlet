import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import "../styles/product-detail.css";
import { useQuery } from "@tanstack/react-query";
import { api } from "../services/api";

function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addItem } = useCart();
  const [quantity, setQuantity] = useState(1);

  const { data: product, isLoading } = useQuery({
    queryKey: ["product", id],
    queryFn: () =>
      api
        .getProducts()
        .then((products) => products.find((p) => p.id === parseInt(id))),
  });

  const handleAddToCart = async () => {
    try {
      await addItem(product);
      //navigate("/cart");
    } catch (error) {
      console.error("Failed to add item to cart:", error);
    }
  };

  if (isLoading) {
    return (
      <div className="product-detail-page">
        <div className="container">
          <div className="loading">Loading product details...</div>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="product-detail-page">
        <div className="container">
          <div className="error">Product not found</div>
        </div>
      </div>
    );
  }

  return (
    <div className="product-detail-page">
      <div className="container">
        <div className="product-detail-content">
          <div className="product-image-section">
            <img src={product.image} alt={product.name} />
          </div>

          <div className="product-info-section">
            <h1>{product.name}</h1>
            <div className="price">RM {product.price.toFixed(2)}</div>

            <div className="description">
              <h2>Description</h2>
              <p>{product.description}</p>
            </div>

            <div className="quantity-section">
              <label>Quantity:</label>
              <div className="quantity-controls">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="quantity-btn"
                >
                  -
                </button>
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) =>
                    setQuantity(Math.max(1, parseInt(e.target.value) || 1))
                  }
                  min="1"
                />
                <button
                  onClick={() => setQuantity(quantity + 1)}
                  className="quantity-btn"
                >
                  +
                </button>
              </div>
            </div>

            <div className="product-actions">
              <button onClick={handleAddToCart} className="add-to-cart-btn">
                Add to Cart
              </button>
              <button
                onClick={() => navigate("/products")}
                className="continue-shopping-btn"
              >
                Continue Shopping
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;
