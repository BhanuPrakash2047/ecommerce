import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Package, ChevronRight, Search, Filter, Calendar, Truck, CheckCircle2, Clock, XCircle, ArrowRight } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Card, Spinner, Badge, Input } from '@/components/common';
import { fetchUserOrders } from '@/store/thunks/orderThunks';
import { showToast } from '@/utils/toast';

const OrderHistoryPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { items: orders, loading, error } = useSelector(state => state.orders);
  const { user } = useSelector(state => state.auth);

  const [filteredOrders, setFilteredOrders] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    dispatch(fetchUserOrders())
      .unwrap()
      .then(data => {
        console.log('Orders received:', data);
      })
      .catch(err => {
        showToast(err || 'Failed to load orders', 'error');
      });
  }, [user, dispatch, navigate]);

  useEffect(() => {
    let result = [...(orders || [])];

    if (filterStatus !== 'all') {
      result = result.filter(o => o.status === filterStatus);
    }

    if (searchTerm) {
      result = result.filter(
        o =>
          o.orderNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          o.id?.toString().includes(searchTerm) ||
          o.receiverName?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    setFilteredOrders(result);
  }, [orders, searchTerm, filterStatus]);

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
      case 'RETURNED':
        return 'warning';
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
        return <XCircle className="w-4 h-4" />;
      default:
        return <Package className="w-4 h-4" />;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-IN', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
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

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-red-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <Spinner size="lg" />
            <p className="mt-4 text-slate-600 font-medium">Loading your orders...</p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-red-50">
      <Header />

      <div className="max-w-7xl mx-auto px-4 py-16">
        {/* Hero Section */}
        <div className="mb-12 animate-fadeInDown">
          <div className="flex items-center gap-4 mb-4">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-orange-400 to-orange-600 flex items-center justify-center shadow-lg">
              <Package className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-4xl font-black bg-gradient-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                Your Orders
              </h1>
              <p className="text-slate-600 mt-1">
                Track and manage all your purchases in one place
              </p>
            </div>
          </div>
          <p className="text-slate-500 text-sm font-medium">
            {orders?.length || 0} {orders?.length === 1 ? 'order' : 'orders'} • 
            {filteredOrders?.length !== orders?.length ? ` ${filteredOrders.length} filtered` : ' All'}
          </p>
        </div>

        {/* Search & Filter Section */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-10 animate-fadeInUp">
          <div className="md:col-span-2 relative group">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none group-focus-within:opacity-100 opacity-60 transition">
              <Search className="w-5 h-5 text-orange-400" />
            </div>
            <Input
              placeholder="Search by order number, ID or recipient name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-12 py-3 text-base border-2 border-orange-200 hover:border-orange-300 focus:border-orange-500 transition-all rounded-xl bg-white shadow-sm"
            />
          </div>

          <div className="relative group">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Filter className="w-5 h-5 text-orange-400" />
            </div>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="w-full pl-12 pr-4 py-3 text-base font-medium border-2 border-orange-200 hover:border-orange-300 focus:border-orange-500 focus:outline-none focus:ring-2 focus:ring-orange-200 rounded-xl bg-white shadow-sm transition-all appearance-none cursor-pointer"
            >
              <option value="all">All Orders</option>
              <option value="CREATED">Created</option>
              <option value="PAYMENT_PENDING">Payment Pending</option>
              <option value="PAID">Paid</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="SHIPPED">Shipped</option>
              <option value="DELIVERED">Delivered</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
        </div>

        {/* Orders List */}
        {filteredOrders.length === 0 ? (
          <div className="animate-fadeInUp">
            <Card className="p-16 text-center border-2 border-dashed border-orange-200 bg-gradient-to-b from-white to-orange-50">
              <div className="inline-block p-4 rounded-full bg-orange-100 mb-6">
                <Package className="w-12 h-12 text-orange-500" />
              </div>
              <h2 className="text-2xl font-bold text-slate-900 mb-2">
                {orders?.length === 0 ? 'No orders yet' : 'No matching orders'}
              </h2>
              <p className="text-slate-500 mb-8 max-w-md mx-auto">
                {orders?.length === 0
                  ? 'Start your snacking journey by placing your first order!'
                  : 'Try adjusting your search or filter to find what you\'re looking for'}
              </p>
              <Button onClick={() => navigate('/products')} variant="primary" size="lg">
                <Package className="w-5 h-5 mr-2" />
                Start Shopping
              </Button>
            </Card>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.map((order, index) => (
              <div
                key={order.id}
                className="group animate-fadeInUp"
              >
                <Card
                  className="p-0 overflow-hidden border-2 border-transparent hover:border-orange-300 transition-all duration-300 cursor-pointer hover:shadow-xl hover:-translate-y-1"
                  onClick={() => navigate(`/orders/${order.id}`)}
                >
                  <div className={`h-1 bg-gradient-to-r ${
                    order.status === 'DELIVERED' ? 'from-green-400 to-emerald-500' :
                    order.status === 'SHIPPED' ? 'from-blue-400 to-blue-600' :
                    order.status === 'CONFIRMED' ? 'from-orange-400 to-orange-600' :
                    order.status === 'CANCELLED' ? 'from-red-400 to-red-600' :
                    'from-yellow-400 to-orange-500'
                  }`} />

                  <div className="p-6">
                    <div className="grid grid-cols-1 md:grid-cols-5 gap-6 items-center">
                      <div className="md:col-span-2">
                        <div className="flex items-start gap-4">
                          <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-orange-100 to-orange-50 flex items-center justify-center text-lg">
                            📦
                          </div>
                          <div>
                            <h3 className="font-bold text-lg text-slate-900 mb-1">
                              {order.orderNumber}
                            </h3>
                            <div className="flex items-center gap-2 text-sm text-slate-500">
                              <Calendar className="w-4 h-4" />
                              {formatDate(order.createdAt)}
                            </div>
                            {order.receiverName && (
                              <p className="text-sm text-slate-600 mt-2 font-medium">
                                📍 {order.receiverName}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4 md:col-span-2">
                        <div className="text-center">
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Items</p>
                          <p className="text-2xl font-bold text-slate-900">
                            {order.itemCount || 0}
                          </p>
                        </div>

                        <div className="text-center">
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Amount</p>
                          <p className="text-2xl font-bold bg-gradient-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                            {formatAmount(order.totalAmount)}
                          </p>
                        </div>
                      </div>

                      <div className="flex flex-col items-center gap-4">
                        <Badge
                          variant={getStatusColor(order.status)}
                          size="sm"
                          className="flex items-center gap-2 px-4 py-2 text-center justify-center w-full"
                        >
                          {getStatusIcon(order.status)}
                          {formatStatus(order.status)}
                        </Badge>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/orders/${order.id}`);
                          }}
                          className="w-full flex items-center justify-center gap-2 px-4 py-2 rounded-lg bg-orange-50 hover:bg-orange-100 text-orange-600 font-semibold text-sm transition-all duration-300 group-hover:gap-3"
                        >
                          View Details
                          <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
                        </button>
                      </div>
                    </div>

                    {(order.trackingNumber || order.deliveredAt) && (
                      <div className="mt-4 pt-4 border-t border-slate-100 flex flex-wrap gap-4 text-sm">
                        {order.trackingNumber && (
                          <div className="flex items-center gap-2 text-slate-600">
                            <Truck className="w-4 h-4 text-orange-500" />
                            <span className="font-mono font-semibold text-slate-900">{order.trackingNumber}</span>
                          </div>
                        )}
                        {order.deliveredAt && (
                          <div className="flex items-center gap-2 text-green-600 font-medium">
                            <CheckCircle2 className="w-4 h-4" />
                            Delivered {formatDate(order.deliveredAt)}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </Card>
              </div>
            ))}
          </div>
        )}
      </div>

      <Footer />
    </div>
  );
};

export default OrderHistoryPage;
