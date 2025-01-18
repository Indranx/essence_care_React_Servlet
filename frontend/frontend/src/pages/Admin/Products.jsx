import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../../services/api";
import ProductForm from "../../components/Admin/ProductForm";
import "../../styles/adminProducts.css";
import ConfirmModal from "../../components/Admin/ConfirmModal";
import LoadingModal from "../../components/Admin/LoadingModal";

function Products() {
  const [page, setPage] = useState(1);
  const [sortBy, setSortBy] = useState("name");
  const [category, setCategory] = useState("all");
  const [showForm, setShowForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [deleteId, setDeleteId] = useState(null);
  const [isUpdating, setIsUpdating] = useState(false);

  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["admin-products", page, sortBy, category],
    queryFn: () =>
      api.getAdminProducts({
        page,
        sortBy: "name",
        sortOrder: "asc",
        category,
      }),
  });

  const sortProducts = (products) => {
    if (!products) return [];

    return [...products].sort((a, b) => {
      switch (sortBy) {
        case "name":
          return a.name.localeCompare(b.name);
        case "price":
          return a.price - b.price;
        case "category":
          return a.category.localeCompare(b.category);
        default:
          return a.name.localeCompare(b.name);
      }
    });
  };

  const sortedProducts = sortProducts(data?.products);

  const handleUpdateComplete = () => {
    setIsUpdating(false);
    window.location.reload(); // Force refresh the page
  };

  const createMutation = useMutation({
    mutationFn: api.createProduct,
  });

  const deleteMutation = useMutation({
    mutationFn: api.deleteProduct,
  });

  const handleDeleteClick = (productId) => {
    setDeleteId(productId);
  };

  const handleDeleteConfirm = async () => {
    try {
      await api.deleteProduct(deleteId);
      setDeleteId(null);
      setIsUpdating(true); // Show loading modal immediately
      queryClient.invalidateQueries(["admin-products"]);
    } catch (error) {
      console.error("Failed to delete product:", error);
      setDeleteId(null);
    }
  };

  const handleEdit = (product) => {
    setEditingProduct(product);
    setShowForm(true);
  };

  if (isLoading) return <div className="loading">Loading products...</div>;

  return (
    <div className="admin-products">
      <div className="products-header">
        <h1>Manage Products</h1>
        <button
          className="btn-add"
          onClick={() => {
            setEditingProduct(null);
            setShowForm(true);
          }}
        >
          Add New Product
        </button>
      </div>

      <div className="filters">
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="name">Sort by Name</option>
          <option value="price">Sort by Price</option>
          <option value="category">Sort by Category</option>
        </select>

        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="all">All Categories</option>
          <option value="skincare">Skincare</option>
          <option value="haircare">Haircare</option>
          <option value="bodycare">Bodycare</option>
        </select>
      </div>

      <div className="products-table">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {sortedProducts.map((product) => (
              <tr key={product.id}>
                <td>{product.name}</td>
                <td>{product.category}</td>
                <td>RM {parseFloat(product.price).toFixed(2)}</td>
                <td>
                  {product.stockQuantity !== null
                    ? parseInt(product.stockQuantity, 10)
                    : 0}
                </td>
                <td>
                  <button
                    className="btn-edit"
                    onClick={() => handleEdit(product)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn-delete"
                    onClick={() => handleDeleteClick(product.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <button
          className="pagination-btn"
          disabled={page === 1}
          onClick={() => setPage((p) => p - 1)}
        >
          Previous
        </button>
        <span>
          Page {page} of {data?.totalPages}
        </span>
        <button
          className="pagination-btn"
          disabled={page === data?.totalPages}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>

      {showForm && (
        <ProductForm
          product={editingProduct}
          onClose={() => {
            setShowForm(false);
            setEditingProduct(null);
          }}
          onSubmit={() => {
            setShowForm(false);
            setEditingProduct(null);
            setIsUpdating(true); // Show loading modal immediately
            queryClient.invalidateQueries(["admin-products"]);
          }}
        />
      )}

      <ConfirmModal
        isOpen={deleteId !== null}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDeleteConfirm}
        title="Delete Product"
        message="Are you sure you want to delete this product? This action cannot be undone."
      />

      <LoadingModal isOpen={isUpdating} onComplete={handleUpdateComplete} />
    </div>
  );
}

export default Products;
