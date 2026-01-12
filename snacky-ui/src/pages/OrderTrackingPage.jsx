import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { MapPin, Truck, CheckCircle, Package, MoreVertical, Download, Phone, Mail } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Card, Spinner, Badge } from '@/components/common';
import { fetchOrderDetails } from '@/store/thunks/orderThunks';
import { showToast } from '@/utils/toast';

const OrderTrackingPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { currentOrder, tracking, loading, error } = useSelector(state => state.orders);
  const { user } = useSelector(state => state.auth);

  const [showInvoice, setShowInvoice] = useState(false);
  const [downloadingInvoice, setDownloadingInvoice] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (orderId) {
      dispatch(fetchOrderDetails(orderId))
        .unwrap()
        .catch(err => {
          showToast(err || 'Failed to load order', 'error');
        });
    }
  }, [orderId, user, dispatch, navigate]);

  const handleDownloadInvoice = async () => {
    setDownloadingInvoice(true);
    try {
      // Mock invoice download
      showToast('Invoice downloaded successfully', 'success');
    } catch (err) {
      showToast(err || 'Failed to download invoice', 'error');
    } finally {
      setDownloadingInvoice(false);
    }
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

  if (error || !currentOrder) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-slate-900 mb-4">Order not found</h1>
            <Button onClick={() => navigate('/orders')}>View All Orders</Button>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  const statusSteps = [
    { status: 'pending', label: 'Order Placed', icon: Package },
    { status: 'processing', label: 'Processing', icon: Package },
    { status: 'shipped', label: 'Shipped', icon: Truck },
    { status: 'delivered', label: 'Delivered', icon: CheckCircle },
  ];

  const currentStepIndex = statusSteps.findIndex(s => s.status === currentOrder.status);

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white">
      <Header />

      <div className="max-w-4xl mx-auto px-4 py-12">
        {/* Order Header */}
        <div className="mb-8">
          <button
            onClick={() => navigate('/orders')}
            className="text-orange-600 hover:text-orange-700 font-medium mb-4 transition"
          >
            ← Back to Orders
          </button>
          <h1 className="text-4xl font-bold text-slate-900 mb-2">Order #{currentOrder.id}</h1>
          <p className="text-slate-600">
            Placed on {new Date(currentOrder.createdAt).toLocaleDateString('en-IN', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>

        {/* Status Timeline */}
        <Card className="mb-12 p-8">
          <h2 className="text-2xl font-bold text-slate-900 mb-8">Order Status</h2>
          
          <div className="relative">
            {/* Timeline Line */}
            <div className="absolute top-6 left-0 right-0 h-1 bg-slate-200">
              <div
                className="h-full bg-gradient-to-r from-orange-500 to-emerald-500 transition-all duration-500"
                style={{
                  width: `${((currentStepIndex + 1) / statusSteps.length) * 100}%`,
                }}
              />
            </div>

            {/* Timeline Steps */}
            <div className="relative flex justify-between">
              {statusSteps.map((step, idx) => {
                const isCompleted = idx <= currentStepIndex;
                const Icon = step.icon;

                return (
                  <div key={step.status} className="flex flex-col items-center">
                    <div
                      className={`w-12 h-12 rounded-full flex items-center justify-center transition ${
                        isCompleted
                          ? 'bg-gradient-to-r from-orange-500 to-emerald-500 text-white'
                          : 'bg-slate-200 text-slate-600'
                      }`}
                    >
                      <Icon className="w-6 h-6" />
                    </div>
                    <p className="text-sm font-semibold text-slate-900 mt-3 text-center">
                      {step.label}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Status Badge */}
          <div className="mt-8 pt-8 border-t border-slate-200">
            <div className="inline-flex items-center gap-2">
              <Badge
                label={currentOrder.status?.charAt(0).toUpperCase() + currentOrder.status?.slice(1)}
                variant={
                  currentOrder.status === 'delivered'
                    ? 'green'
                    : currentOrder.status === 'shipped'
                    ? 'blue'
                    : 'orange'
                }
              />
              <span className="text-slate-600">
                {currentOrder.status === 'pending' && 'Your order is being prepared for shipment'}
                {currentOrder.status === 'processing' && 'Your order is being processed'}
                {currentOrder.status === 'shipped' && 'Your order is on the way!'}
                {currentOrder.status === 'delivered' && 'Your order has been delivered'}
              </span>
            </div>
          </div>
        </Card>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Items Ordered */}
            <Card className="p-8">
              <h3 className="text-2xl font-bold text-slate-900 mb-6">Items Ordered</h3>
              <div className="space-y-4">
                {currentOrder.items?.map((item, idx) => (
                  <div
                    key={idx}
                    className="flex gap-4 pb-4 border-b border-slate-200 last:border-0"
                  >
                    <div className="w-20 h-20 bg-slate-100 rounded-lg overflow-hidden flex-shrink-0">
                      <img
                        src={item.image || '/placeholder.jpg'}
                        alt={item.name}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-slate-900 mb-1">{item.name}</h4>
                      <p className="text-sm text-slate-600 mb-2">Quantity: {item.quantity}</p>
                      <div className="flex justify-between">
                        <span className="text-slate-600">
                          ₹{item.price} × {item.quantity}
                        </span>
                        <span className="font-bold text-slate-900">
                          ₹{(item.price * item.quantity).toLocaleString()}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>

            {/* Tracking Info */}
            {tracking && (
              <Card className="p-8 bg-blue-50 border-l-4 border-l-blue-500">
                <h3 className="text-2xl font-bold text-slate-900 mb-4 flex items-center gap-2">
                  <Truck className="w-6 h-6 text-blue-600" />
                  Tracking Information
                </h3>
                <div className="space-y-3 text-slate-700">
                  {tracking.trackingNumber && (
                    <p>
                      <span className="font-semibold">Tracking Number:</span> {tracking.trackingNumber}
                    </p>
                  )}
                  {tracking.carrier && (
                    <p>
                      <span className="font-semibold">Carrier:</span> {tracking.carrier}
                    </p>
                  )}
                  {tracking.estimatedDelivery && (
                    <p>
                      <span className="font-semibold">Estimated Delivery:</span>{' '}
                      {new Date(tracking.estimatedDelivery).toLocaleDateString()}
                    </p>
                  )}
                  {tracking.lastUpdate && (
                    <p>
                      <span className="font-semibold">Last Update:</span>{' '}
                      {new Date(tracking.lastUpdate).toLocaleString()}
                    </p>
                  )}
                </div>
              </Card>
            )}

            {/* Delivery Address */}
            <Card className="p-8">
              <h3 className="text-2xl font-bold text-slate-900 mb-4 flex items-center gap-2">
                <MapPin className="w-6 h-6 text-orange-600" />
                Delivery Address
              </h3>
              {currentOrder.deliveryAddress && (
                <div className="text-slate-700">
                  <p className="font-bold text-lg mb-2">{currentOrder.deliveryAddress.fullName}</p>
                  <p>{currentOrder.deliveryAddress.addressLine1}</p>
                  {currentOrder.deliveryAddress.addressLine2 && (
                    <p>{currentOrder.deliveryAddress.addressLine2}</p>
                  )}
                  <p>
                    {currentOrder.deliveryAddress.city}, {currentOrder.deliveryAddress.state} -{' '}
                    {currentOrder.deliveryAddress.pinCode}
                  </p>
                  <p className="mt-4 text-sm">Phone: {currentOrder.deliveryAddress.phone}</p>
                </div>
              )}
            </Card>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Order Summary */}
            <Card className="p-6">
              <h3 className="font-bold text-lg text-slate-900 mb-6">Order Summary</h3>
              <div className="space-y-3 mb-6 pb-6 border-b border-slate-200">
                <div className="flex justify-between text-slate-600">
                  <span>Subtotal</span>
                  <span>₹{currentOrder.subtotal?.toLocaleString()}</span>
                </div>
                {currentOrder.discount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount</span>
                    <span>-₹{currentOrder.discount.toLocaleString()}</span>
                  </div>
                )}
                <div className="flex justify-between text-slate-600">
                  <span>Shipping</span>
                  <span className="text-green-600">FREE</span>
                </div>
                <div className="flex justify-between text-slate-600">
                  <span>Tax (18%)</span>
                  <span>₹{currentOrder.tax?.toLocaleString()}</span>
                </div>
              </div>
              <div className="flex justify-between">
                <span className="font-bold text-slate-900">Total</span>
                <span className="text-2xl font-bold text-orange-600">
                  ₹{currentOrder.total?.toLocaleString()}
                </span>
              </div>
            </Card>

            {/* Payment Info */}
            <Card className="p-6">
              <h3 className="font-bold text-lg text-slate-900 mb-4">Payment</h3>
              <div className="space-y-2 text-sm text-slate-700">
                <p>
                  <span className="font-semibold">Method:</span>{' '}
                  {currentOrder.paymentMethod === 'cod'
                    ? 'Cash on Delivery'
                    : currentOrder.paymentMethod === 'card'
                    ? 'Credit/Debit Card'
                    : 'UPI'}
                </p>
                <p>
                  <span className="font-semibold">Status:</span>{' '}
                  <Badge
                    label={currentOrder.paymentStatus?.toUpperCase()}
                    variant={currentOrder.paymentStatus === 'completed' ? 'green' : 'orange'}
                  />
                </p>
              </div>
            </Card>

            {/* Actions */}
            <div className="space-y-3">
              <Button
                onClick={handleDownloadInvoice}
                variant="outline"
                className="w-full flex items-center justify-center gap-2"
                disabled={downloadingInvoice}
              >
                <Download className="w-4 h-4" />
                {downloadingInvoice ? 'Downloading...' : 'Download Invoice'}
              </Button>

              <Button
                onClick={() => navigate('/products')}
                variant="primary"
                className="w-full"
              >
                Continue Shopping
              </Button>
            </div>

            {/* Support */}
            <Card className="p-6 bg-slate-50">
              <h3 className="font-bold text-slate-900 mb-4">Need Help?</h3>
              <div className="space-y-3 text-sm">
                <a
                  href="tel:+919876543210"
                  className="flex items-center gap-2 text-slate-700 hover:text-orange-600 transition"
                >
                  <Phone className="w-4 h-4" />
                  Call Support
                </a>
                <a
                  href="mailto:support@snacky.com"
                  className="flex items-center gap-2 text-slate-700 hover:text-orange-600 transition"
                >
                  <Mail className="w-4 h-4" />
                  Email Support
                </a>
              </div>
            </Card>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default OrderTrackingPage;
