// src/styles/theme.js
// ============================================
// SNACKY - Design System & Color Palette
// ============================================
// This file centralizes all design tokens for consistent UI across the entire application

export const THEME = {
  // ========== FONTS ==========
  fonts: {
    primary: 'Inter, system-ui, sans-serif',
    display: 'Poppins, system-ui, sans-serif',
    mono: 'JetBrains Mono, monospace',
  },

  // ========== TYPOGRAPHY SIZES ==========
  fontSize: {
    xs: '0.75rem',       // 12px
    sm: '0.875rem',      // 14px
    base: '1rem',        // 16px
    lg: '1.125rem',      // 18px
    xl: '1.25rem',       // 20px
    '2xl': '1.5rem',     // 24px
    '3xl': '1.875rem',   // 30px
    '4xl': '2.25rem',    // 36px
    '5xl': '3rem',       // 48px
  },

  fontWeight: {
    thin: 100,
    extralight: 200,
    light: 300,
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
    extrabold: 800,
    black: 900,
  },

  // ========== ANIMATIONS ==========
  animations: {
    // Fade animations
    fadeIn: 'fadeIn 0.6s ease-in-out',
    fadeOut: 'fadeOut 0.6s ease-in-out',
    fadeInUp: 'fadeInUp 0.6s ease-out',
    fadeInDown: 'fadeInDown 0.6s ease-out',
    fadeInLeft: 'fadeInLeft 0.6s ease-out',
    fadeInRight: 'fadeInRight 0.6s ease-out',

    // Scale animations - For product cards, CTAs
    scaleIn: 'scaleIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)',
    scaleBounce: 'scaleBounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1)',
    scalePulse: 'scalePulse 2s ease-in-out infinite',

    // Slide animations - For UI transitions
    slideInLeft: 'slideInLeft 0.5s ease-out',
    slideInRight: 'slideInRight 0.5s ease-out',
    slideInUp: 'slideInUp 0.5s ease-out',
    slideInDown: 'slideInDown 0.5s ease-out',

    // Attention animations
    pulseGlow: 'pulseGlow 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
    bounceSlow: 'bounceSlowCustom 2s ease-in-out infinite',
    wiggle: 'wiggle 0.5s ease-in-out',
    shake: 'shake 0.4s ease-in-out',

    // Rotation animations
    spinSlow: 'spin 3s linear infinite',
    rotateX: 'rotateX 1s ease-in-out infinite',

    // Shimmer - For skeleton loaders
    shimmer: 'shimmer 2s infinite',
    shimmerSlow: 'shimmer 3s infinite',

    // Gradient animations
    gradientShift: 'gradientShift 3s ease infinite',
    gradientFlow: 'gradientFlow 4s ease infinite',

    // Float animations - Subtle, elegant
    float: 'float 3s ease-in-out infinite',
    floatSlow: 'floatSlow 4s ease-in-out infinite',

    // Pulse with color - For badges
    pulseBrand: 'pulseBrand 2s ease-in-out infinite',
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

  // ========== COLOR TOKENS ==========
  colors: {
    // PRIMARY - Main Brand Color (Orange)
    primary: {
      main: '#ff8c00',      // Use for: Main CTAs, primary buttons
      dark: '#ea580c',      // Use for: Button hover, headings
      light: '#ffedd5',     // Use for: Light backgrounds
      lighter: '#fff7ed',   // Use for: Page backgrounds
    },

    // ACCENT - Action & Emphasis (Red)
    accent: {
      main: '#ef4444',      // Use for: Highlights, badges
      dark: '#e63946',      // Use for: Hover states, strong emphasis
      light: '#fee2e2',     // Use for: Light accents
    },

    // CTA - Call to Action (Emerald - NEW)
    cta: {
      main: '#10b981',      // Use for: "Add to Cart", checkout buttons
      light: '#dcfce7',     // Use for: Success states, completion
    },

    // SPECIAL - Hot Deals & Flash Sales (Neon)
    special: {
      hot: '#ff6b35',       // Use for: Flash sale badges, limited offers
      premium: '#a4161a',   // Use for: Premium items, exclusive products
    },

    // NEUTRAL - Typography & Backgrounds
    neutral: {
      darkest: '#111827',   // Use for: Footer background, dark text
      dark: '#374151',      // Use for: Body text, primary copy
      medium: '#4b5563',    // Use for: Secondary text, descriptions
      light: '#9ca3af',     // Use for: Disabled states, subtle text
      lighter: '#e5e7eb',   // Use for: Borders, dividers
      lightest: '#f3f4f6',  // Use for: Subtle backgrounds
      white: '#ffffff',     // Use for: Card backgrounds, modals
    },

    // CATEGORY COLORS - Product Classification
    categories: {
      chips: '#facc15',           // Yellow - Savory/Chips
      cookies: '#d97706',         // Amber - Sweet/Cookies
      chocolate: '#f43f5e',       // Rose - Chocolate/Premium
      popcorn: '#fbbf24',         // Amber-light - Popcorn
      healthy: '#34d399',         // Emerald - Healthy options
      pretzels: '#ff8c00',        // Orange - Pretzels/Salty
    },
  },

  // ========== TYPOGRAPHY ==========
  typography: {
    // Heading Styles
    h1: {
      fontSize: '2.5rem',         // 40px
      fontWeight: '700',
      lineHeight: '1.2',
      letterSpacing: '-0.02em',
      color: '#111827',           // gray-900
    },
    h2: {
      fontSize: '2rem',           // 32px
      fontWeight: '600',
      lineHeight: '1.3',
      letterSpacing: '-0.01em',
      color: '#111827',           // gray-900
    },
    h3: {
      fontSize: '1.5rem',         // 24px
      fontWeight: '600',
      lineHeight: '1.4',
      color: '#111827',           // gray-900
    },
    h4: {
      fontSize: '1.25rem',        // 20px
      fontWeight: '600',
      lineHeight: '1.5',
      color: '#374151',           // gray-700
    },

    // Body Styles
    body: {
      fontSize: '1rem',           // 16px
      fontWeight: '400',
      lineHeight: '1.6',
      color: '#374151',           // gray-700
    },
    bodySmall: {
      fontSize: '0.875rem',       // 14px
      fontWeight: '400',
      lineHeight: '1.5',
      color: '#4b5563',           // gray-600
    },
    caption: {
      fontSize: '0.75rem',        // 12px
      fontWeight: '500',
      lineHeight: '1.4',
      color: '#9ca3af',           // gray-400
    },
  },

  // ========== SPACING ==========
  spacing: {
    xs: '0.25rem',    // 4px
    sm: '0.5rem',     // 8px
    md: '1rem',       // 16px
    lg: '1.5rem',     // 24px
    xl: '2rem',       // 32px
    xxl: '3rem',      // 48px
    xxxl: '4rem',     // 64px
  },

  // ========== BORDER RADIUS ==========
  borderRadius: {
    xs: '0.25rem',    // 4px - Small elements
    sm: '0.5rem',     // 8px - Inputs, buttons
    md: '0.75rem',    // 12px - Cards
    lg: '1rem',       // 16px - Larger cards
    xl: '1.5rem',     // 24px - Hero sections
    full: '9999px',   // Circular
  },

  // ========== SHADOWS ==========
  shadows: {
    none: 'none',
    xs: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
    brand: '0 10px 25px -5px rgba(255, 140, 0, 0.2)',
    brandLg: '0 20px 40px -10px rgba(255, 140, 0, 0.3)',
  },

  // ========== TRANSITIONS ==========
  transitions: {
    fast: '150ms ease-in-out',
    normal: '300ms ease-in-out',
    slow: '500ms ease-in-out',
  },

  // ========== BREAKPOINTS ==========
  breakpoints: {
    xs: '320px',
    sm: '640px',
    md: '768px',
    lg: '1024px',
    xl: '1280px',
    xxl: '1536px',
  },
};

// ============================================
// USAGE GUIDELINES
// ============================================

/*
COMPONENT COLOR MAPPING:

Buttons:
  - Primary CTA: brand.main (#ff8c00)
  - Add to Cart: cta.main (#10b981)
  - Secondary: neutral.light (#9ca3af)
  - Danger: accent.dark (#e63946)
  - Hover: Darker shade of base color

Badges:
  - Flash Sale: special.hot (#ff6b35)
  - Premium: special.premium (#a4161a)
  - In Stock: cta.main (#10b981)
  - Category: categories[type]

Cards:
  - Background: neutral.white (#ffffff)
  - Border: neutral.lighter (#e5e7eb)
  - Hover Shadow: shadows.lg
  - Corner Radius: borderRadius.md

Text:
  - Headings: neutral.darkest (#111827)
  - Body: neutral.dark (#374151)
  - Secondary: neutral.medium (#4b5563)
  - Disabled: neutral.light (#9ca3af)

Backgrounds:
  - Page: gradient-page-bg or neutral.lightest
  - Sections: colors.primary.lighter
  - Hover: Slight opacity increase of base color

Borders & Dividers:
  - Default: neutral.lighter (#e5e7eb)
  - Subtle: neutral.lightest (#f3f4f6)
  - Dark: neutral.medium (#4b5563)

Gradients:
  - Hero Section: gradient-hero
  - Brand: gradient-brand
  - Product Cards: gradient-[category]
  - Special Sections: gradient-special-section
*/

// ============================================
// COMMON COMBINATIONS (Tailwind Classes)
// ============================================

export const commonStyles = {
  // Button Styles
  primaryButton: 'bg-brand-600 hover:bg-brand-700 text-white px-4 py-2 rounded-md font-semibold transition-colors duration-300 shadow-md hover:shadow-lg',
  secondaryButton: 'bg-neutral-light hover:bg-neutral-lighter text-neutral-dark px-4 py-2 rounded-md font-medium transition-colors duration-300',
  ctaButton: 'bg-cta-600 hover:bg-cta-700 text-white px-6 py-3 rounded-lg font-bold transition-colors duration-300 shadow-brand hover:shadow-brand-lg',

  // Card Styles
  card: 'bg-white rounded-lg border border-neutral-lighter shadow-md hover:shadow-lg transition-shadow duration-300 p-6',
  cardHover: 'hover:border-brand-500 hover:shadow-brand transition-all duration-300',

  // Text Styles
  heading1: 'text-4xl font-bold text-neutral-darkest leading-tight',
  heading2: 'text-2xl font-bold text-neutral-darkest leading-snug',
  heading3: 'text-xl font-semibold text-neutral-darkest',
  bodyText: 'text-neutral-dark leading-relaxed',
  secondaryText: 'text-neutral-medium text-sm',

  // Badge Styles
  badgeFlash: 'bg-neon-600 text-white px-3 py-1 rounded-full text-xs font-bold uppercase',
  badgePremium: 'bg-deep-700 text-white px-3 py-1 rounded-full text-xs font-bold',
  badgeHealthy: 'bg-healthy-600 text-white px-3 py-1 rounded-full text-xs font-semibold',

  // Input Styles
  input: 'w-full px-4 py-2 border border-neutral-lighter rounded-md focus:border-brand-500 focus:ring-2 focus:ring-brand-200 transition-all duration-200',
};
