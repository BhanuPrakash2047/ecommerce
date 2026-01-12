import React from 'react';
import { THEME } from '@/styles/theme';

/**
 * Button Component - Multiple variants
 * Variants: primary, cta, secondary, danger, outline
 * Sizes: sm, md, lg
 * States: loading, disabled
 */
export const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  fullWidth = false,
  icon: Icon = null,
  onClick,
  className = '',
  ...props
}) => {
  const baseStyles = 'font-semibold rounded-md transition-all duration-300 flex items-center justify-center gap-2 focus-visible:outline-2 focus-visible:outline-offset-2';

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2.5 text-base',
    lg: 'px-6 py-3 text-lg',
  };

  const variantStyles = {
    primary: `bg-brand-600 text-white hover:bg-brand-700 shadow-md hover:shadow-lg disabled:bg-gray-400 disabled:shadow-none focus-visible:outline-brand-500`,
    cta: `bg-cta-600 text-white hover:bg-cta-700 shadow-brand hover:shadow-brand-lg disabled:bg-gray-400 disabled:shadow-none focus-visible:outline-cta-600 font-bold scale-100 hover:scale-105 active:scale-95`,
    secondary: `bg-gray-100 text-gray-700 hover:bg-gray-200 border border-gray-300 disabled:bg-gray-50 disabled:text-gray-300 focus-visible:outline-gray-400`,
    danger: `bg-accent-700 text-white hover:bg-accent-800 shadow-md hover:shadow-lg disabled:bg-gray-400 focus-visible:outline-accent-600`,
    outline: `border-2 border-brand-600 text-brand-600 hover:bg-brand-50 disabled:border-gray-400 disabled:text-gray-400 focus-visible:outline-brand-500`,
  };

  const disabledClass = disabled || loading ? 'opacity-70 cursor-not-allowed' : 'cursor-pointer';
  const widthClass = fullWidth ? 'w-full' : '';

  const combinedClass = `${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${disabledClass} ${widthClass} ${className}`;

  return (
    <button
      onClick={onClick}
      disabled={disabled || loading}
      className={combinedClass}
      {...props}
    >
      {loading ? (
        <>
          <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
          <span>{children}</span>
        </>
      ) : (
        <>
          {Icon && <Icon className="w-5 h-5" />}
          {children}
        </>
      )}
    </button>
  );
};

export default Button;
