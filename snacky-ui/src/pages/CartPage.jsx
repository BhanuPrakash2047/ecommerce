import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Trash2, Plus, Minus, ShoppingBag, X, AlertCircle, Gift, Zap, Truck } from 'lucide-react';
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
        .catch(err => {
          console.error('Failed to refresh eligible coupons', err);
        });
    }
  }, [items, user, dispatch]);

  const handleQuantityChange = (itemId, newQuantity) => {
    if (newQuantity < 1) return;

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
      <div className="min-h-screen bg-linear-to-b from-orange-50 via-white to-emerald-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <div className="relative w-20 h-20 mb-6 mx-auto">
              <div className="absolute inset-0 bg-linear-to-r from-orange-400 to-red-400 rounded-full animate-spin opacity-75"></div>
              <div className="absolute inset-2 bg-white rounded-full flex items-center justify-center text-2xl">🛒</div>
            </div>
            <p className="text-slate-600 text-lg font-semibold">Loading your cart...</p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  const safeCartItems = items || [];
  const discountPercent = subtotal > 0 ? Math.round((discountAmount / subtotal) * 100) : 0;
  const finalTotal = subtotal - discountAmount;

  return (
    <div className="min-h-screen bg-linear-to-b from-orange-50 via-white to-emerald-50 overflow-hidden">
      <style>{`
        @keyframes float {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-20px); }
        }
        @keyframes slide-in-left {
          from { opacity: 0; transform: translateX(-30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes slide-in-right {
          from { opacity: 0; transform: translateX(30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes bounce-in {
          0% { transform: scale(0.3); opacity: 0; }
          50% { opacity: 1; }
          70% { transform: scale(1.05); }
          100% { transform: scale(1); }
        }
        @keyframes pulse-scale {
          0%, 100% { transform: scale(1); }
          50% { transform: scale(1.05); }
        }
        .animate-float { animation: float 3s cubic-bezier(0.4, 0.0, 0.2, 1) infinite; }
        .animate-slide-in-left { animation: slide-in-left 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-slide-in-right { animation: slide-in-right 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-bounce-in { animation: bounce-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; }
        .animate-pulse-scale { animation: pulse-scale 2s ease-in-out infinite; }
      `}</style>
      
      <Header />

      {/* HERO SECTION */}
      <section className="relative overflow-hidden pt-8 pb-12 md:pt-12 md:pb-16">
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-0 left-1/4 w-72 h-72 bg-orange-400/20 rounded-full blur-3xl animate-float"></div>
          <div className="absolute bottom-0 right-1/4 w-72 h-72 bg-emerald-400/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          <div className="text-center space-y-4 animate-slide-in-left">
            <div className="inline-flex items-center gap-2 bg-linear-to-r from-orange-100 to-red-100 text-orange-700 px-4 py-2 rounded-full w-fit border border-orange-200 mx-auto">
              <span className="text-xl">🛒</span>
              <span className="text-sm font-semibold">Shopping Cart</span>
            </div>
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-black bg-linear-to-r from-orange-600 via-red-500 to-emerald-600 bg-clip-text text-transparent">
              Your Order, Our Priority 💝
            </h1>
            <p className="text-base md:text-lg text-slate-600 max-w-2xl mx-auto">
              {safeCartItems.length} {safeCartItems.length === 1 ? 'delicious item' : 'delicious items'} waiting to be delivered to your doorstep
            </p>
          </div>
        </div>
      </section>

      {/* MAIN CONTENT */}
      <section className="py-8 md:py-12 lg:py-16">
        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          {safeCartItems.length === 0 ? (
            // Empty Cart
            <div className="relative group max-w-2xl mx-auto">
              <div className="absolute -inset-1 bg-linear-to-r from-orange-600 via-red-500 to-emerald-600 rounded-3xl blur-lg opacity-75 group-hover:opacity-100 transition duration-1000"></div>
              <div className="relative bg-white rounded-3xl p-8 md:p-16 text-center">
                <div className="text-7xl md:text-8xl mb-6 animate-bounce">🛍️</div>
                <h2 className="text-3xl md:text-4xl font-black text-slate-900 mb-3">Cart is Empty</h2>
                <p className="text-base md:text-lg text-slate-600 mb-8">
                  Time to fill your cart with amazing snacks! Let's start shopping 🍿
                </p>
                <Button onClick={handleContinueShopping} className="bg-linear-to-r from-orange-600 to-red-600 hover:from-orange-700 hover:to-red-700 text-white font-bold px-8 py-4 rounded-xl">
                  Explore Products
                </Button>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 lg:gap-8">
              {/* Cart Items */}
              <div className="lg:col-span-2 space-y-4">
                {safeCartItems.map((item, idx) => (
                  <div 
                    key={item.id}
                    className="animate-bounce-in"
                    style={{ animationDelay: `${idx * 0.05}s` }}
                  >
                    <div className="bg-white rounded-2xl border-2 border-slate-100 hover:border-orange-300 shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden group p-4 md:p-6">
                      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 md:gap-6">
                        {/* Product Image */}
                        <div className="sm:col-span-1 bg-linear-to-br from-orange-50 to-slate-100 rounded-xl overflow-hidden h-32 sm:h-40 group-hover:shadow-lg transition-all duration-300">
                          <img
                            src={
                              (productImages[item.productId] && productImages[item.productId].length > 0)
                                ? productImages[item.productId][0]
                                : item.image || '/placeholder.jpg'
                            }
                            alt={item.name}
                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                          />
                        </div>

                        {/* Product Info */}
                        <div className="sm:col-span-2">
                          <h3 className="font-bold text-slate-900 mb-2 text-base md:text-lg line-clamp-2">{item.productName}</h3>
                          <p className="text-xs md:text-sm text-orange-600 font-semibold mb-3 bg-orange-50 rounded-full w-fit px-3 py-1">{item.category}</p>
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="text-xl md:text-2xl font-black text-orange-600">
                              ₹{item.currentPrice}
                            </span>
                            {item.originalPrice && (
                              <span className="text-sm text-slate-400 line-through">
                                ₹{item.originalPrice}
                              </span>
                            )}
                            {item.originalPrice && (
                              <span className="text-xs font-bold text-green-600 bg-green-50 px-2 py-1 rounded-full">
                                Save {Math.round(((item.originalPrice - item.currentPrice) / item.originalPrice) * 100)}%
                              </span>
                            )}
                          </div>
                        </div>

                        {/* Quantity & Remove */}
                        <div className="sm:col-span-1 flex flex-col justify-between gap-3">
                          {/* Quantity Selector */}
                          <div className="flex items-center gap-2 bg-linear-to-r from-orange-50 to-red-50 rounded-xl w-fit p-2 self-start md:self-end">
                            <button
                              onClick={() => handleQuantityChange(item.cartItemId, item.quantity - 1)}
                              className="p-1 hover:bg-white rounded-lg transition text-orange-600 hover:text-orange-700"
                            >
                              <Minus className="w-4 h-4" />
                            </button>
                            <span className="w-6 text-center font-bold text-slate-900 text-sm">
                              {item.quantity}
                            </span>
                            <button
                              onClick={() => handleQuantityChange(item.cartItemId, item.quantity + 1)}
                              className="p-1 hover:bg-white rounded-lg transition text-orange-600 hover:text-orange-700"
                            >
                              <Plus className="w-4 h-4" />
                            </button>
                          </div>

                          {/* Remove Button */}
                          <button
                            onClick={() => handleRemoveItem(item.cartItemId)}
                            className="text-red-600 hover:text-red-700 p-2 hover:bg-red-50 rounded-lg transition text-sm font-semibold flex items-center gap-1 self-start md:self-end"
                          >
                            <Trash2 className="w-4 h-4" />
                            <span className="hidden md:inline">Remove</span>
                          </button>
                        </div>
                      </div>

                      {/* Item Total */}
                      <div className="mt-4 pt-4 border-t-2 border-slate-100 text-right">
                        <p className="text-xs md:text-sm text-slate-600 mb-1 font-semibold">Item Total</p>
                        <p className="text-lg md:text-xl font-black text-orange-600">
                          ₹{(item.currentPrice * item.quantity).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}

                {/* Continue Shopping */}
                <button
                  onClick={handleContinueShopping}
                  className="w-full mt-2 py-3 md:py-4 px-6 rounded-xl font-bold text-slate-700 border-2 border-slate-200 hover:border-orange-500 hover:bg-orange-50 transition-all duration-300 flex items-center justify-center gap-2 group text-sm md:text-base"
                >
                  <span>← Continue Shopping</span>
                  <Zap className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                </button>
              </div>

              {/* Order Summary Sidebar */}
            <div className="lg:col-span-1">
              <div className="sticky top-20 space-y-4">
                {/* Coupon Section */}
                <div className="bg-linear-to-br from-orange-500 via-red-500 to-orange-600 rounded-2xl p-4 md:p-6 text-white shadow-lg">
                  {appliedCouponCode && parseFloat(discountAmount) > 0 ? (
                    <div className="space-y-3">
                      <div className="flex items-center gap-2 mb-2">
                        <Gift className="w-5 h-5" />
                        <p className="text-sm font-bold">✓ Coupon Applied!</p>
                      </div>
                      <div className="bg-white/20 backdrop-blur rounded-lg p-3 flex items-center justify-between">
                        <p className="font-bold text-lg">{appliedCouponCode}</p>
                        <button
                          onClick={handleRemoveCoupon}
                          className="p-1 text-white hover:bg-white/30 rounded-full transition"
                          title="Remove coupon"
                        >
                          <X className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      onClick={() => setShowCouponModal(true)}
                      className="w-full flex items-center justify-between font-bold text-sm hover:opacity-90 transition"
                    >
                      <span className="flex items-center gap-2">
                        <Gift className="w-4 h-4" />
                        Apply Coupon Code
                      </span>
                    </button>
                  )}
                </div>

                {/* Order Summary Card */}
                <div className="bg-white rounded-2xl border-2 border-slate-100 shadow-sm overflow-hidden">
                  {/* Summary */}
                  <div className="p-6 space-y-4">
                    <h3 className="font-bold text-xl text-slate-900 mb-6 flex items-center gap-2">
                      <span className="text-2xl">📦</span>
                      Order Summary
                    </h3>

                    <div className="space-y-3">
                      <div className="flex justify-between text-slate-600 text-sm md:text-base">
                        <span className="font-medium">Subtotal</span>
                        <span className="font-semibold">₹{parseFloat(subtotal).toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                      </div>

                      {parseFloat(discountAmount) > 0 && (
                        <div className="flex justify-between text-green-600 text-sm md:text-base bg-green-50 p-3 rounded-lg">
                          <span className="font-semibold">Discount {discountPercent > 0 && `(-${discountPercent}%)`}</span>
                          <span className="font-bold">-₹{parseFloat(discountAmount).toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                        </div>
                      )}

                      <div className="flex justify-between text-slate-600 text-sm md:text-base">
                        <span className="font-medium flex items-center gap-2">
                          <Truck className="w-4 h-4 text-emerald-600" />
                          Shipping
                        </span>
                        <span className="font-semibold text-emerald-600 flex items-center gap-1">
                          <span>FREE</span>
                          <span className="text-xs">✓</span>
                        </span>
                      </div>
                    </div>

                    <div className="border-t-2 border-slate-100 pt-4">
                      <div className="flex justify-between mb-6">
                        <span className="font-bold text-lg text-slate-900">Total Amount</span>
                        <div className="text-right">
                          <span className="text-3xl font-black bg-linear-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                            ₹{parseFloat(finalTotal).toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                          </span>
                        </div>
                      </div>

                      <button
                        onClick={handleCheckout}
                        className="w-full mb-3 py-3 md:py-4 px-6 bg-linear-to-r from-orange-600 via-red-600 to-orange-600 text-white font-bold rounded-xl hover:shadow-xl transform hover:scale-105 transition-all duration-300 text-sm md:text-base flex items-center justify-center gap-2 group"
                      >
                        <Zap className="w-4 h-4 group-hover:animate-pulse" />
                        Proceed to Checkout
                      </button>

                      <button
                        onClick={handleContinueShopping}
                        className="w-full py-3 md:py-4 px-6 rounded-xl font-bold text-slate-700 border-2 border-slate-200 hover:border-orange-500 hover:bg-orange-50 transition-all duration-300 text-sm md:text-base"
                      >
                        Continue Shopping
                      </button>
                    </div>
                  </div>

                  {/* Free Shipping Info */}
                  <div className="bg-linear-to-r from-emerald-50 to-emerald-100 border-t-2 border-emerald-200 p-4 flex gap-3">
                    <Truck className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-bold text-emerald-900 text-sm">Free Shipping on All Orders</p>
                      <p className="text-xs text-emerald-700 mt-0.5">Delivered across India within 5-7 business days</p>
                    </div>
                  </div>
                </div>
              </div>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* Coupon Modal */}
      <Modal
        isOpen={showCouponModal}
        onClose={() => setShowCouponModal(false)}
        title="Select Best Coupon for You"
      >
        <div className="space-y-4">
          {couponLoading ? (
            <div className="flex flex-col items-center justify-center py-12">
              <div className="relative w-12 h-12 mb-4">
                <div className="absolute inset-0 bg-linear-to-r from-orange-400 to-red-400 rounded-full animate-spin opacity-75"></div>
                <div className="absolute inset-2 bg-white rounded-full flex items-center justify-center text-xl">🎟️</div>
              </div>
              <p className="text-slate-600 font-medium">Loading exclusive coupons...</p>
            </div>
          ) : eligibleCoupons && eligibleCoupons.length > 0 ? (
            <div className="space-y-3">
              <div className="flex items-center gap-2 mb-4">
                <Gift className="w-5 h-5 text-orange-600" />
                <h4 className="font-bold text-slate-900 text-lg">Available Coupons Just for You:</h4>
              </div>
              {eligibleCoupons.map((coupon, idx) => (
                <button
                  key={coupon.id}
                  onClick={() => setSelectedCoupon(coupon)}
                  className={`w-full p-4 rounded-xl border-2 transition transform hover:scale-102 animate-bounce-in ${
                    selectedCoupon?.id === coupon.id
                      ? 'border-orange-500 bg-linear-to-br from-orange-50 to-red-50 shadow-lg'
                      : 'border-slate-200 hover:border-orange-400 hover:bg-orange-50/50 bg-white'
                  }`}
                  style={{ animationDelay: `${idx * 0.05}s` }}
                >
                  <div className="flex items-center justify-between text-left gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <p className="font-bold text-slate-900 text-lg">{coupon.code}</p>
                        {selectedCoupon?.id === coupon.id && (
                          <span className="text-xs bg-orange-600 text-white px-2 py-1 rounded-full font-bold">✓ Selected</span>
                        )}
                      </div>
                      <p className="text-sm text-slate-600">{coupon.description || 'Get discount on this order'}</p>
                      {coupon.minOrderAmount && (
                        <p className="text-xs text-slate-500 mt-2 bg-slate-50 px-2 py-1 rounded w-fit">Min order: ₹{coupon.minOrderAmount}</p>
                      )}
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-lg font-black bg-linear-to-br from-green-500 to-emerald-600 bg-clip-text text-transparent">
                        {coupon.type === 'PERCENTAGE' ? `${coupon.discountValue}%` : `₹${coupon.discountValue}`}
                      </p>
                      <p className="text-xs text-green-700 font-bold mt-1">
                        {coupon.type === 'PERCENTAGE' ? 'OFF' : 'Discount'}
                      </p>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="text-6xl mb-4">🎟️</div>
              <p className="text-slate-600 font-medium text-lg">No eligible coupons available</p>
              <p className="text-slate-500 text-sm mt-2">Check back soon for exclusive offers!</p>
            </div>
          )}

          <div className="flex gap-3 pt-6 border-t-2 border-slate-200">
            <button
              onClick={handleApplyCoupon}
              disabled={applyingCoupon || !selectedCoupon}
              className="flex-1 py-3 px-6 bg-linear-to-r from-orange-600 to-red-600 text-white font-bold rounded-xl hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 flex items-center justify-center gap-2"
            >
              <Gift className="w-4 h-4" />
              {applyingCoupon ? 'Applying...' : 'Apply Coupon'}
            </button>
            <button
              onClick={() => {
                setShowCouponModal(false);
                setSelectedCoupon(null);
              }}
              className="flex-1 py-3 px-6 border-2 border-slate-200 text-slate-700 font-bold rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all duration-300"
            >
              Cancel
            </button>
          </div>
        </div>
      </Modal>

      <Footer />
    </div>
  );
};

export default CartPage;
