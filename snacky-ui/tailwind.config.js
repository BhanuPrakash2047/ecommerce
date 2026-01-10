/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      // ========== FONTS ==========
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
        display: ['Poppins', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },

      colors: {
        // Primary Brand Colors
        brand: {
          50: '#fff7ed',   // Lightest background
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#ff8c00',  // PRIMARY - Main brand color (ENHANCED)
          600: '#ea580c',  // ENHANCED - Primary buttons, headings
          700: '#c2410c',
          800: '#9a3412',
          900: '#7c2d12',
        },

        // Accent Red
        accent: {
          50: '#fef2f2',
          100: '#fee2e2',
          200: '#fecaca',
          300: '#fca5a5',
          400: '#f87171',
          500: '#ef4444',
          600: '#e63946',  // ENHANCED - Stronger, more vibrant
          700: '#dc2626',
          800: '#991b1b',
          900: '#7f1d1d',
        },

        // Pink Gradient End
        pink: {
          50: '#fdf2f8',
          100: '#fce7f3',
          200: '#fbcfe8',
          300: '#f9a8d4',
          400: '#f472b6',
          500: '#ec4899',
          600: '#db2777',  // Gradient endings
          700: '#be185d',
          800: '#831843',
          900: '#500724',
        },

        // Emerald for CTAs (NEW - for Add to Cart urgency)
        cta: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#10b981',  // ENHANCED - Emerald for urgency
          700: '#059669',
          800: '#047857',
          900: '#065f46',
        },

        // Yellow/Amber Accents
        yellow: {
          50: '#fefce8',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#facc15',
          500: '#eab308',
          600: '#ca8a04',
          700: '#a16207',
          800: '#854d0e',
          900: '#713f12',
        },

        amber: {
          50: '#fffbeb',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',  // Warm accents
          700: '#b45309',
          800: '#92400e',
          900: '#78350f',
        },

        // Neon Orange for Flash Sales (NEW)
        neon: {
          50: '#fff8f0',
          100: '#ffe8d6',
          200: '#ffd4a3',
          300: '#ffb870',
          400: '#ff9c3d',
          500: '#ff8c00',
          600: '#ff6b35',  // Neon Orange for hot deals
          700: '#e55100',
          800: '#c43d00',
          900: '#a13500',
        },

        // Deep Red for Premium Items (NEW)
        deep: {
          50: '#fef2f2',
          100: '#fee2e2',
          200: '#fecaca',
          300: '#fca5a5',
          400: '#f87171',
          500: '#ef4444',
          600: '#dc2626',
          700: '#a4161a',  // Deep red for premium
          800: '#7f1d1d',
          900: '#65070f',
        },

        // Healthy/Success - Emerald
        healthy: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#34d399',  // Healthy category
          700: '#059669',
          800: '#047857',
          900: '#065f46',
        },

        // Teal for Special Tags
        special: {
          50: '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6',  // Special tags
          600: '#0d9488',
          700: '#0f766e',
          800: '#115e59',
          900: '#134e4a',
        },

        // Rose for Premium Items
        rose: {
          50: '#fff7f2',
          100: '#ffe4e6',
          200: '#fecdd3',
          300: '#fed7e2',
          400: '#fb7185',
          500: '#f43f5e',  // Premium items
          600: '#e11d48',
          700: '#be123c',
          800: '#9d174d',
          900: '#831843',
        },

        // Neutral Grays
        gray: {
          50: '#f9fafb',
          100: '#f3f4f6',  // Subtle backgrounds
          200: '#e5e7eb',  // Borders
          300: '#d1d5db',
          400: '#9ca3af',  // Disabled, borders
          500: '#6b7280',
          600: '#4b5563',  // Secondary text
          700: '#374151',  // Body text
          800: '#1f2937',
          900: '#111827',  // Footer, dark text
        },
      },

      // Custom gradients
      backgroundImage: {
        'gradient-brand': 'linear-gradient(135deg, rgb(255, 140, 0) 0%, rgb(230, 57, 70) 100%)',
        'gradient-hero': 'linear-gradient(135deg, rgb(234, 88, 12) 0%, rgb(220, 38, 38) 50%, rgb(219, 39, 119) 100%)',
        'gradient-button-hover': 'linear-gradient(135deg, rgb(230, 57, 70) 0%, rgb(219, 39, 119) 100%)',
        'gradient-warm': 'linear-gradient(135deg, rgb(234, 88, 12) 0%, rgb(220, 38, 38) 100%)',
        
        // Product Category Gradients
        'gradient-chips': 'linear-gradient(135deg, rgb(250, 204, 21) 0%, rgb(255, 140, 0) 100%)',
        'gradient-cookies': 'linear-gradient(135deg, rgb(217, 119, 6) 0%, rgb(180, 83, 9) 100%)',
        'gradient-chocolate': 'linear-gradient(135deg, rgb(244, 63, 94) 0%, rgb(220, 38, 38) 100%)',
        'gradient-popcorn': 'linear-gradient(135deg, rgb(250, 204, 21) 0%, rgb(252, 211, 77) 100%)',
        'gradient-healthy': 'linear-gradient(135deg, rgb(52, 211, 153) 0%, rgb(20, 184, 166) 100%)',
        'gradient-pretzels': 'linear-gradient(135deg, rgb(255, 140, 0) 0%, rgb(217, 119, 6) 100%)',
        
        // Page & Card Backgrounds
        'gradient-page-bg': 'linear-gradient(135deg, rgb(255, 247, 237) 0%, rgb(255, 251, 235) 50%, rgb(254, 252, 232) 100%)',
        'gradient-card-1': 'linear-gradient(135deg, rgb(255, 247, 237) 0%, rgb(254, 252, 232) 100%)',
        'gradient-card-2': 'linear-gradient(135deg, rgb(255, 247, 237) 0%, rgb(255, 228, 181) 100%)',
        'gradient-special-section': 'linear-gradient(135deg, rgb(234, 88, 12) 0%, rgb(220, 38, 38) 100%)',
      },

      // Custom shadows for depth
      boxShadow: {
        'brand': '0 10px 25px -5px rgba(255, 140, 0, 0.2)',
        'brand-lg': '0 20px 40px -10px rgba(255, 140, 0, 0.3)',
        'card': '0 4px 6px -1px rgba(0, 0, 0, 0.05)',
        'card-hover': '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
      },

      // Custom transitions
      transitionDuration: {
        '400': '400ms',
      },

      // ========== ANIMATIONS ==========
      animation: {
        // Fade animations
        'fade-in': 'fadeIn 0.6s ease-in-out',
        'fade-out': 'fadeOut 0.6s ease-in-out',
        'fade-in-up': 'fadeInUp 0.6s ease-out',
        'fade-in-down': 'fadeInDown 0.6s ease-out',
        'fade-in-left': 'fadeInLeft 0.6s ease-out',
        'fade-in-right': 'fadeInRight 0.6s ease-out',

        // Scale animations - Great for product cards, CTAs
        'scale-in': 'scaleIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)',
        'scale-bounce': 'scaleBounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1)',
        'scale-pulse': 'scalePulse 2s ease-in-out infinite',

        // Slide animations - For UI transitions
        'slide-in-left': 'slideInLeft 0.5s ease-out',
        'slide-in-right': 'slideInRight 0.5s ease-out',
        'slide-in-up': 'slideInUp 0.5s ease-out',
        'slide-in-down': 'slideInDown 0.5s ease-out',

        // Pulse & attention animations
        'pulse-glow': 'pulseGlow 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'bounce-slow': 'bounceSlowCustom 2s ease-in-out infinite',
        'wiggle': 'wiggle 0.5s ease-in-out',
        'shake': 'shake 0.4s ease-in-out',

        // Rotation animations - For loading states
        'spin-slow': 'spin 3s linear infinite',
        'rotate-x': 'rotateX 1s ease-in-out infinite',

        // Shimmer - For skeleton loaders
        'shimmer': 'shimmer 2s infinite',
        'shimmer-slow': 'shimmer 3s infinite',

        // Gradient animations
        'gradient-shift': 'gradientShift 3s ease infinite',
        'gradient-flow': 'gradientFlow 4s ease infinite',

        // Float animations - Subtle, elegant
        'float': 'float 3s ease-in-out infinite',
        'float-slow': 'floatSlow 4s ease-in-out infinite',

        // Pulse with color - For badges
        'pulse-brand': 'pulseBrand 2s ease-in-out infinite',
      },

      // ========== KEYFRAMES ==========
      keyframes: {
        // Fade Keyframes
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        fadeOut: {
          '0%': { opacity: '1' },
          '100%': { opacity: '0' },
        },
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInDown: {
          '0%': { opacity: '0', transform: 'translateY(-20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInLeft: {
          '0%': { opacity: '0', transform: 'translateX(-20px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        fadeInRight: {
          '0%': { opacity: '0', transform: 'translateX(20px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },

        // Scale Keyframes
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        scaleBounce: {
          '0%, 100%': { transform: 'scale(1)' },
          '50%': { transform: 'scale(1.05)' },
        },
        scalePulse: {
          '0%, 100%': { transform: 'scale(1)' },
          '50%': { transform: 'scale(1.02)' },
        },

        // Slide Keyframes
        slideInLeft: {
          '0%': { transform: 'translateX(-40px)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        slideInRight: {
          '0%': { transform: 'translateX(40px)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        slideInUp: {
          '0%': { transform: 'translateY(40px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideInDown: {
          '0%': { transform: 'translateY(-40px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },

        // Pulse & Attention Keyframes
        pulseGlow: {
          '0%, 100%': { opacity: '1', boxShadow: '0 0 0 0 rgba(255, 140, 0, 0.7)' },
          '50%': { boxShadow: '0 0 0 10px rgba(255, 140, 0, 0)' },
        },
        bounceSlowCustom: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        wiggle: {
          '0%, 100%': { transform: 'rotate(0deg)' },
          '25%': { transform: 'rotate(-2deg)' },
          '75%': { transform: 'rotate(2deg)' },
        },
        shake: {
          '0%, 100%': { transform: 'translateX(0)' },
          '25%': { transform: 'translateX(-5px)' },
          '75%': { transform: 'translateX(5px)' },
        },

        // Rotation Keyframes
        rotateX: {
          '0%': { transform: 'rotateX(0deg)' },
          '100%': { transform: 'rotateX(360deg)' },
        },

        // Shimmer Keyframe
        shimmer: {
          '0%': { backgroundPosition: '-1000px 0' },
          '100%': { backgroundPosition: '1000px 0' },
        },

        // Gradient Keyframes
        gradientShift: {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
        },
        gradientFlow: {
          '0%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
          '100%': { backgroundPosition: '0% 50%' },
        },

        // Float Keyframes
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        floatSlow: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-30px)' },
        },

        // Pulse Brand Keyframe
        pulseBrand: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.8' },
        },
      },
    },
  },
  plugins: [],
};
