import { useRouteError } from "react-router-dom";

export default function ErrorBoundary() {
  const error = useRouteError();
  console.error("Route error:", error);

  return (
    <div className="error-container">
      <h1>Oops! Something went wrong</h1>
      <p>{error.message || "An unexpected error occurred"}</p>
      {process.env.NODE_ENV === "development" && <pre>{error.stack}</pre>}
    </div>
  );
}
