import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../common';
import { FiArrowRight } from 'react-icons/fi';
/**
 * Hero Section Component
 * Full-screen hero with animated background, quote, and CTAs
 */
export const HeroSection = ({
  title = 'Crunch, Munch & Savor Every Moment',
  subtitle = 'Premium snacks delivered fresh to your doorstep',
  quote = '"Life is uncertain. Eat snacks first." – Jim Gaffigan',
  backgroundImage = '/hero.jpg',
  ctaText = 'Explore Products',
  ctaLink = '/products',
}) => {
  const scrollToProducts = () => {
    document.getElementById('products-section')?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <section className="relative h-screen min-h-96 md:min-h-screen flex items-center justify-center overflow-hidden">
      {/* Background Image with Overlay */}
      <div
        className="absolute inset-0 bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage: `url(${backgroundImage})`,
        }}
      >
        {/* Dark Gradient Overlay */}
        <div className="absolute inset-0 bg-gradient-to-r from-black/70 via-black/50 to-transparent" />

        {/* Animated gradient overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent opacity-30" />
      </div>

      {/* Floating elements for decoration */}
      <div className="absolute -top-20 -left-20 w-96 h-96 bg-brand-500/10 rounded-full blur-3xl animate-float" />
      <div className="absolute -bottom-40 -right-20 w-96 h-96 bg-accent-500/10 rounded-full blur-3xl animate-float-slow" />

      {/* Content */}
      <div className="relative z-10 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center animate-fade-in-up">
        {/* Badge */}
        <div className="mb-6 inline-block">
          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-full px-6 py-2 text-white text-sm font-semibold hover:bg-white/20 transition-all duration-300">
            ✨ Premium Snacks Selection
          </div>
        </div>

        {/* Main Title */}
        <h1 className="text-4xl md:text-5xl lg:text-6xl font-black font-display text-white mb-4 leading-tight">
          <span className="bg-gradient-to-r from-yellow-200 via-brand-300 to-pink-300 bg-clip-text text-transparent animate-gradient-flow">
            {title}
          </span>
        </h1>

        {/* Subtitle */}
        <p className="text-lg md:text-xl text-gray-200 mb-8 max-w-2xl mx-auto leading-relaxed">
          {subtitle}
        </p>

        {/* Quote */}
        <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-lg p-6 mb-12 max-w-2xl mx-auto hover:bg-white/10 transition-all duration-300">
          <p className="text-white text-lg md:text-xl italic font-semibold">
            {quote}
          </p>
        </div>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-12">
          <Button
            variant="cta"
            size="lg"
            onClick={scrollToProducts}
            icon={FiArrowRight}
            className="group shadow-brand hover:shadow-brand-lg"
          >
            {ctaText}
            <span className="group-hover:translate-x-1 transition-transform">\u2192</span>
          </Button>

          <Button
            variant="outline"
            size="lg"
            asChild
            className="border-white text-white hover:bg-white/10"
          >
            <Link to="/about">Learn More</Link>
          </Button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-4 md:gap-8 max-w-2xl mx-auto">
          <div className="group">
            <p className="text-3xl md:text-4xl font-bold text-brand-300 group-hover:scale-110 transition-transform">
              10K+
            </p>
            <p className="text-gray-300 text-sm">Happy Customers</p>
          </div>
          <div className="group">
            <p className="text-3xl md:text-4xl font-bold text-yellow-300 group-hover:scale-110 transition-transform">
              500+
            </p>
            <p className="text-gray-300 text-sm">Snack Varieties</p>
          </div>
          <div className="group">
            <p className="text-3xl md:text-4xl font-bold text-pink-300 group-hover:scale-110 transition-transform">
              24/7
            </p>
            <p className="text-gray-300 text-sm">Fast Delivery</p>
          </div>
        </div>
      </div>

      {/* Scroll Indicator */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-10 animate-bounce">
        <div className="text-white/60 text-center">
          <p className="text-sm mb-2">Scroll to explore</p>
          <svg
            className="w-6 h-6 mx-auto animate-bounce"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 14l-7 7m0 0l-7-7m7 7V3"
            />
          </svg>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
