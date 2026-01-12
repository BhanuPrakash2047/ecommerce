import React from 'react';

const Badge = ({ label, variant = 'slate' }) => {
  const variantClasses = {
    slate: 'bg-slate-100 text-slate-800',
    orange: 'bg-orange-100 text-orange-800',
    blue: 'bg-blue-100 text-blue-800',
    green: 'bg-green-100 text-green-800',
    red: 'bg-red-100 text-red-800',
    yellow: 'bg-yellow-100 text-yellow-800',
  };

  return (
    <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold ${variantClasses[variant]}`}>
      {label}
    </span>
  );
};

export default Badge;
