import React, { useEffect, useState } from "react";
import styled from "styled-components";
import { api } from "../../services/api";
import { Link } from "react-router-dom";

const Home = () => {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .getProducts()
      .then((products) => {
        // Get first 3 products as featured items
        setFeaturedProducts(products.slice(0, 3));
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching products:", error);
        setLoading(false);
      });
  }, []);

  return (
    <HomeContainer>
      {/* Hero Section */}
      <HeroSection>
        <HeroContent>
          <h1>Welcome to Essence & Care</h1>
          <p>Discover natural beauty with our premium skincare products</p>
          <ShopButton as={Link} to="/products">
            Shop Now
          </ShopButton>
        </HeroContent>
      </HeroSection>

      {/* Featured Products Section */}
      <FeaturedSection>
        <h2>Featured Products</h2>
        <ProductGrid>
          {loading ? (
            <LoadingText>Loading products...</LoadingText>
          ) : (
            featuredProducts.map((product) => (
              <ProductCard key={product.id}>
                <ProductImage src={product.imageUrl} alt={product.name} />
                <ProductInfo>
                  <h3>{product.name}</h3>
                  <p>${product.price}</p>
                  <ProductDescription>
                    {product.description.slice(0, 100)}...
                  </ProductDescription>
                  <ViewButton as={Link} to={`/products/${product.id}`}>
                    View Details
                  </ViewButton>
                </ProductInfo>
              </ProductCard>
            ))
          )}
        </ProductGrid>
      </FeaturedSection>

      {/* Categories Section */}
      <CategorySection>
        <h2>Our Categories</h2>
        <CategoryGrid>
          <CategoryCard>
            <img src="https://i.imgur.com/8kxwTe1.jpg" alt="Skincare" />
            <h3>Skincare</h3>
          </CategoryCard>
          <CategoryCard>
            <img src="https://i.imgur.com/QYq4Dhi.jpg" alt="Body Care" />
            <h3>Body Care</h3>
          </CategoryCard>
          <CategoryCard>
            <img src="https://i.imgur.com/vYqFvdh.jpg" alt="Face Care" />
            <h3>Face Care</h3>
          </CategoryCard>
        </CategoryGrid>
      </CategorySection>

      {/* About Section */}
      <AboutSection>
        <h2>Why Choose Us?</h2>
        <FeatureGrid>
          <FeatureCard>
            <h3>Natural Ingredients</h3>
            <p>
              All our products are made with carefully selected natural
              ingredients
            </p>
          </FeatureCard>
          <FeatureCard>
            <h3>Cruelty Free</h3>
            <p>We never test on animals and support ethical beauty</p>
          </FeatureCard>
          <FeatureCard>
            <h3>Eco-Friendly</h3>
            <p>Sustainable packaging and environmentally conscious practices</p>
          </FeatureCard>
        </FeatureGrid>
      </AboutSection>
    </HomeContainer>
  );
};

// Existing styled components...

// New styled components for products
const FeaturedSection = styled.section`
  padding: 2rem;
  text-align: center;
  margin-bottom: 4rem;

  h2 {
    margin-bottom: 2rem;
  }
`;

const ProductGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  padding: 0 1rem;
`;

const ProductCard = styled.div`
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-5px);
  }
`;

const ProductImage = styled.img`
  width: 100%;
  height: 200px;
  object-fit: cover;
`;

const ProductInfo = styled.div`
  padding: 1.5rem;

  h3 {
    margin-bottom: 0.5rem;
  }

  p {
    color: #4caf50;
    font-weight: bold;
    margin-bottom: 1rem;
  }
`;

const ProductDescription = styled.p`
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
`;

const ViewButton = styled(Link)`
  display: inline-block;
  background: #4caf50;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  transition: background 0.3s;

  &:hover {
    background: #388e3c;
  }
`;

const LoadingText = styled.p`
  grid-column: 1 / -1;
  text-align: center;
  font-size: 1.2rem;
  color: #666;
`;

// Keep all previous styled components...

export default Home;
