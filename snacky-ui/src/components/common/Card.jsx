import React from 'react';

/**
 * Card Component - Base card with shadow & border
 */
export const Card = ({
  children,
  className = '',
  hoverable = false,
  padding = 'p-6',
  onClick,
  ...props
}) => {
  const hoverClass = hoverable ? 'hover:shadow-lg hover:border-brand-500 cursor-pointer transition-all duration-300 transform hover:-translate-y-1' : '';

  return (
    <div
      className={`bg-white rounded-lg border border-gray-200 shadow-md ${hoverClass} ${padding} ${className}`}
      onClick={onClick}
      {...props}
    >
      {children}
    </div>
  );
};

/**
 * Badge Component - Multiple variants & colors
 */
export const Badge = ({
  children,
  variant = 'default',
  size = 'md',
  icon: Icon = null,
  className = '',
  ...props
}) => {
  const sizeStyles = {
    sm: 'px-2 py-1 text-xs',
    md: 'px-3 py-1.5 text-sm',
    lg: 'px-4 py-2 text-base',
  };

  const variantStyles = {
    default: 'bg-gray-200 text-gray-800',
    primary: 'bg-brand-100 text-brand-700',
    success: 'bg-cta-100 text-cta-700',
    danger: 'bg-accent-100 text-accent-700',
    warning: 'bg-yellow-100 text-yellow-800',
    flash: 'bg-neon-600 text-white font-bold animate-pulse-brand',
    premium: 'bg-deep-700 text-white font-semibold',
    healthy: 'bg-healthy-100 text-healthy-700',
    special: 'bg-special-100 text-special-700',
  };

  return (
    <span
      className={`inline-flex items-center gap-1 font-semibold rounded-full ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {Icon && <Icon className="w-4 h-4" />}
      {children}
    </span>
  );
};

/**
 * Avatar Component
 */
export const Avatar = ({
  src = '',
  alt = 'Avatar',
  size = 'md',
  name = '',
  className = '',
  ...props
}) => {
  const sizeStyles = {
    sm: 'w-8 h-8 text-xs',
    md: 'w-10 h-10 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-16 h-16 text-lg',
  };

  const getInitials = (str) => {
    return str
      .split(' ')
      .map(word => word.charAt(0).toUpperCase())
      .slice(0, 2)
      .join('');
  };

  return src ? (
    <img
      src={src}
      alt={alt}
      className={`${sizeStyles[size]} rounded-full object-cover ${className}`}
      {...props}
    />
  ) : (
    <div
      className={`${sizeStyles[size]} rounded-full bg-gradient-brand flex items-center justify-center text-white font-bold ${className}`}
      {...props}
    >
      {getInitials(name)}
    </div>
  );
};

/**
 * Divider Component
 */
export const Divider = ({
  variant = 'horizontal',
  margin = 'my-4',
  className = '',
}) => {
  const dividerClass = variant === 'horizontal' ? 'h-px bg-gray-200' : 'w-px h-full bg-gray-200';
  return <div className={`${dividerClass} ${margin} ${className}`} />;
};

/**
 * Spinner Component
 */
export const Spinner = ({
  size = 'md',
  color = 'brand',
  className = '',
  ...props
}) => {
  const sizeStyles = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
    xl: 'w-16 h-16',
  };

  const colorStyles = {
    brand: 'border-brand-300 border-t-brand-600',
    white: 'border-white/20 border-t-white',
    cta: 'border-cta-300 border-t-cta-600',
  };

  return (
    <div
      className={`${sizeStyles[size]} border-4 ${colorStyles[color]} rounded-full animate-spin ${className}`}
      {...props}
    />
  );
};

/**
 * Skeleton Component - For loading states
 */
export const Skeleton = ({
  count = 1,
  height = 'h-4',
  width = 'w-full',
  circle = false,
  className = '',
}) => {
  return (
    <div className="space-y-2">
      {Array(count)
        .fill(null)
        .map((_, i) => (
          <div
            key={i}
            className={`bg-gray-200 animate-shimmer ${circle ? 'rounded-full w-10 h-10' : `${height} ${width} rounded-md`} ${className}`}
          />
        ))}
    </div>
  );
};

export default { Card, Badge, Avatar, Divider, Spinner, Skeleton };
