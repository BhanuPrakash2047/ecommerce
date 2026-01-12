import React, { useState } from 'react';
import { THEME } from '@/styles/theme';

/**
 * Input Component
 * Variants: text, password, email, number, tel
 * Sizes: sm, md, lg
 * States: error, disabled, focused
 */
export const Input = ({
  type = 'text',
  placeholder = '',
  value,
  onChange,
  onBlur,
  error = null,
  disabled = false,
  size = 'md',
  fullWidth = false,
  label = '',
  icon: Icon = null,
  helpText = '',
  required = false,
  className = '',
  ...props
}) => {
  const [isFocused, setIsFocused] = useState(false);

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2.5 text-base',
    lg: 'px-4 py-3 text-lg',
  };

  const baseStyles = `w-full rounded-md border-2 transition-all duration-200 font-sans
    focus:outline-none focus:ring-2 focus:ring-offset-0
    disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed`;

  const borderStyles = error
    ? 'border-accent-600 focus:border-accent-600 focus:ring-accent-200'
    : isFocused
    ? 'border-brand-500 focus:ring-brand-200'
    : 'border-gray-200 hover:border-gray-300';

  const widthClass = fullWidth ? 'w-full' : '';
  const iconClass = Icon ? 'pl-10' : '';

  const inputClass = `${baseStyles} ${sizeStyles[size]} ${borderStyles} ${iconClass} ${widthClass} ${className}`;

  return (
    <div className={widthClass}>
      {label && (
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          {label}
          {required && <span className="text-accent-600 ml-1">*</span>}
        </label>
      )}

      <div className="relative">
        {Icon && (
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            <Icon className="w-5 h-5" />
          </div>
        )}

        <input
          type={type}
          value={value}
          onChange={onChange}
          onBlur={(e) => {
            setIsFocused(false);
            onBlur?.(e);
          }}
          onFocus={() => setIsFocused(true)}
          placeholder={placeholder}
          disabled={disabled}
          className={inputClass}
          {...props}
        />
      </div>

      {error && (
        <p className="text-accent-600 text-sm mt-1 font-medium flex items-center gap-1">
          <span>⚠</span> {error}
        </p>
      )}

      {helpText && !error && (
        <p className="text-gray-500 text-sm mt-1">{helpText}</p>
      )}
    </div>
  );
};

/**
 * Textarea Component
 */
export const Textarea = ({
  placeholder = '',
  value,
  onChange,
  error = null,
  disabled = false,
  rows = 4,
  label = '',
  fullWidth = true,
  required = false,
  className = '',
  ...props
}) => {
  const [isFocused, setIsFocused] = useState(false);

  const baseStyles = `w-full px-4 py-2.5 rounded-md border-2 transition-all duration-200 font-sans resize-none
    focus:outline-none focus:ring-2 focus:ring-offset-0
    disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed`;

  const borderStyles = error
    ? 'border-accent-600 focus:border-accent-600 focus:ring-accent-200'
    : isFocused
    ? 'border-brand-500 focus:ring-brand-200'
    : 'border-gray-200 hover:border-gray-300';

  const textareaClass = `${baseStyles} ${borderStyles} ${className}`;

  return (
    <div>
      {label && (
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          {label}
          {required && <span className="text-accent-600 ml-1">*</span>}
        </label>
      )}

      <textarea
        value={value}
        onChange={onChange}
        onBlur={() => setIsFocused(false)}
        onFocus={() => setIsFocused(true)}
        placeholder={placeholder}
        disabled={disabled}
        rows={rows}
        className={textareaClass}
        {...props}
      />

      {error && (
        <p className="text-accent-600 text-sm mt-1 font-medium">⚠ {error}</p>
      )}
    </div>
  );
};

/**
 * Select Component
 */
export const Select = ({
  options = [],
  value,
  onChange,
  placeholder = 'Select an option',
  error = null,
  disabled = false,
  label = '',
  fullWidth = true,
  required = false,
  className = '',
  ...props
}) => {
  const [isFocused, setIsFocused] = useState(false);

  const baseStyles = `w-full px-4 py-2.5 rounded-md border-2 transition-all duration-200 font-sans cursor-pointer appearance-none
    focus:outline-none focus:ring-2 focus:ring-offset-0
    disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed`;

  const borderStyles = error
    ? 'border-accent-600 focus:border-accent-600 focus:ring-accent-200'
    : isFocused
    ? 'border-brand-500 focus:ring-brand-200'
    : 'border-gray-200 hover:border-gray-300';

  const selectClass = `${baseStyles} ${borderStyles} ${className}`;

  return (
    <div>
      {label && (
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          {label}
          {required && <span className="text-accent-600 ml-1">*</span>}
        </label>
      )}

      <div className="relative">
        <select
          value={value}
          onChange={onChange}
          onBlur={() => setIsFocused(false)}
          onFocus={() => setIsFocused(true)}
          disabled={disabled}
          className={selectClass}
          {...props}
        >
          <option value="">{placeholder}</option>
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2">
          <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
          </svg>
        </div>
      </div>

      {error && (
        <p className="text-accent-600 text-sm mt-1 font-medium">⚠ {error}</p>
      )}
    </div>
  );
};

export default Input;
