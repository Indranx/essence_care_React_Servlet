import { Link } from "react-router-dom";
import "../styles/footer.css";

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <div className="footer-section">
            <h3>About Us</h3>
            <p>
              Discover natural skincare products that enhance your beauty while
              being kind to your skin and the environment.
            </p>
            <div className="social-links">
              <a href="#" className="social-link">
                Facebook
              </a>
              <a href="#" className="social-link">
                Instagram
              </a>
              <a href="#" className="social-link">
                Twitter
              </a>
            </div>
          </div>

          <div className="footer-section">
            <h3>Quick Links</h3>
            <nav className="footer-nav">
              <Link to="/">Home</Link>
              <Link to="/products">Products</Link>
              <Link to="/">About</Link>
              <Link to="/">Contact</Link>
            </nav>
          </div>

          <div className="footer-section">
            <h3>Contact Info</h3>
            <p>Email: info@essencecare.com</p>
            <p>Phone: +60 12-345 6789</p>
            <p>Address: 123 Beauty Street, 11900 Penang</p>
          </div>
        </div>

        <div className="footer-bottom">
          <p>&copy; 2024 Essence Care. All rights reserved.</p>
          <nav className="footer-bottom-nav">
            <Link to="/">Privacy Policy</Link>
            <Link to="/">Terms of Service</Link>
            <Link to="/">Shipping Info</Link>
          </nav>
        </div>
      </div>
    </footer>
  );
}
