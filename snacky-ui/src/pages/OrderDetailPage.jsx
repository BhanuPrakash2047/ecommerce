import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { 
  ArrowLeft, Package, MapPin, Phone, Mail, Calendar, 
  Truck, CheckCircle2, Clock, AlertCircle, Zap, Download 
} from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Card, Spinner, Badge } from '@/components/common';
import { Modal } from '@/components/common/Toast';
import { fetchOrderDetails } from '@/store/thunks/orderThunks';
import apiClient from '@/store/api';
import { showToast } from '@/utils/toast';

const OrderDetailPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { selectedOrder: order, loading, error } = useSelector(state => state.orders);
  const [showTrackingModal, setShowTrackingModal] = useState(false);
  const [address, setAddress] = useState(null);
  const [loadingAddress, setLoadingAddress] = useState(false);

  useEffect(() => {
    if (orderId) {
      dispatch(fetchOrderDetails(orderId))
        .unwrap()
        .catch(err => {
          showToast('Failed to load order details', 'error');
          navigate('/orders');
        });
    }
  }, [orderId, dispatch, navigate]);

  // Fetch address details when order is loaded
  useEffect(() => {
    if (order?.addressId) {
      setLoadingAddress(true);
      apiClient.get(`/addresses/${order.addressId}`)
        .then(res => {
          const addressData = res.data.data || res.data;
          setAddress(addressData);
          setLoadingAddress(false);
        })
        .catch(err => {
          console.error('Error fetching address:', err);
          setLoadingAddress(false);
        });
    }
  }, [order?.addressId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-red-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <Spinner size="lg" />
            <p className="mt-4 text-slate-600 font-medium">Loading order details...</p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-red-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <Card className="p-8 text-center max-w-md">
            <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-slate-900 mb-2">Order Not Found</h2>
            <p className="text-slate-600 mb-6">
              {error || 'The order you are looking for does not exist.'}
            </p>
            <Button onClick={() => navigate('/orders')} variant="primary">
              Back to Orders
            </Button>
          </Card>
        </div>
        <Footer />
      </div>
    );
  }

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-IN', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatAmount = (amount) => {
    if (!amount) return '₹0';
    return `₹${Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatStatus = (status) => {
    if (!status) return 'Unknown';
    return status
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.toLowerCase().slice(1))
      .join(' ');
  };

  const getStatusColor = (status) => {
    const statusUpper = status?.toUpperCase();
    switch (statusUpper) {
      case 'CREATED':
      case 'PAYMENT_PENDING':
      case 'PAID':
        return 'warning';
      case 'CONFIRMED':
        return 'primary';
      case 'SHIPPED':
        return 'primary';
      case 'DELIVERED':
        return 'success';
      case 'CANCELLED':
        return 'danger';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status) => {
    const statusUpper = status?.toUpperCase();
    switch (statusUpper) {
      case 'CREATED':
      case 'PAYMENT_PENDING':
        return <Clock className="w-4 h-4" />;
      case 'PAID':
      case 'CONFIRMED':
        return <CheckCircle2 className="w-4 h-4" />;
      case 'SHIPPED':
        return <Truck className="w-4 h-4" />;
      case 'DELIVERED':
        return <CheckCircle2 className="w-4 h-4" />;
      case 'CANCELLED':
        return <AlertCircle className="w-4 h-4" />;
      default:
        return <Package className="w-4 h-4" />;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-red-50">
      <Header />

      <div className="max-w-5xl mx-auto px-4 py-8 sm:py-12">
        {/* Navigation */}
        <button
          onClick={() => navigate('/orders')}
          className="flex items-center gap-2 text-orange-600 hover:text-orange-700 font-semibold mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          Back to Orders
        </button>

        {/* Main Order Card */}
        <Card className="mb-8 p-6 sm:p-8 border-2 border-orange-200 bg-gradient-to-r from-orange-50 to-red-50">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 sm:gap-6">
            <div className="min-w-0">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Order #</p>
              <p className="text-lg sm:text-2xl font-black text-slate-900 truncate">{order.orderNumber}</p>
              <p className="text-xs sm:text-sm text-slate-600 mt-1 flex items-center gap-1">
                <Calendar className="w-3 h-3 sm:w-4 sm:h-4 flex-shrink-0" />
                <span className="truncate">{formatDate(order.createdAt).split(',')[0]}</span>
              </p>
            </div>

            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">Status</p>
              <Badge
                variant={getStatusColor(order.status)}
                size="sm"
                className="inline-flex gap-1 text-xs sm:text-sm"
              >
                {getStatusIcon(order.status)}
                {formatStatus(order.status)}
              </Badge>
            </div>

            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Total</p>
              <p className="text-lg sm:text-2xl font-bold bg-gradient-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                {formatAmount(order.totalAmount)}
              </p>
            </div>

            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Items</p>
              <p className="text-lg sm:text-2xl font-bold text-slate-900">
                {order.items?.length || 0}
              </p>
            </div>
          </div>
        </Card>

        {/* Main Content with Sidebar */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 sm:gap-8">
          {/* Left Sidebar - Items & Summary Combined */}
          <div className="lg:col-span-1">
            <Card className="p-6 sm:p-8 border-2 border-slate-200 hover:border-orange-300 transition-colors sticky top-4">
              {/* Items Section */}
              <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center flex-shrink-0">
                  <Package className="w-6 h-6 text-orange-600" />
                </div>
                <h2 className="text-lg font-bold text-slate-900">Items</h2>
              </div>

              <div className="space-y-3 mb-8 pb-8 border-b-2 border-slate-200">
                {order.items?.map((item) => (
                  <div key={item.itemId} className="flex flex-col gap-2 p-3 bg-slate-50 rounded-lg hover:bg-orange-50 transition-colors">
                    <p className="font-semibold text-slate-900 text-sm truncate">{item.productName}</p>
                    <div className="flex justify-between items-center text-xs text-slate-600">
                      <span>{item.quantity} ×</span>
                      <span>{formatAmount(item.unitPrice)}</span>
                    </div>
                    <div className="border-t border-slate-200 pt-2 text-right">
                      <p className="font-bold text-slate-900 text-sm">{formatAmount(item.subtotal)}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Summary Section */}
              <h2 className="text-lg font-bold text-slate-900 mb-6">Summary</h2>

              <div className="space-y-3 pb-6 border-b-2 border-slate-200">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-600">Subtotal</span>
                  <span className="font-semibold text-slate-900">{formatAmount(order.subtotal)}</span>
                </div>

                {order.discountAmount > 0 && (
                  <div className="flex justify-between items-center text-green-600 text-sm">
                    <span>Discount</span>
                    <span className="font-semibold">-{formatAmount(order.discountAmount)}</span>
                  </div>
                )}

                <div className="flex justify-between items-center text-blue-600 text-sm">
                  <span>Shipping</span>
                  <span className="font-semibold">FREE</span>
                </div>
              </div>

              <div className="flex justify-between items-center pt-6">
                <span className="font-bold text-slate-900">Total</span>
                <span className="text-xl font-black bg-gradient-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                  {formatAmount(order.totalAmount)}
                </span>
              </div>
            </Card>
          </div>

          {/* Right Main Section - Address, Tracking & Timeline Combined */}
          <div className="lg:col-span-2">
            <Card className="p-6 sm:p-8 border-2 border-slate-200">
              {/* Shipping Address */}
              <div className="mb-8 pb-8 border-b-2 border-slate-200">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center flex-shrink-0">
                    <MapPin className="w-6 h-6 text-blue-600" />
                  </div>
                  <h2 className="text-lg font-bold text-slate-900">Delivery Address</h2>
                </div>

                {loadingAddress ? (
                  <p className="text-slate-600 text-sm">Loading address...</p>
                ) : address ? (
                  <div className="space-y-2 text-sm">
                    <p className="font-semibold text-slate-900 text-base">{address.fullName || address.name}</p>
                    {address.addressLine1 && (
                      <p className="text-slate-700">{address.addressLine1}</p>
                    )}
                    {address.addressLine2 && (
                      <p className="text-slate-700">{address.addressLine2}</p>
                    )}
                    {(address.city || address.state || address.zipCode) && (
                      <p className="text-slate-700">
                        {[address.city, address.state, address.zipCode].filter(Boolean).join(', ')}
                      </p>
                    )}
                    {address.country && (
                      <p className="text-slate-700">{address.country}</p>
                    )}
                    {address.phoneNumber && (
                      <div className="flex items-center gap-2 mt-4 pt-3 border-t border-slate-200">
                        <Phone className="w-4 h-4 text-slate-400" />
                        <span>{address.phoneNumber}</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="p-4 bg-slate-50 rounded-lg border border-slate-200 text-center">
                    <AlertCircle className="w-6 h-6 text-slate-400 mx-auto mb-2" />
                    <p className="text-sm text-slate-600">Address details not available</p>
                  </div>
                )}
              </div>

              {/* Tracking Info */}
              <div className="mb-8 pb-8 border-b-2 border-slate-200">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center flex-shrink-0">
                    <Truck className="w-6 h-6 text-blue-600" />
                  </div>
                  <h2 className="text-lg font-bold text-slate-900">Tracking</h2>
                </div>

                {order.trackingNumber ? (
                  <div className="space-y-4">
                    <div className="p-4 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg border border-blue-200">
                      <p className="text-xs font-semibold text-slate-500 uppercase mb-1">Tracking Number</p>
                      <p className="font-mono font-bold text-blue-600 text-lg break-all">{order.trackingNumber}</p>
                    </div>
                    <p className="text-sm text-slate-600">
                      Agent: <span className="font-semibold text-slate-900">{order.trackingAgent || 'N/A'}</span>
                    </p>
                    <Button
                      onClick={() => setShowTrackingModal(true)}
                      variant="primary"
                      className="w-full"
                    >
                      <Truck className="w-4 h-4 mr-2" />
                      Track Shipment
                    </Button>
                  </div>
                ) : (
                  <div className="p-4 bg-yellow-50 rounded-lg border border-yellow-200 text-center">
                    <AlertCircle className="w-8 h-8 text-yellow-600 mx-auto mb-2" />
                    <p className="text-sm text-slate-600">
                      Tracking information will be available once your order ships.
                    </p>
                  </div>
                )}
              </div>

              {/* Timeline */}
              <div>
                <h2 className="text-lg font-bold text-slate-900 mb-6">Order Timeline</h2>

                <div className="space-y-4">
                  <div className="flex gap-4">
                    <div className="flex flex-col items-center flex-shrink-0">
                      <div className="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center">
                        <CheckCircle2 className="w-6 h-6 text-green-600" />
                      </div>
                      <div className="w-0.5 h-12 bg-slate-200"></div>
                    </div>
                    <div className="pb-4 min-w-0">
                      <p className="font-semibold text-slate-900 text-sm">Order Placed</p>
                      <p className="text-xs text-slate-600 truncate">{formatDate(order.createdAt)}</p>
                    </div>
                  </div>

                  {order.status === 'CONFIRMED' && (
                    <div className="flex gap-4">
                      <div className="flex flex-col items-center flex-shrink-0">
                        <div className="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center">
                          <CheckCircle2 className="w-6 h-6 text-orange-600" />
                        </div>
                        <div className="w-0.5 h-12 bg-slate-200"></div>
                      </div>
                      <div className="pb-4 min-w-0">
                        <p className="font-semibold text-slate-900 text-sm">Order Confirmed</p>
                        <p className="text-xs text-slate-600 truncate">{formatDate(order.updatedAt)}</p>
                      </div>
                    </div>
                  )}

                  {order.status === 'SHIPPED' && (
                    <div className="flex gap-4">
                      <div className="flex flex-col items-center flex-shrink-0">
                        <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                          <Truck className="w-6 h-6 text-blue-600" />
                        </div>
                        <div className="w-0.5 h-12 bg-slate-200"></div>
                      </div>
                      <div className="pb-4 min-w-0">
                        <p className="font-semibold text-slate-900 text-sm">Shipped</p>
                        <p className="text-xs text-slate-600 truncate">{formatDate(order.updatedAt)}</p>
                      </div>
                    </div>
                  )}

                  {order.status === 'DELIVERED' && (
                    <div className="flex gap-4">
                      <div className="flex flex-col items-center flex-shrink-0">
                        <div className="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center">
                          <CheckCircle2 className="w-6 h-6 text-green-600" />
                        </div>
                      </div>
                      <div className="pb-4 min-w-0">
                        <p className="font-semibold text-slate-900 text-sm">Delivered</p>
                        <p className="text-xs text-slate-600 truncate">{formatDate(order.deliveredAt)}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>

      {/* Tracking Modal */}
      <Modal isOpen={showTrackingModal} onClose={() => setShowTrackingModal(false)}>
        <div className="p-8 max-w-md">
          <div className="mb-6">
            <div className="w-12 h-12 rounded-lg bg-blue-100 flex items-center justify-center mb-4">
              <Truck className="w-6 h-6 text-blue-600" />
            </div>
            <h2 className="text-2xl font-bold text-slate-900">Track Your Order</h2>
            <p className="text-slate-600 mt-2">Tracking #{order.trackingNumber}</p>
          </div>

          <Card className="p-6 mb-6 bg-gradient-to-br from-blue-50 to-indigo-50 border-2 border-blue-200">
            <p className="text-sm text-slate-600 mb-4">
              Real-time tracking information will be available soon. Currently, the tracking API is being integrated.
            </p>
            <div className="space-y-3">
              <div className="p-3 bg-white rounded border border-blue-200">
                <p className="text-xs font-semibold text-blue-600 uppercase mb-1">Tracking Number</p>
                <p className="font-mono font-bold text-slate-900">{order.trackingNumber}</p>
              </div>
              <div className="p-3 bg-white rounded border border-blue-200">
                <p className="text-xs font-semibold text-blue-600 uppercase mb-1">Shipping Partner</p>
                <p className="font-semibold text-slate-900">{order.trackingAgent || 'Delhivery'}</p>
              </div>
            </div>
          </Card>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
            <div className="flex gap-3">
              <AlertCircle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
              <p className="text-sm text-yellow-800">
                Tracking information will be updated as your order progresses. Please check back soon.
              </p>
            </div>
          </div>

          <div className="flex gap-3">
            <Button
              onClick={() => setShowTrackingModal(false)}
              variant="outline"
              className="flex-1"
            >
              Close
            </Button>
            <Button
              variant="primary"
              className="flex-1"
              disabled
            >
              <Zap className="w-4 h-4 mr-2" />
              Coming Soon
            </Button>
          </div>
        </div>
      </Modal>

      <Footer />
    </div>
  );
};

export default OrderDetailPage;
