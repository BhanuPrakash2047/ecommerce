import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

/**
 * Professional Toast Notification System
 * Beautiful, consistent notifications matching Snacky design system
 */

let toastQueue = [];
let listeners = [];

const notifyListeners = () => {
  listeners.forEach(listener => listener([...toastQueue]));
};

export const showToast = (message, type = 'info', duration = 4000, action = null) => {
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
  const [isExiting, setIsExiting] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsExiting(true);
      setTimeout(onClose, 300);
    }, 4000);

    return () => clearTimeout(timer);
  }, [onClose]);

  // Color palette mapping
  const typeConfig = {
    success: {
      icon: CheckCircle,
      bgColor: 'bg-emerald-50',
      borderColor: 'border-emerald-200',
      iconColor: 'text-emerald-600',
      textColor: 'text-emerald-900',
      labelColor: 'text-emerald-700',
      accentColor: 'bg-emerald-100 hover:bg-emerald-200',
      progressColor: 'bg-emerald-500',
    },
    error: {
      icon: AlertCircle,
      bgColor: 'bg-red-50',
      borderColor: 'border-red-200',
      iconColor: 'text-red-600',
      textColor: 'text-red-900',
      labelColor: 'text-red-700',
      accentColor: 'bg-red-100 hover:bg-red-200',
      progressColor: 'bg-red-500',
    },
    warning: {
      icon: AlertTriangle,
      bgColor: 'bg-amber-50',
      borderColor: 'border-amber-200',
      iconColor: 'text-amber-600',
      textColor: 'text-amber-900',
      labelColor: 'text-amber-700',
      accentColor: 'bg-amber-100 hover:bg-amber-200',
      progressColor: 'bg-amber-500',
    },
    info: {
      icon: Info,
      bgColor: 'bg-blue-50',
      borderColor: 'border-blue-200',
      iconColor: 'text-blue-600',
      textColor: 'text-blue-900',
      labelColor: 'text-blue-700',
      accentColor: 'bg-blue-100 hover:bg-blue-200',
      progressColor: 'bg-blue-500',
    },
  };

  const config = typeConfig[type] || typeConfig.info;
  const Icon = config.icon;

  return (
    <div
      className={`
        relative w-full max-w-md
        ${config.bgColor} 
        border-l-4 ${config.borderColor}
        rounded-lg shadow-lg
        px-5 py-4
        flex items-start gap-4
        transform transition-all duration-300 ease-out
        ${isExiting 
          ? 'translate-x-full opacity-0' 
          : 'translate-x-0 opacity-100 animate-slideInRight'
        }
      `}
      role="alert"
      aria-live="polite"
    >
      {/* Icon */}
      <div className="flex-shrink-0 mt-0.5">
        <Icon className={`w-5 h-5 ${config.iconColor}`} strokeWidth={2.5} />
      </div>

      {/* Content */}
      <div className="flex-grow min-w-0">
        <p className={`text-sm font-medium ${config.textColor} leading-relaxed`}>
          {message}
        </p>
      </div>

      {/* Action Button */}
      {action && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            action.onClick();
            setIsExiting(true);
            setTimeout(onClose, 300);
          }}
          className={`
            flex-shrink-0 px-3 py-1.5 rounded
            text-xs font-semibold ${config.labelColor}
            ${config.accentColor} transition-colors
            whitespace-nowrap mt-0.5
          `}
        >
          {action.label}
        </button>
      )}

      {/* Close Button */}
      <button
        onClick={(e) => {
          e.stopPropagation();
          setIsExiting(true);
          setTimeout(onClose, 300);
        }}
        className={`flex-shrink-0 ${config.iconColor} hover:opacity-70 transition-opacity p-0.5`}
        aria-label="Close notification"
      >
        <X className="w-5 h-5" strokeWidth={2.5} />
      </button>

      {/* Progress Bar */}
      <div 
        className={`absolute bottom-0 left-0 h-1 ${config.progressColor} rounded-b-lg animate-shrink`}
        style={{
          animation: 'shrink 4s linear forwards',
        }}
      />
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
    <div className="fixed top-6 right-6 z-50 flex flex-col gap-3 pointer-events-none">
      {toasts.map(toast => (
        <div key={toast.id} className="pointer-events-auto">
          <Toast
            {...toast}
            onClose={() => removeToast(toast.id)}
          />
        </div>
      ))}

      <style>{`
        @keyframes slideInRight {
          from {
            transform: translateX(400px);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }

        .animate-slideInRight {
          animation: slideInRight 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
        }

        @keyframes shrink {
          from {
            width: 100%;
          }
          to {
            width: 0%;
          }
        }

        .animate-shrink {
          animation: shrink 4s linear forwards;
        }
      `}</style>
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
