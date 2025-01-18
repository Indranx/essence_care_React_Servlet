import { useQuery } from "@tanstack/react-query";
import { api } from "../../services/api";
import "../../styles/admin.css";
import { useState } from "react";

function AdminDashboard() {
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const {
    data: products,
    isLoading: productsLoading,
    error: productsError,
  } = useQuery({
    queryKey: ["admin-products", page],
    queryFn: () => api.getAdminProducts({ page, pageSize }),
  });

  // Get low stock products (quantity < 50)
  const lowStockProducts =
    products?.products?.filter((product) => product.stockQuantity < 50) || [];

  if (productsLoading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  if (productsError) {
    return (
      <div className="error-message">
        {productsError?.message || "Error loading dashboard data"}
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>

      <div className="dashboard-stats">
        <div className="stat-card">
          <h3>Total Products</h3>
          <p>{products?.totalProducts || 0}</p>
        </div>
        <div className="stat-card warning">
          <h3>Low Stock Items</h3>
          <p>{lowStockProducts.length}</p>
        </div>
      </div>

      {/* Low Stock Alerts */}
      {lowStockProducts.length > 0 && (
        <div className="stock-alerts">
          <h2>⚠️ Low Stock Alerts</h2>
          <div className="alert-list">
            {lowStockProducts.map((product) => (
              <div key={product.id} className="alert-item">
                <span className="product-name">{product.name}</span>
                <span className="stock-count">
                  Only {product.stockQuantity} units left
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="dashboard-content">
        {/* Product Table */}
        <div className="dashboard-tables">
          <section className="products-table">
            <h2>Available Products</h2>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Stock</th>
                </tr>
              </thead>
              <tbody>
                {products?.products.map((product) => (
                  <tr
                    key={product.id}
                    className={product.stockQuantity < 50 ? "low-stock" : ""}
                  >
                    <td>{product.name}</td>
                    <td>{product.category}</td>
                    <td>RM {product.price.toFixed(2)}</td>
                    <td>{product.stockQuantity}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          <div className="pagination">
            <button
              className="pagination-btn"
              disabled={page === 1}
              onClick={() => setPage((p) => p - 1)}
            >
              Previous
            </button>
            <span className="page-info">
              Page {page} of {products?.totalPages || 1}
            </span>
            <button
              className="pagination-btn"
              disabled={page === products?.totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;
