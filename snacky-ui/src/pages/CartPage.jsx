import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Trash2, Plus, Minus, ShoppingBag, X, AlertCircle } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Input, Card, Spinner, Modal } from '@/components/common';
import { fetchCart, updateCartItemQuantity, removeFromCart, getEligibleCoupons, applyCoupon, removeCoupon } from '@/store/thunks/cartThunks';
import { showToast } from '@/utils/toast';

const CartPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { items, loading, appliedCouponCode, discountAmount, subtotal } = useSelector(state => state.cart);
  const { user } = useSelector(state => state.auth);
  const { productImages } = useSelector(state => state.products);
  const { eligibleCoupons, loading: couponLoading } = useSelector(state => state.cart);

  const [showCouponModal, setShowCouponModal] = useState(false);
  const [selectedCoupon, setSelectedCoupon] = useState(null);
  const [applyingCoupon, setApplyingCoupon] = useState(false);

  // Fetch cart and eligible coupons on mount
  useEffect(() => {
    if (user) {
      dispatch(fetchCart())
        .unwrap()
        .catch(err => {
          showToast(err || 'Failed to load cart', 'error');
        });
      
      // Fetch eligible coupons for the cart
      dispatch(getEligibleCoupons())
        .unwrap()
        .then(data => {
          console.log('Eligible coupons received:', data);
        })
        .catch(err => {
          console.error('Failed to fetch eligible coupons', err);
        });
    } else {
      navigate('/login');
    }
  }, [user, dispatch, navigate]);

  // Refetch eligible coupons whenever cart items change (quantity, add, remove operations)
  // This ensures coupons eligibility is always recalculated based on current cart total
  useEffect(() => {
    if (user && items && items.length > 0) {
      dispatch(getEligibleCoupons())
        .unwrap()
        .then(data => {
          console.log('Eligible coupons refreshed after cart change:', data);
        })
        .catch(err => {
          console.error('Failed to refresh eligible coupons', err);
        });
    }
  }, [items, user, dispatch]);

  const handleQuantityChange = (itemId, newQuantity) => {
    if (newQuantity < 1) return;
    console.log('Updating item', itemId, 'to quantity', newQuantity);

    dispatch(updateCartItemQuantity({
      cartItemId: itemId,
      quantity: newQuantity,
    }))
      .unwrap()
      .catch(err => {
        showToast(err || 'Failed to update quantity', 'error');
      });
  };

  const handleRemoveItem = (itemId) => {
    dispatch(removeFromCart(itemId))
      .unwrap()
      .then(() => {
        showToast('Item removed from cart', 'success');
      })
      .catch(err => {
        showToast(err || 'Failed to remove item', 'error');
      });
  };

  const handleApplyCoupon = () => {
    if (!selectedCoupon) {
      showToast('Please select a coupon', 'error');
      return;
    }

    setApplyingCoupon(true);
    dispatch(applyCoupon(selectedCoupon.id))
      .unwrap()
      .then(() => {
        showToast(`Coupon "${selectedCoupon.code}" applied successfully!`, 'success');
        setShowCouponModal(false);
        setSelectedCoupon(null);
      })
      .catch(err => {
        showToast(err || 'Failed to apply coupon', 'error');
      })
      .finally(() => setApplyingCoupon(false));
  };

  const handleRemoveCoupon = () => {
    dispatch(removeCoupon())
      .unwrap()
      .then(() => {
        showToast('Coupon removed', 'success');
        setSelectedCoupon(null);
      })
      .catch(err => {
        showToast(err || 'Failed to remove coupon', 'error');
      });
  };

  const handleContinueShopping = () => {
    navigate('/products');
  };

  const handleCheckout = () => {
    if (items.length === 0) {
      showToast('Your cart is empty', 'error');
      return;
    }
    navigate('/checkout');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <Spinner size="lg" />
        </div>
        <Footer />
      </div>
    );
  }

  const safeCartItems = items || [];
  const discountPercent = subtotal > 0 ? Math.round((discountAmount / subtotal) * 100) : 0;
  const finalTotal = subtotal - discountAmount;

  return (
    <div className="min-h-screen bg-linear-to-b from-slate-50 to-white">
      <Header />

      <div className="max-w-6xl mx-auto px-4 py-12">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-slate-900 mb-2">Shopping Cart</h1>
          <p className="text-slate-600">
            {safeCartItems.length} {safeCartItems.length === 1 ? 'item' : 'items'} in your cart
          </p>
        </div>

        {safeCartItems.length === 0 ? (
          // Empty Cart
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-12 text-center">
            <ShoppingBag className="w-16 h-16 text-slate-300 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-slate-900 mb-2">Your cart is empty</h2>
            <p className="text-slate-600 mb-8">
              Add some delicious snacks to get started!
            </p>
            <Button onClick={handleContinueShopping} variant="primary">
              Continue Shopping
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-4">
              {safeCartItems.map(item => (
                <Card key={item.id} className="p-4 lg:p-6">
                  <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
                    {/* Product Image */}
                    <div className="bg-slate-100 rounded-lg overflow-hidden h-32 sm:h-auto">
                      <img
                        src={
                          (productImages[item.productId] && productImages[item.productId].length > 0)
                            ? productImages[item.productId][0]
                            : item.image || '/placeholder.jpg'
                        }
                        alt={item.name}
                        className="w-full h-full object-cover"
                      />
                    </div>

                    {/* Product Info */}
                    <div className="sm:col-span-2">
                      <h3 className="font-bold text-slate-900 mb-1 text-lg">{item.productName}</h3>
                      <p className="text-sm text-slate-600 mb-4">{item.category}</p>
                      <div className="flex items-center gap-2">
                        <span className="text-2xl font-bold text-orange-600">
                          ₹{item.currentPrice}
                        </span>
                        {item.originalPrice && (
                          <span className="text-sm text-slate-400 line-through">
                            ₹{item.originalPrice}
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Quantity & Remove */}
                    <div className="flex flex-col justify-between sm:items-end">
                      {/* Quantity Selector */}
                      <div className="flex items-center gap-3 bg-slate-100 rounded-lg w-fit p-2 mb-4">
                        <button
                          onClick={() => handleQuantityChange(item.cartItemId, item.quantity - 1)}
                          className="p-1 hover:bg-slate-200 rounded transition"
                        >
                          <Minus className="w-4 h-4" />
                        </button>
                        <span className="w-8 text-center font-semibold text-slate-900">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => handleQuantityChange(item.cartItemId, item.quantity + 1)}
                          className="p-1 hover:bg-slate-200 rounded transition"
                        >
                          <Plus className="w-4 h-4" />
                        </button>
                      </div>

                      {/* Remove Button */}
                      <button
                        onClick={() => handleRemoveItem(item.cartItemId)}
                        className="text-red-600 hover:text-red-700 p-2 hover:bg-red-50 rounded-lg transition"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>

                  {/* Item Total */}
                  <div className="mt-4 pt-4 border-t border-slate-200 text-right">
                    <p className="text-sm text-slate-600 mb-1">Item Total</p>
                    <p className="text-xl font-bold text-slate-900">
                      ₹{(item.currentPrice * item.quantity).toLocaleString()}
                    </p>
                  </div>
                </Card>
              ))}

              {/* Continue Shopping */}
              <Button
                onClick={handleContinueShopping}
                variant="outline"
                className="w-full"
              >
                 Continue Shopping
              </Button>
            </div>

            {/* Order Summary */}
            <div className="lg:col-span-1">
              <div className="sticky top-4 bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                {/* Coupon Section */}
                <div className="bg-linear-to-r from-orange-50 to-orange-100 p-4 border-b border-orange-200">
                  {appliedCouponCode && parseFloat(discountAmount) > 0 ? (
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm text-orange-700 font-semibold">✓ Coupon Applied</p>
                        <p className="text-xs text-orange-600 mt-1">{appliedCouponCode}</p>
                      </div>
                      <button
                        onClick={handleRemoveCoupon}
                        className="p-1 text-orange-600 hover:bg-orange-200 rounded-full transition"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => setShowCouponModal(true)}
                      className="text-sm text-orange-700 font-semibold hover:text-orange-800 transition"
                    >
                      + Apply Coupon Code
                    </button>
                  )}
                </div>

                {/* Summary */}
                <div className="p-6 space-y-4">
                  <h3 className="font-bold text-lg text-slate-900 mb-6">Order Summary</h3>

                  <div className="space-y-3">
                    <div className="flex justify-between text-slate-600">
                      <span>Subtotal</span>
                      <span className="font-semibold">₹{parseFloat(subtotal).toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                    </div>

                    {parseFloat(discountAmount) > 0 && (
                      <div className="flex justify-between text-green-600">
                        <span>Discount {discountPercent > 0 && `(${discountPercent}%)`}</span>
                        <span className="font-semibold">-₹{parseFloat(discountAmount).toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                      </div>
                    )}

                    <div className="flex justify-between text-slate-600">
                      <span>Shipping</span>
                      <span className="font-semibold text-green-600">FREE</span>
                    </div>
                  </div>

                  <div className="border-t border-slate-200 pt-4">
                    <div className="flex justify-between mb-6">
                      <span className="font-bold text-lg text-slate-900">Total</span>
                      <span className="text-2xl font-bold text-orange-600">
                        ₹{parseFloat(finalTotal).toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                      </span>
                    </div>

                    <Button
                      onClick={handleCheckout}
                      variant="cta"
                      className="w-full mb-3"
                    >
                      Proceed to Checkout
                    </Button>

                    <Button
                      onClick={handleContinueShopping}
                      variant="outline"
                      className="w-full"
                    >
                      Continue Shopping
                    </Button>
                  </div>
                </div>

                {/* Info */}
                <div className="bg-blue-50 border-t border-slate-200 p-4 flex gap-3 text-sm text-blue-700">
                  <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                  <p>Free shipping on all orders across India</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Coupon Modal */}
      <Modal
        isOpen={showCouponModal}
        onClose={() => setShowCouponModal(false)}
        title="Select a Coupon"
      >
        <div className="space-y-4">
          {couponLoading ? (
            <div className="flex items-center justify-center py-8">
              <Spinner size="sm" />
              <span className="ml-2 text-slate-600">Loading coupons...</span>
            </div>
          ) : eligibleCoupons && eligibleCoupons.length > 0 ? (
            <div className="space-y-3">
              <h4 className="font-semibold text-slate-900 mb-3">Available Coupons for You:</h4>
              {eligibleCoupons.map(coupon => (
                <button
                  key={coupon.id}
                  onClick={() => {
                    console.log('Selected coupon:', coupon);
                    setSelectedCoupon(coupon);
                  }}
                  className={`w-full p-4 rounded-lg border-2 transition ${
                    selectedCoupon?.id === coupon.id
                      ? 'border-orange-500 bg-orange-50'
                      : 'border-slate-200 hover:border-orange-300'
                  }`}
                >
                  <div className="flex items-center justify-between text-left">
                    <div>
                      <p className="font-bold text-slate-900">{coupon.code}</p>
                      <p className="text-sm text-slate-600 mt-1">{coupon.description || 'Get discount on this order'}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-green-600 font-bold text-lg">
                        {coupon.type === 'PERCENTAGE' ? `${coupon.discountValue}%` : `₹${coupon.discountValue}`}
                        {coupon.type === 'PERCENTAGE' ? ' off' : ''}
                      </p>
                      {coupon.minOrderAmount && (
                        <p className="text-xs text-slate-500 mt-1">Min ₹{coupon.minOrderAmount}</p>
                      )}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-slate-600">No eligible coupons available</p>
            </div>
          )}

          <div className="flex gap-3 pt-4 border-t border-slate-200">
            <Button
              onClick={handleApplyCoupon}
              variant="primary"
              className="flex-1"
              disabled={applyingCoupon || !selectedCoupon}
            >
              {applyingCoupon ? 'Applying...' : 'Apply Selected Coupon'}
            </Button>
            <Button
              onClick={() => {
                setShowCouponModal(false);
                setSelectedCoupon(null);
              }}
              variant="outline"
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </div>
      </Modal>

      <Footer />
    </div>
  );
};

export default CartPage;
