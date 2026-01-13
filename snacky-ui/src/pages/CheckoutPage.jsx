/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { MapPin, Plus, Edit2, Trash2, AlertCircle, CheckCircle } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Input, Card, Modal, Spinner } from '@/components/common';
import { fetchAddresses, createAddress, updateAddress, deleteAddress } from '@/store/thunks/addressThunks';
import { createOrder } from '@/store/thunks/orderThunks';
import { fetchCart } from '@/store/thunks/cartThunks';
import { showToast } from '@/utils/toast';
import { useFormValidation } from '@/hooks';

const CheckoutPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user, token } = useSelector(state => state.auth);
  const { items = [], loading: cartLoading, discountAmount: cartDiscount = '0' } = useSelector(state => state.cart || {});
  const { items: addresses = [], loading: addressLoading } = useSelector(state => state.addresses || {});

  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [editingAddressId, setEditingAddressId] = useState(null);
  const [savingAddress, setSavingAddress] = useState(false);
  const [placing, setPlacing] = useState(false);
  const [orderSuccess, setOrderSuccess] = useState(null);
  const [error, setError] = useState(null);
  const apiBase = import.meta.env.VITE_API_URL;
  
  const { formData: addressForm, errors, touched, handleChange, handleBlur, validateForm, getFieldError, setFormValues } = useFormValidation({
    fullName: '',
    phoneNumber: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    zipCode: '',
  });

  const addressValidationRules = {
    fullName: 'fullName',
    phoneNumber: 'phone',
    addressLine1: 'addressLine',
    addressLine2: 'addressLine',
    city: 'required',
    state: 'required',
    zipCode: 'zipCode',
  };

  // Redirect if not logged in
  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  // Fetch cart data on mount
  useEffect(() => {
    if (user) {
      dispatch(fetchCart());
    }
  }, [user, dispatch]);

  // Redirect if cart is empty (but only after it's loaded)
  useEffect(() => {
    if (!cartLoading && items.length === 0 && user) {
      showToast('Your cart is empty', 'error');
      navigate('/cart');
    }
  }, [cartLoading, items.length, user, navigate]);

  // Fetch addresses on mount
  useEffect(() => {
    if (user) {
      dispatch(fetchAddresses());
    }
  }, [user, dispatch]);

  // Auto-select default address
  useEffect(() => {
    if (addresses.length > 0 && !selectedAddressId) {
      const defaultAddr = addresses.find(a => a.isDefault);
      setSelectedAddressId(defaultAddr?.id || addresses[0].id);
    }
  }, [addresses, selectedAddressId]);

  const handleAddAddress = () => {
    setEditingAddressId(null);
    setFormValues({
      fullName: '',
      phoneNumber: '',
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      zipCode: '',
    });
    setShowAddressModal(true);
  };

  const handleEditAddress = (address) => {
    setEditingAddressId(address.id);
    setFormValues({
      fullName: address.fullName,
      phoneNumber: address.phoneNumber,
      addressLine1: address.addressLine1,
      addressLine2: address.addressLine2 || '',
      city: address.city,
      state: address.state,
      zipCode: address.zipCode,
    });
    setShowAddressModal(true);
  };

  const handleSaveAddress = async () => {
    // Validate form using validation rules
    if (!validateForm(addressForm, addressValidationRules)) {
      return;
    }

    setSavingAddress(true);
    try {
      if (editingAddressId) {
        await dispatch(updateAddress({
          addressId: editingAddressId,
          ...addressForm
        })).unwrap();
        showToast('Address updated successfully', 'success');
      } else {
        const result = await dispatch(createAddress(addressForm)).unwrap();
        setSelectedAddressId(result.id);
        showToast('Address added successfully', 'success');
      }
      setShowAddressModal(false);
    } catch (err) {
      showToast(err || 'Failed to save address', 'error');
    } finally {
      setSavingAddress(false);
    }
  };

  const handleDeleteAddress = async (id) => {
    if (window.confirm('Are you sure you want to delete this address?')) {
      try {
        await dispatch(deleteAddress(id)).unwrap();
        showToast('Address deleted successfully', 'success');
        if (selectedAddressId === id) {
          setSelectedAddressId(null);
        }
      } catch (err) {
        showToast(err || 'Failed to delete address', 'error');
      }
    }
  };

  const handlePlaceOrder = async () => {
    if (!selectedAddressId) {
      showToast('Please select a delivery address', 'error');
      return;
    }

    setPlacing(true);
    setError(null);

    try {
      // Step 1: Create order on backend
      const orderResponse = await dispatch(createOrder(selectedAddressId)).unwrap();

      // Step 2: Initialize Razorpay payment
      if (orderResponse.razorpayOrderId) {
        initializeRazorpay(orderResponse);
      } else {
        // No payment required or COD
        setOrderSuccess(orderResponse);
        setTimeout(() => {
          navigate(`/orders/${orderResponse.orderId}`);
        }, 3000);
      }
    } catch (err) {
      const errorMsg = err || 'Failed to place order';
      setError(errorMsg);
      showToast(errorMsg, 'error');
    } finally {
      setPlacing(false);
    }
  };

  const initializeRazorpay = (orderResponse) => {
    const options = {
      key: import.meta.env.VITE_RAZORPAY_KEY,
      amount: orderResponse.amount, // Amount from backend (already in paise)
      currency: 'INR',
      order_id: orderResponse.razorpayOrderId,
      name: 'Snacky',
      description: `Order #${orderResponse.orderNumber}`,
      email: orderResponse.email,
      contact: orderResponse.phone,
      notes: {
        orderId: orderResponse.orderId,
        orderNumber: orderResponse.orderNumber,
      },
      
      // ✅ SUCCESS HANDLER
      handler: async function(response) {
        await handlePaymentSuccess(
          orderResponse.razorpayOrderId,
          response.razorpay_payment_id,
          response.razorpay_signature,
          orderResponse
        );
      },

      prefill: {
        name: user?.name || 'Customer',
        email: orderResponse.email || '',
        contact: orderResponse.phone || '',
      },

      theme: {
        color: '#EA580C',
      },

      // ❌ FAILURE HANDLER - when user dismisses modal
      modal: {
        ondismiss: function() {
          handlePaymentCancelled(orderResponse.razorpayOrderId);
        },
      },
    };

    // Load and open Razorpay
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload = () => {
      try {
        const rzp = new window.Razorpay(options);
        rzp.open();
      } catch (err) {
        console.error('❌ Razorpay Error:', err);
        setError('Razorpay Error: ' + err.message);
        showToast('Razorpay Error: ' + err.message, 'error');
      }
    };
    script.onerror = () => {
      console.error('❌ Failed to load Razorpay SDK');
      setError('Failed to load payment gateway');
      showToast('Failed to load payment gateway', 'error');
    };
    document.body.appendChild(script);
  };

  // ========== PAYMENT SUCCESS HANDLER ==========
  const handlePaymentSuccess = async (razorpayOrderId, razorpayPaymentId, signature, orderResponse) => {
    try {
      // Call backend webhook endpoint to verify payment
      const response = await fetch(
        `${apiBase}/payments/webhook/success?razorpayOrderId=${razorpayOrderId}&razorpayPaymentId=${razorpayPaymentId}&signature=${signature}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`, 
          },
        }
      );

      const data = await response.json();

      if (response.ok) {
        showToast('✅ Payment Successful! Order confirmed.', 'success');
        setOrderSuccess(orderResponse);
        setTimeout(() => {
          navigate(`/orders/${orderResponse.orderId}`);
        }, 2000);
      } else {
        setError('Payment verification failed: ' + (data.error || 'Unknown error'));
        showToast('Payment verification failed: ' + (data.error || 'Unknown error'), 'error');
      }
    } catch (error) {
      setError('Failed to verify payment: ' + error.message);
      showToast('Failed to verify payment', 'error');
    }
  };

  // ========== PAYMENT CANCELLED HANDLER ==========
  const handlePaymentCancelled = async (razorpayOrderId) => {
    try {
      // Optionally call backend to mark order as failed/cancelled
      const response = await fetch(
        `${apiBase}/payments/webhook/failure?razorpayOrderId=${razorpayOrderId}&razorpayPaymentId=cancelled`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`, 

          },
        }
      );

      const data = await response.json();
      showToast('Payment cancelled. Your order is saved. You can retry anytime.', 'info');
      setError('Payment cancelled by user. You can try again.');
    } catch (error) {
      console.error('Payment Cancellation Error:', error);
      showToast('Payment cancelled. Your order is saved.', 'info');
      setError('Payment cancelled. Your order is saved.');
    }
  };

  // Loading state
  if (addressLoading) {
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

  // Success state - show order confirmation
  if (orderSuccess) {
    return (
      <div className="min-h-screen bg-linear-to-b from-green-50 to-white">
        <Header />
        <div className="max-w-2xl mx-auto px-4 py-12 text-center">
          <div className="mb-8 animate-bounce">
            <CheckCircle className="w-24 h-24 text-green-500 mx-auto" />
          </div>
          <h1 className="text-4xl font-bold text-slate-900 mb-4">Order Confirmed!</h1>
          <p className="text-xl text-slate-600 mb-8">
            Thank you for your order. Redirecting to order details...
          </p>
          <Card className="p-6 border-l-4 border-l-green-500 text-left max-w-md mx-auto">
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-slate-600">Order ID:</span>
                <span className="font-bold text-slate-900">{orderSuccess.orderId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-600">Order Number:</span>
                <span className="font-bold text-slate-900">{orderSuccess.orderNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-600">Amount:</span>
                <span className="font-bold text-green-600 text-lg">₹{orderSuccess.amount?.toLocaleString()}</span>
              </div>
            </div>
          </Card>
        </div>
        <Footer />
      </div>
    );
  }

  const subtotal = items.reduce((sum, item) => sum + (item.currentPrice * item.quantity), 0);
  const appliedDiscount = parseFloat(cartDiscount || 0) || 0;
  const total = subtotal - appliedDiscount;

  return (
    <div className="min-h-screen bg-linear-to-b from-slate-50 to-white">
      <Header />

      <div className="max-w-6xl mx-auto px-4 py-12">
        <h1 className="text-3xl font-bold text-slate-900 mb-12">Checkout</h1>

        {/* Error Alert */}
        {error && (
          <div className="mb-8 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
            <div>
              <h3 className="font-bold text-red-900 mb-1">Error</h3>
              <p className="text-red-800">{error}</p>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2">
            {/* Delivery Address Section */}
            <Card className="mb-8 p-6 border-l-4 border-l-orange-500">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
                  <MapPin className="w-6 h-6 text-orange-600" />
                  Delivery Address
                </h2>
              </div>

              {addresses.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-slate-600 mb-4">No addresses found</p>
                  <Button
                    onClick={handleAddAddress}
                    variant="primary"
                    className="inline-flex items-center gap-2"
                  >
                    <Plus className="w-4 h-4" />
                    Add Your First Address
                  </Button>
                </div>
              ) : (
                <>
                  {/* Addresses List */}
                  <div className="space-y-3 mb-6">
                    {addresses.map(address => (
                      <div
                        key={address.id}
                        onClick={() => setSelectedAddressId(address.id)}
                        className={`p-4 rounded-lg border-2 cursor-pointer transition ${
                          selectedAddressId === address.id
                            ? 'border-orange-500 bg-orange-50'
                            : 'border-slate-200 bg-white hover:border-slate-300'
                        }`}
                      >
                        <div className="flex justify-between items-start mb-2">
                          <div className="flex items-start gap-3">
                            <input
                              type="radio"
                              checked={selectedAddressId === address.id}
                              readOnly
                              className="mt-1 w-4 h-4 accent-orange-500"
                            />
                            <div>
                              <h4 className="font-bold text-slate-900">{address.fullName}</h4>
                              <p className="text-sm text-slate-600">{address.phoneNumber}</p>
                            </div>
                          </div>
                          {address.isDefault && (
                            <span className="bg-green-100 text-green-800 text-xs font-semibold px-3 py-1 rounded-full">
                              Default
                            </span>
                          )}
                        </div>
                        <p className="text-slate-700 text-sm mb-1">
                          {address.addressLine1}
                          {address.addressLine2 && `, ${address.addressLine2}`}
                        </p>
                        <p className="text-slate-600 text-sm">
                          {address.city}, {address.state} - {address.zipCode}
                        </p>
                        <div className="mt-3 flex gap-2">
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleEditAddress(address);
                            }}
                            className="text-sm text-orange-600 hover:text-orange-700 flex items-center gap-1"
                          >
                            <Edit2 className="w-4 h-4" /> Edit
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteAddress(address.id);
                            }}
                            className="text-sm text-red-600 hover:text-red-700 flex items-center gap-1"
                          >
                            <Trash2 className="w-4 h-4" /> Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Add New Address Button */}
                  <Button
                    onClick={handleAddAddress}
                    variant="outline"
                    className="w-full flex items-center justify-center gap-2"
                  >
                    <Plus className="w-4 h-4" />
                    Add Another Address
                  </Button>
                </>
              )}
            </Card>

            {/* Place Order Button */}
            <Button
              onClick={handlePlaceOrder}
              disabled={!selectedAddressId || placing}
              className={`w-full py-4 text-lg font-bold rounded-lg transition ${
                !selectedAddressId || placing
                  ? 'bg-slate-300 text-slate-500 cursor-not-allowed'
                  : 'bg-linear-to-r from-orange-600 to-red-600 text-white hover:shadow-lg'
              }`}
            >
              {placing ? (
                <div className="flex items-center justify-center gap-2">
                  <Spinner size="sm" />
                  Processing...
                </div>
              ) : (
                `Place Order & Pay (₹${total.toLocaleString()})`
              )}
            </Button>
          </div>

          {/* Order Summary Sidebar */}
          <div>
            <div className="sticky top-4 bg-white rounded-xl border border-slate-200 shadow-lg p-6">
              <h3 className="font-bold text-lg text-slate-900 mb-6">Order Summary</h3>

              {/* Cart Items */}
              <div className="space-y-3 mb-6 max-h-64 overflow-y-auto">
                {items.map(item => (
                  <div key={item.id} className="flex justify-between text-sm">
                    <div>
                      <p className="font-medium text-slate-900">{item.productName}</p>
                      <p className="text-slate-600 text-xs">x{item.quantity}</p>
                    </div>
                    <span className="font-semibold text-slate-900">
                      ₹{(item.currentPrice * item.quantity).toLocaleString()}
                    </span>
                  </div>
                ))}
              </div>

              {/* Price Breakdown */}
              <div className="border-t border-slate-200 pt-4 space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-600">Subtotal</span>
                  <span className="text-slate-900 font-medium">₹{subtotal.toLocaleString()}</span>
                </div>
                {appliedDiscount > 0 && (
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Discount</span>
                    <span className="text-green-600 font-medium">-₹{appliedDiscount.toLocaleString()}</span>
                  </div>
                )}
                <div className="flex justify-between text-sm">
                  <span className="text-slate-600">Shipping</span>
                  <span className="text-green-600 font-bold">FREE</span>
                </div>
                <div className="border-t border-slate-200 pt-3 flex justify-between">
                  <span className="font-bold text-slate-900">Total Amount</span>
                  <span className="text-2xl font-bold text-orange-600">
                    ₹{total.toLocaleString()}
                  </span>
                </div>
              </div>

              {/* Promo Message */}
              <div className="mt-6 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-xs text-blue-800">
                  💳 Secure payment powered by Razorpay. Your data is encrypted.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Modal
        isOpen={showAddressModal}
        onClose={() => setShowAddressModal(false)}
        title={editingAddressId ? 'Edit Address' : 'Add New Address'}
      >
        <div className="space-y-4">
          <Input
            placeholder="Full Name *"
            value={addressForm.fullName}
            onChange={(e) => handleChange(e, addressValidationRules)}
            onBlur={(e) => handleBlur('fullName', addressValidationRules)}
            error={getFieldError('fullName')}
          />
          <Input
            placeholder="Phone Number *"
            value={addressForm.phoneNumber}
            onChange={(e) => handleChange(e, addressValidationRules)}
            onBlur={(e) => handleBlur('phoneNumber', addressValidationRules)}
            error={getFieldError('phoneNumber')}
          />
          <Input
            placeholder="Address Line 1 *"
            value={addressForm.addressLine1}
            onChange={(e) => handleChange(e, addressValidationRules)}
            onBlur={(e) => handleBlur('addressLine1', addressValidationRules)}
            error={getFieldError('addressLine1')}
          />
          <Input
            placeholder="Address Line 2 (Optional)"
            value={addressForm.addressLine2}
            onChange={(e) => handleChange(e, addressValidationRules)}
            onBlur={(e) => handleBlur('addressLine2', addressValidationRules)}
          />
          <div className="grid grid-cols-2 gap-4">
            <Input
              placeholder="City *"
              value={addressForm.city}
              onChange={(e) => handleChange(e, addressValidationRules)}
              onBlur={(e) => handleBlur('city', addressValidationRules)}
              error={getFieldError('city')}
            />
            <Input
              placeholder="State *"
              value={addressForm.state}
              onChange={(e) => handleChange(e, addressValidationRules)}
              onBlur={(e) => handleBlur('state', addressValidationRules)}
              error={getFieldError('state')}
            />
          </div>
          <Input
            placeholder="PIN Code *"
            value={addressForm.zipCode}
            onChange={(e) => handleChange(e, addressValidationRules)}
            onBlur={(e) => handleBlur('zipCode', addressValidationRules)}
            error={getFieldError('zipCode')}
          />

          <div className="flex gap-3">
            <Button
              onClick={handleSaveAddress}
              variant="primary"
              className="flex-1"
              disabled={savingAddress}
            >
              {savingAddress ? 'Saving...' : 'Save Address'}
            </Button>
            <Button
              onClick={() => setShowAddressModal(false)}
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

export default CheckoutPage;
