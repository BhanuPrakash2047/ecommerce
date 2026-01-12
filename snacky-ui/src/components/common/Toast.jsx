import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';

/**
 * Toast Notification System
 * Shows notifications for success, error, warning, info
 */

let toastQueue = [];
let listeners = [];

const notifyListeners = () => {
  listeners.forEach(listener => listener([...toastQueue]));
};

export const showToast = (message, type = 'info', duration = 3000, action = null) => {
  const id = Math.random();
  const toast = { id, message, type, action };

  toastQueue.push(toast);
  notifyListeners();

  if (duration > 0) {
    setTimeout(() => {
      toastQueue = toastQueue.filter(t => t.id !== id);
      notifyListeners();
    }, duration);
  }

  return id;
};

export const removeToast = (id) => {
  toastQueue = toastQueue.filter(t => t.id !== id);
  notifyListeners();
};

export const Toast = ({ id, message, type, action, onClose }) => {
  useEffect(() => {
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  const typeStyles = {
    success: 'bg-gradient-to-r from-cta-600 to-cta-500 text-white shadow-md border-l-4 border-cta-700',
    error: 'bg-gradient-to-r from-accent-600 to-accent-500 text-white shadow-md border-l-4 border-accent-700',
    warning: 'bg-gradient-to-r from-yellow-500 to-yellow-400 text-gray-900 shadow-md border-l-4 border-yellow-600',
    info: 'bg-gradient-to-r from-brand-600 to-brand-500 text-white shadow-md border-l-4 border-brand-700',
  };

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ',
  };

  return (
    <div
      className={`${typeStyles[type]} rounded-lg px-4 py-3 mb-3 flex items-center justify-between gap-3 animate-slide-in-right min-w-80`}
      role="alert"
    >
      <div className="flex items-center gap-3">
        <span className="text-xl font-bold">{icons[type]}</span>
        <p className="font-medium">{message}</p>
      </div>

      {action && (
        <button
          onClick={action.onClick}
          className="ml-2 px-3 py-1 bg-white/20 hover:bg-white/30 rounded font-semibold text-sm transition-colors"
        >
          {action.label}
        </button>
      )}

      <button
        onClick={onClose}
        className="ml-auto text-lg hover:opacity-70 transition-opacity"
      >
        ×
      </button>
    </div>
  );
};

export const ToastContainer = () => {
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    const listener = (newToasts) => setToasts(newToasts);
    listeners.push(listener);

    return () => {
      listeners = listeners.filter(l => l !== listener);
    };
  }, []);

  if (toasts.length === 0) return null;

  return ReactDOM.createPortal(
    <div className="fixed top-4 right-4 z-50 flex flex-col animate-fade-in">
      {toasts.map(toast => (
        <Toast
          key={toast.id}
          {...toast}
          onClose={() => removeToast(toast.id)}
        />
      ))}
    </div>,
    document.body
  );
};

/**
 * Modal Component
 */
export const Modal = ({
  isOpen,
  onClose,
  title = '',
  children,
  footer = null,
  size = 'md',
  closeOnEscape = true,
  closeOnBackdrop = true,
  className = '',
}) => {
  useEffect(() => {
    const handleEscape = (e) => {
      if (closeOnEscape && e.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'auto';
    };
  }, [isOpen, closeOnEscape, onClose]);

  if (!isOpen) return null;

  const sizeStyles = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
    '2xl': 'max-w-2xl',
  };

  return ReactDOM.createPortal(
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-40 animate-fade-in"
      onClick={() => closeOnBackdrop && onClose()}
    >
      <div
        className={`bg-white rounded-lg shadow-2xl ${sizeStyles[size]} w-full mx-4 max-h-[90vh] overflow-y-auto animate-scale-in ${className}`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        {title && (
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors text-2xl leading-none"
            >
              ×
            </button>
          </div>
        )}

        {/* Body */}
        <div className="p-6">{children}</div>

        {/* Footer */}
        {footer && <div className="border-t border-gray-200 p-6 flex justify-end gap-3">{footer}</div>}
      </div>
    </div>,
    document.body
  );
};

/**
 * Drawer Component - Side panel
 */
export const Drawer = ({
  isOpen,
  onClose,
  side = 'right',
  title = '',
  children,
  width = 'w-80',
  closeOnEscape = true,
  closeOnBackdrop = true,
}) => {
  useEffect(() => {
    const handleEscape = (e) => {
      if (closeOnEscape && e.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'auto';
    };
  }, [isOpen, closeOnEscape, onClose]);

  if (!isOpen) return null;

  const sideClass = side === 'left' ? 'left-0 animate-slide-in-left' : 'right-0 animate-slide-in-right';

  return ReactDOM.createPortal(
    <div
      className="fixed inset-0 bg-black/50 z-40 animate-fade-in"
      onClick={() => closeOnBackdrop && onClose()}
    >
      <div
        className={`fixed top-0 ${sideClass} ${width} h-full bg-white shadow-2xl flex flex-col`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors text-2xl"
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-6">{children}</div>
      </div>
    </div>,
    document.body
  );
};

export default { Toast, ToastContainer, Modal, Drawer, showToast, removeToast };
