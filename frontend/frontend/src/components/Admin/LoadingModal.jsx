import { useState, useEffect } from "react";

function LoadingModal({ isOpen, onComplete }) {
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    if (!isOpen) return;

    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          onComplete();
          return 5; // Reset for next time
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isOpen, onComplete]);

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="loading-modal">
        <div className="spinner"></div>
        <h2>Updating Products</h2>
        <p>Please wait while we update the products...</p>
        <p className="countdown">Refreshing in {countdown} seconds</p>
      </div>
    </div>
  );
}

export default LoadingModal;
