import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { api } from "../services/api";
import "../styles/home.css";

function Home() {
  const { data: featuredProducts, isLoading } = useQuery({
    queryKey: ["featuredProducts"],
    queryFn: api.getProducts,
  });

  return (
    <div className="home">
      {/* Hero Section */}
      <section className="hero">
        <div className="container">
          <h1>
            Natural Beauty, <br />
            Naturally You
          </h1>
          <p>
            Discover our curated collection of organic skincare products,
            crafted with care to enhance your natural beauty while nurturing
            your skin
          </p>
          <Link to="/products" className="btn btn-primary">
            Explore Collection
          </Link>
        </div>
      </section>

      {/* Featured Products */}
      <section className="featured-products">
        <div className="container">
          <h2>Featured Products</h2>
          <div className="products-grid">
            {isLoading ? (
              <p className="loading">Loading products...</p>
            ) : (
              featuredProducts?.slice(0, 4).map((product) => (
                <div key={product.id} className="product-card">
                  <Link to={`/products/${product.id}`}>
                    <img src={product.image} alt={product.name} />
                    <div className="product-info">
                      <h3>{product.name}</h3>
                      <p className="price">RM {product.price.toFixed(2)}</p>
                    </div>
                  </Link>
                </div>
              ))
            )}
          </div>
        </div>
      </section>

      {/* About Section */}
      <section className="about">
        <div className="container">
          <h2>Why Choose Us</h2>
          <div className="features-grid">
            <div className="feature">
              <h3>Pure & Natural</h3>
              <p>
                Carefully selected natural ingredients that work in harmony with
                your skin
              </p>
            </div>
            <div className="feature">
              <h3>Cruelty Free</h3>
              <p>
                Committed to ethical beauty with no animal testing in our
                process
              </p>
            </div>
            <div className="feature">
              <h3>Eco-Friendly</h3>
              <p>
                Sustainable packaging and environmentally conscious practices
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;
