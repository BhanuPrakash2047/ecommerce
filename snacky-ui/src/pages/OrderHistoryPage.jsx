/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Package, ChevronRight, Search, Filter, Calendar, Truck, CheckCircle2, Clock, XCircle, ArrowRight, Zap } from 'lucide-react';
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
    
    // Use microtask to avoid cascading renders warning
    queueMicrotask(() => {
      setFilteredOrders(result);
    });
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
      <div className="min-h-screen bg-linear-to-b from-orange-50 via-white to-emerald-50 flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <div className="relative w-20 h-20 mb-6 mx-auto">
              <div className="absolute inset-0 bg-linear-to-r from-orange-400 to-red-400 rounded-full animate-spin opacity-75"></div>
              <div className="absolute inset-2 bg-white rounded-full flex items-center justify-center text-2xl">📦</div>
            </div>
            <p className="text-slate-600 text-lg font-semibold">Loading your orders...</p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

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
        .animate-float { animation: float 3s cubic-bezier(0.4, 0.0, 0.2, 1) infinite; }
        .animate-slide-in-left { animation: slide-in-left 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-slide-in-right { animation: slide-in-right 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-bounce-in { animation: bounce-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; }
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
              <span className="text-xl">📦</span>
              <span className="text-sm font-semibold">Order History</span>
            </div>
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-black bg-linear-to-r from-orange-600 via-red-500 to-emerald-600 bg-clip-text text-transparent">
              Your Orders 
            </h1>
            <p className="text-base md:text-lg text-slate-600 max-w-2xl mx-auto">
              Track and manage all your purchases in one place. {orders?.length || 0} {orders?.length === 1 ? 'order' : 'orders'} total
            </p>
          </div>
        </div>
      </section>

      {/* SEARCH & FILTER SECTION */}
      <section className="py-6 md:py-8 border-b-2 border-slate-100">
        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 animate-slide-in-right">
            <div className="md:col-span-2 relative group">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none opacity-60 group-focus-within:opacity-100 transition">
                <Search className="w-5 h-5 text-orange-600" />
              </div>
              <Input
                placeholder="Search by order number or name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-12 py-2.5 md:py-3 text-sm border-2 border-orange-200 hover:border-orange-400 focus:border-orange-500 transition-all rounded-xl bg-white shadow-sm"
              />
            </div>

            <div className="relative group">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Filter className="w-5 h-5 text-orange-600" />
              </div>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="w-full pl-12 pr-4 py-2.5 md:py-3 text-sm font-medium border-2 border-orange-200 hover:border-orange-400 focus:border-orange-500 focus:outline-none rounded-xl bg-white shadow-sm transition-all appearance-none cursor-pointer"
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
        </div>
      </section>

      {/* ORDERS LIST */}
      <section className="py-8 md:py-12 lg:py-16">
        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
        {filteredOrders.length === 0 ? (
          <div className="animate-bounce-in" style={{ animation: 'bounce-in 0.5s ease-out' }}>
            <Card className="p-12 md:p-16 text-center border-2 border-dashed border-orange-200 bg-linear-to-b from-white to-orange-50">
              <div className="inline-block p-3 md:p-4 rounded-full bg-orange-100 mb-4 md:mb-6">
                <Package className="w-10 h-10 md:w-12 md:h-12 text-orange-500" />
              </div>
              <h2 className="text-lg md:text-2xl font-bold text-slate-900 mb-2">
                {orders?.length === 0 ? 'No orders yet' : 'No matching orders'}
              </h2>
              <p className="text-xs md:text-sm text-slate-500 mb-6 md:mb-8 max-w-md mx-auto">
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
          <div className="space-y-3">
            {filteredOrders.map((order, index) => (
              <div
                key={order.id}
                className="group animate-bounce-in"
                style={{ animation: `bounce-in 0.5s ease-out ${index * 0.05}s both` }}
              >
                <Card
                  className="p-0 overflow-hidden border-2 border-slate-100 hover:border-orange-400 transition-all duration-300 cursor-pointer hover:shadow-lg hover:-translate-y-0.5 bg-white"
                  onClick={() => navigate(`/orders/${order.id}`)}
                >
                  <div className={`h-1 bg-linear-to-r ${
                    order.status === 'DELIVERED' ? 'from-green-400 to-emerald-500' :
                    order.status === 'SHIPPED' ? 'from-blue-400 to-blue-600' :
                    order.status === 'CONFIRMED' ? 'from-orange-400 to-orange-600' :
                    order.status === 'CANCELLED' ? 'from-red-400 to-red-600' :
                    'from-yellow-400 to-orange-500'
                  }`} />

                  <div className="p-4 md:p-5">
                    <div className="grid grid-cols-1 md:grid-cols-5 gap-4 md:gap-5 items-start md:items-center">
                      <div className="md:col-span-2">
                        <div className="flex items-start gap-3">
                          <div className="w-10 h-10 md:w-12 md:h-12 rounded-lg bg-linear-to-br from-orange-100 to-orange-50 flex items-center justify-center text-base md:text-lg shrink-0">
                            📦
                          </div>
                          <div className="min-w-0">
                            <h3 className="font-bold text-base md:text-lg text-slate-900 mb-0.5 truncate">
                              {order.orderNumber}
                            </h3>
                            <div className="flex items-center gap-1 text-xs md:text-sm text-slate-500 mb-1">
                              <Calendar className="w-3 h-3 md:w-4 md:h-4 shrink-0" />
                              {formatDate(order.createdAt)}
                            </div>
                            {order.receiverName && (
                              <p className="text-xs md:text-sm text-slate-600 font-medium truncate">
                                📍 {order.receiverName}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3 md:col-span-2">
                        <div className="text-center">
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Items</p>
                          <p className="text-lg md:text-xl font-bold text-slate-900">
                            {order.itemCount || 0}
                          </p>
                        </div>

                        <div className="text-center">
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1">Amount</p>
                          <p className="text-lg md:text-xl font-bold bg-linear-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                            {formatAmount(order.totalAmount)}
                          </p>
                        </div>
                      </div>

                      <div className="flex flex-col items-center gap-2 md:gap-3">
                        <Badge
                          variant={getStatusColor(order.status)}
                          size="sm"
                          className="flex items-center gap-1.5 px-3 py-1.5 md:px-4 md:py-2 text-center justify-center w-full text-xs md:text-sm"
                        >
                          {getStatusIcon(order.status)}
                          {formatStatus(order.status)}
                        </Badge>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/orders/${order.id}`);
                          }}
                          className="w-full flex items-center justify-center gap-2 px-3 py-1.5 md:px-4 md:py-2 rounded-lg bg-orange-50 hover:bg-orange-100 text-orange-600 font-semibold text-xs md:text-sm transition-all duration-300 group-hover:gap-3"
                        >
                          View Details
                          <ArrowRight className="w-3.5 h-3.5 md:w-4 md:h-4 transition-transform group-hover:translate-x-1" />
                        </button>
                      </div>
                    </div>

                    {(order.trackingNumber || order.deliveredAt) && (
                      <div className="mt-3 md:mt-4 pt-3 md:pt-4 border-t border-slate-100 flex flex-wrap gap-2 md:gap-4 text-xs md:text-sm">
                        {order.trackingNumber && (
                          <div className="flex items-center gap-1.5 text-slate-600">
                            <Truck className="w-3.5 h-3.5 md:w-4 md:h-4 text-orange-500 shrink-0" />
                            <span className="font-mono font-semibold text-slate-900">{order.trackingNumber}</span>
                          </div>
                        )}
                        {order.deliveredAt && (
                          <div className="flex items-center gap-1.5 text-green-600 font-medium">
                            <CheckCircle2 className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
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
      </section>

      <Footer />
    </div>
  );
};

export default OrderHistoryPage;
