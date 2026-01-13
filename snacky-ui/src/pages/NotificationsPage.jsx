import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Bell,
  CheckCircle2,
  Package,
  Truck,
  Lock,
  AlertCircle,
  Trash2,
  Archive,
  Check,
  Calendar
} from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Card, Spinner, Badge } from '@/components/common';
import {
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  getUnreadNotificationCount
} from '@/store/thunks/notificationThunks';
import { showToast } from '@/utils/toast';

const NotificationsPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useSelector(state => state.auth);
  const { items: notifications, loading, error, unreadCount } = useSelector(state => state.notifications);

  const [filteredNotifications, setFilteredNotifications] = useState([]);
  const [filterType, setFilterType] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');
  const [deleting, setDeleting] = useState({});

  // Load notifications on mount
  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    dispatch(fetchNotifications())
      .unwrap()
      .catch(err => {
        showToast(err || 'Failed to load notifications', 'error');
      });
  }, [user, dispatch, navigate]);

  // Filter notifications
  useEffect(() => {
    // Ensure notifications is always an array
    const notificationsList = Array.isArray(notifications) 
      ? notifications 
      : (notifications?.data && Array.isArray(notifications.data))
      ? notifications.data
      : [];
    
    let result = [...notificationsList];

    // Filter by status
    if (filterStatus === 'unread') {
      result = result.filter(n => !n.isRead);
    } else if (filterStatus === 'read') {
      result = result.filter(n => n.isRead);
    }

    // Filter by type
    if (filterType !== 'all') {
      result = result.filter(n => n.type?.toLowerCase() === filterType.toLowerCase());
    }

    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    setFilteredNotifications(result);
  }, [notifications, filterType, filterStatus]);

  // Notification type icons and colors
  const getNotificationIcon = (type) => {
    const typeUpper = type?.toUpperCase();
    switch (typeUpper) {
      case 'PAYMENT_RECEIVED':
        return <CheckCircle2 className="w-5 h-5 text-cta-500" />;
      case 'SHIPMENT_CREATED':
        return <Truck className="w-5 h-5 text-brand-500" />;
      case 'ORDER_DELIVERED':
        return <Package className="w-5 h-5 text-emerald-500" />;
      case 'PASSWORD_CHANGED':
        return <Lock className="w-5 h-5 text-orange-500" />;
      case 'ADMIN_SHIPMENT_FAILED':
        return <AlertCircle className="w-5 h-5 text-accent-500" />;
      default:
        return <Bell className="w-5 h-5 text-slate-500" />;
    }
  };

  const getTypeColor = (type) => {
    const typeUpper = type?.toUpperCase();
    switch (typeUpper) {
      case 'PAYMENT_RECEIVED':
        return 'success';
      case 'SHIPMENT_CREATED':
        return 'primary';
      case 'ORDER_DELIVERED':
        return 'success';
      case 'PASSWORD_CHANGED':
        return 'warning';
      case 'ADMIN_SHIPMENT_FAILED':
        return 'danger';
      default:
        return 'default';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now - date) / (1000 * 60));

    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes}m ago`;

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours}h ago`;

    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) return `${diffInDays}d ago`;

    return date.toLocaleDateString('en-IN', {
      month: 'short',
      day: 'numeric',
      year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
    });
  };

  const handleMarkAsRead = (notificationId, isRead) => {
    if (!isRead) {
      dispatch(markNotificationAsRead(notificationId))
        .unwrap()
        .catch(err => {
          showToast(err || 'Failed to mark as read', 'error');
        });
    }
  };

  const handleMarkAllAsRead = () => {
    dispatch(markAllNotificationsAsRead())
      .unwrap()
      .then(() => {
        showToast('All notifications marked as read', 'success');
      })
      .catch(err => {
        showToast(err || 'Failed to mark all as read', 'error');
      });
  };

  const handleDelete = (notificationId) => {
    setDeleting(prev => ({ ...prev, [notificationId]: true }));
    dispatch(deleteNotification(notificationId))
      .unwrap()
      .then(() => {
        showToast('Notification deleted', 'success');
      })
      .catch(err => {
        showToast(err || 'Failed to delete notification', 'error');
      })
      .finally(() => {
        setDeleting(prev => ({ ...prev, [notificationId]: false }));
      });
  };

  const notificationTypes = [
    { value: 'all', label: 'All Notifications' },
    { value: 'payment_received', label: 'Payments' },
    { value: 'shipment_created', label: 'Shipments' },
    { value: 'order_delivered', label: 'Deliveries' },
    { value: 'password_changed', label: 'Security' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white">
      <Header />

      <div className="max-w-4xl mx-auto px-4 py-12">
        {/* Header */}
        <div className="flex items-center justify-between mb-12">
          <div>
            <h1 className="text-4xl font-bold text-slate-900 mb-2">Notifications</h1>
            <p className="text-slate-600">
              {unreadCount > 0 ? `You have ${unreadCount} unread notification${unreadCount !== 1 ? 's' : ''}` : 'All notifications read'}
            </p>
          </div>
          <div className="text-5xl">
            <Bell className="w-12 h-12 text-brand-500" />
          </div>
        </div>

        {/* Filter & Actions */}
        <div className="flex flex-col lg:flex-row gap-6 mb-8 items-start lg:items-center justify-between">
          {/* Type Filter */}
          <div className="flex gap-2 flex-wrap">
            {notificationTypes.map(type => (
              <button
                key={type.value}
                onClick={() => setFilterType(type.value)}
                className={`px-4 py-2 rounded-lg font-bold text-sm transition-all duration-300 ${
                  filterType === type.value
                    ? 'bg-orange-600 text-white shadow-md'
                    : 'bg-gray-100 text-gray-700 border border-gray-300'
                }`}
              >
                {type.label}
              </button>
            ))}
          </div>

          {/* Status Filter & Actions */}
          <div className="flex gap-3 w-full lg:w-auto lg:ml-auto items-center">
            <div className="flex gap-2 flex-wrap">
              {['all', 'unread', 'read'].map(status => (
                <button
                  key={status}
                  onClick={() => setFilterStatus(status)}
                  className={`px-4 py-2 rounded-lg font-bold text-sm capitalize transition-all duration-300 ${
                    filterStatus === status
                      ? 'bg-orange-600 text-white shadow-md'
                      : 'bg-gray-100 text-gray-700 border border-gray-300'
                  }`}
                >
                  {status}
                </button>
              ))}
            </div>
            {unreadCount > 0 && (
              <Button
                onClick={handleMarkAllAsRead}
                variant="primary"
                size="sm"
                className="flex items-center gap-2 whitespace-nowrap ml-2 bg-green-600"
              >
                <Check className="w-4 h-4" />
                Mark All Read
              </Button>
            )}
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex justify-center py-12">
            <Spinner />
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <Card className="p-8 text-center border-red-200 bg-red-50">
            <AlertCircle className="w-12 h-12 text-accent-500 mx-auto mb-4" />
            <p className="text-red-600 font-semibold">{error}</p>
            <Button
              onClick={() => dispatch(fetchNotifications())}
              variant="outline"
              size="sm"
              className="mt-4"
            >
              Try Again
            </Button>
          </Card>
        )}

        {/* Notifications List */}
        {!loading && filteredNotifications.length === 0 ? (
          <Card className="p-12 text-center">
            <Bell className="w-16 h-16 text-slate-300 mx-auto mb-4" />
            <p className="text-slate-600 text-lg font-medium">No notifications</p>
            <p className="text-slate-500 text-sm">
              {filterStatus === 'read' && filterType === 'all'
                ? 'You have no read notifications'
                : filterStatus === 'unread' && filterType === 'all'
                ? 'All caught up!'
                : 'No notifications match your filters'}
            </p>
          </Card>
        ) : (
          <div className="space-y-3">
            {filteredNotifications.map(notification => (
              <Card
                key={notification.id}
                className={`p-5 transition-all duration-300 ${
                  !notification.isRead
                    ? 'bg-linear-to-r from-blue-50 to-white border-l-4 border-brand-500 shadow-md'
                    : 'bg-white hover:shadow-md'
                }`}
              >
                <div className="flex items-start gap-4">
                  {/* Icon */}
                  <div className="flex-shrink-0 mt-1">
                    {getNotificationIcon(notification.type)}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <h3 className={`font-semibold text-lg transition-colors ${
                          !notification.isRead ? 'text-slate-900' : 'text-slate-700'
                        }`}>
                          {notification.title}
                        </h3>
                        <p className="text-slate-600 text-sm mt-1.5">
                          {notification.message}
                        </p>
                      </div>
                      {!notification.isRead && (
                        <div className="flex-shrink-0 w-2.5 h-2.5 rounded-full bg-brand-500 mt-2 animate-pulse"></div>
                      )}
                    </div>

                    {/* Type Badge & Date */}
                    <div className="flex items-center gap-3 mt-3.5">
                      <Badge variant={getTypeColor(notification.type)} size="sm">
                        {notification.type?.replace(/_/g, ' ')}
                      </Badge>
                      <div className="flex items-center gap-1 text-xs text-slate-500">
                        <Calendar className="w-3 h-3" />
                        {formatDate(notification.createdAt)}
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 flex-shrink-0">
                    {!notification.isRead && (
                      <button
                        onClick={() => handleMarkAsRead(notification.id, notification.isRead)}
                        className="p-2 hover:bg-brand-100 rounded-lg transition text-slate-500 hover:text-brand-600"
                        title="Mark as read"
                      >
                        <Check className="w-5 h-5" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(notification.id)}
                      disabled={deleting[notification.id]}
                      className="p-2 hover:bg-red-100 rounded-lg transition text-slate-500 hover:text-red-600 disabled:opacity-50"
                      title="Delete"
                    >
                      {deleting[notification.id] ? (
                        <Spinner className="w-5 h-5" />
                      ) : (
                        <Trash2 className="w-5 h-5" />
                      )}
                    </button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <Footer />
    </div>
  );
};

export default NotificationsPage;
