import { useState, useCallback } from 'react';

/**
 * Custom hook for form validation
 * Provides consistent validation patterns across all forms
 * Shows errors below fields (most common UX pattern)
 */
export const useFormValidation = (initialFormState) => {
  const [formData, setFormData] = useState(initialFormState);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  // Validation rules
  const validationRules = {
    email: (value) => {
      if (!value.trim()) return 'Email is required';
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(value)) return 'Please enter a valid email address';
      return null;
    },
    
    password: (value, minLength = 8) => {
      if (!value.trim()) return 'Password is required';
      if (value.length < minLength) return `Password must be at least ${minLength} characters long`;
      return null;
    },
    
    confirmPassword: (value, password) => {
      if (!value.trim()) return 'Please confirm your password';
      if (value !== password) return 'Passwords do not match';
      return null;
    },

    fullName: (value) => {
      if (!value.trim()) return 'Full name is required';
      if (value.trim().length < 2) return 'Full name must be at least 2 characters';
      return null;
    },

    phone: (value) => {
      if (!value.trim()) return 'Phone number is required';
      const phoneRegex = /^[0-9]{10}$/;
      if (!phoneRegex.test(value.replace(/\D/g, ''))) {
        return 'Phone number must be 10 digits';
      }
      return null;
    },

    pinCode: (value) => {
      if (!value.trim()) return 'PIN code is required';
      const pinRegex = /^[0-9]{6}$/;
      if (!pinRegex.test(value)) return 'PIN code must be 6 digits';
      return null;
    },

    zipCode: (value) => {
      if (!value.trim()) return 'ZIP code is required';
      const zipRegex = /^[0-9]{5,6}$/;
      if (!zipRegex.test(value)) return 'ZIP code must be 5-6 digits';
      return null;
    },

    required: (value) => {
      if (!value || !String(value).trim()) return 'This field is required';
      return null;
    },

    minLength: (value, minLength) => {
      if (!value) return 'This field is required';
      if (String(value).length < minLength) {
        return `Must be at least ${minLength} characters`;
      }
      return null;
    },

    maxLength: (value, maxLength) => {
      if (String(value).length > maxLength) {
        return `Must be no more than ${maxLength} characters`;
      }
      return null;
    },

    number: (value) => {
      if (!value) return 'This field is required';
      if (isNaN(value)) return 'This must be a number';
      return null;
    },

    url: (value) => {
      if (!value.trim()) return 'URL is required';
      try {
        new URL(value);
        return null;
      } catch {
        return 'Please enter a valid URL';
      }
    },

    addressLine: (value) => {
      if (!value.trim()) return 'Address is required';
      if (value.trim().length < 5) return 'Address must be at least 5 characters';
      return null;
    },

    city: (value) => {
      if (!value.trim()) return 'City is required';
      return null;
    },

    state: (value) => {
      if (!value.trim()) return 'State is required';
      return null;
    },

    select: (value) => {
      if (!value) return 'Please select an option';
      return null;
    },

    checkbox: (value) => {
      if (!value) return 'This must be checked';
      return null;
    },

    textarea: (value, minLength = 10) => {
      if (!value.trim()) return 'This field is required';
      if (value.trim().length < minLength) {
        return `Must be at least ${minLength} characters`;
      }
      return null;
    },
  };

  // Validate single field
  const validateField = useCallback((fieldName, value, rules = {}) => {
    if (!rules[fieldName]) return null;
    
    const rule = rules[fieldName];
    if (typeof rule === 'function') {
      return rule(value);
    }
    
    if (typeof rule === 'string' && validationRules[rule]) {
      return validationRules[rule](value);
    }

    if (Array.isArray(rule)) {
      for (const singleRule of rule) {
        let error = null;
        if (typeof singleRule === 'function') {
          error = singleRule(value);
        } else if (typeof singleRule === 'string' && validationRules[singleRule]) {
          error = validationRules[singleRule](value);
        }
        if (error) return error;
      }
    }

    return null;
  }, [validationRules]);

  // Validate all fields
  const validateForm = useCallback((data, rules) => {
    const newErrors = {};
    Object.keys(rules).forEach((fieldName) => {
      const error = validateField(fieldName, data[fieldName], rules);
      if (error) {
        newErrors[fieldName] = error;
      }
    });
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [validateField]);

  // Handle field blur
  const handleBlur = useCallback((fieldName, rules) => {
    setTouched(prev => ({
      ...prev,
      [fieldName]: true,
    }));
    const error = validateField(fieldName, formData[fieldName], rules);
    setErrors(prev => ({
      ...prev,
      [fieldName]: error,
    }));
  }, [formData, validateField]);

  // Handle field change
  const handleChange = useCallback((e, rules = {}) => {
    const { name, value, type, checked } = e.target;
    const fieldValue = type === 'checkbox' ? checked : value;

    setFormData(prev => ({
      ...prev,
      [name]: fieldValue,
    }));

    // Validate if field was touched
    if (touched[name]) {
      const error = validateField(name, fieldValue, rules);
      setErrors(prev => ({
        ...prev,
        [name]: error,
      }));
    }
  }, [touched, validateField]);

  // Reset form
  const resetForm = useCallback(() => {
    setFormData(initialFormState);
    setErrors({});
    setTouched({});
  }, [initialFormState]);

  // Set form data
  const setFormValues = useCallback((values) => {
    setFormData(values);
  }, []);

  // Get error message for field
  const getFieldError = useCallback((fieldName) => {
    return touched[fieldName] ? errors[fieldName] : null;
  }, [errors, touched]);

  return {
    formData,
    errors,
    touched,
    handleChange,
    handleBlur,
    validateField,
    validateForm,
    resetForm,
    setFormValues,
    getFieldError,
  };
};
