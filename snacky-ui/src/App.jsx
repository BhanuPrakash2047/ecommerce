import './index.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import store from './store';
import ErrorBoundary from './components/common/ErrorBoundary';
import {
  HomePage,
  ProductsPage,
  ProductDetailPage,
  CartPage,
  CheckoutPage,
  LoginPage,
  SignupPage,
  OrderTrackingPage,
  OrderHistoryPage,
  OrderDetailPage,
  UserProfilePage,
  NotificationsPage,
  OAuthSuccessPage,
  OAuthErrorPage,
} from './pages';

function App() {
  return (
    <ErrorBoundary>
      <Provider store={store}>
        <Router>
          <Routes>
          {/* Home & Products */}
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:productId" element={<ProductDetailPage />} />

          {/* Shopping */}
          <Route path="/cart" element={<CartPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />

          {/* Auth */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/oauth2/success" element={<OAuthSuccessPage />} />
          <Route path="/oauth2/error" element={<OAuthErrorPage />} />

          {/* Orders & Profile */}
          <Route path="/orders" element={<OrderHistoryPage />} />
          <Route path="/orders/:orderId" element={<OrderDetailPage />} />
          <Route path="/profile" element={<UserProfilePage />} />
          <Route path="/notifications" element={<NotificationsPage />} />

          {/* 404 - Not Found */}
          <Route
            path="*"
            element={
              <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                  <h1 className="text-4xl font-bold mb-4">404 - Page Not Found</h1>
                  <a href="/" className="text-orange-600 hover:text-orange-700">
                    Go back to home
                  </a>
                </div>
              </div>
            }
          />
        </Routes>
      </Router>
    </Provider>
    </ErrorBoundary>
  );
}

export default App;
