import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { useState } from "react";
import { api } from "../services/api";
import { useCart } from "../context/CartContext";
import "../styles/products.css";

function Products() {
  const [selectedCategory, setSelectedCategory] = useState("all");
  const { addItem } = useCart();
  const { data: products, isLoading } = useQuery({
    queryKey: ["products"],
    queryFn: api.getProducts,
  });

  const categories = ["all", "skincare", "haircare", "bodycare"];

  const filteredProducts =
    selectedCategory === "all"
      ? products
      : products?.filter((product) => product.category === selectedCategory);

  return (
    <div className="products">
      <div className="container">
        <div className="products-header">
          <h1>Our Products</h1>
          <div className="category-filters">
            {categories.map((category) => (
              <button
                key={category}
                className={`category-filter ${
                  selectedCategory === category ? "active" : ""
                }`}
                onClick={() => setSelectedCategory(category)}
              >
                {category.charAt(0).toUpperCase() + category.slice(1)}
              </button>
            ))}
          </div>
        </div>

        <div className="products-grid">
          {isLoading ? (
            <p className="loading">Loading products...</p>
          ) : (
            filteredProducts?.map((product) => (
              <div key={product.id} className="product-card">
                <img src={product.image} alt={product.name} />
                <div className="product-info">
                  <h3>{product.name}</h3>
                  <p className="price">RM {product.price.toFixed(2)}</p>
                  <div className="product-actions">
                    <Link to={`/products/${product.id}`} className="btn-view">
                      View Details
                    </Link>
                    <button
                      className="btn-cart"
                      onClick={() => addItem(product)}
                    >
                      Add to Cart
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default Products;
