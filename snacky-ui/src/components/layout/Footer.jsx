import React from 'react';
import { Link } from 'react-router-dom';
import { FiFacebook, FiTwitter, FiInstagram, FiMail, FiPhone, FiMapPin } from 'react-icons/fi';

/**
 * Footer Component
 * Responsive footer with links, social media, contact info
 */
export const Footer = () => {
  const currentYear = new Date().getFullYear();

  const footerLinks = {
    company: [
      { label: 'About Us', href: '/about' },
      { label: 'Careers', href: '/careers' },
      { label: 'Blog', href: '/blog' },
      { label: 'Press', href: '/press' },
    ],
    shop: [
      { label: 'All Products', href: '/products' },
      { label: 'Categories', href: '/products' },
      { label: 'Offers', href: '/offers' },
      { label: 'Flash Sale', href: '/flash-sale' },
    ],
    support: [
      { label: 'Help Center', href: '/help' },
      { label: 'Contact Us', href: '/contact' },
      { label: 'Shipping Info', href: '/shipping' },
      { label: 'Returns', href: '/returns' },
    ],
    legal: [
      { label: 'Privacy Policy', href: '/privacy' },
      { label: 'Terms & Conditions', href: '/terms' },
      { label: 'Cookie Policy', href: '/cookies' },
      { label: 'Disclaimer', href: '/disclaimer' },
    ],
  };

  const socialLinks = [
    { icon: FiFacebook, href: '#', label: 'Facebook' },
    { icon: FiTwitter, href: '#', label: 'Twitter' },
    { icon: FiInstagram, href: '#', label: 'Instagram' },
  ];

  return (
    <footer className="bg-gray-900 text-gray-300 pt-16 pb-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Top Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8 mb-12">
          {/* Brand */}
          <div className="lg:col-span-1">
            <div className="flex items-center gap-2 mb-4">
              <div className="bg-gradient-brand p-2 rounded-lg">
                <span className="text-2xl font-bold text-white font-display">🍿</span>
              </div>
              <div>
                <h3 className="text-xl font-bold text-white font-display">SNACKY</h3>
                <p className="text-xs text-brand-400">Crunch & Munch</p>
              </div>
            </div>
            <p className="text-sm text-gray-400 leading-relaxed mb-4">
              Your favorite destination for premium snacks and treats. Delivered fresh to your doorstep!
            </p>

            {/* Social Links */}
            <div className="flex gap-3">
              {socialLinks.map(({ icon: Icon, href, label }) => (
                <a
                  key={label}
                  href={href}
                  aria-label={label}
                  className="w-10 h-10 rounded-full bg-gray-800 flex items-center justify-center text-gray-400 hover:bg-brand-600 hover:text-white transition-all duration-300 transform hover:scale-110"
                >
                  <Icon className="w-5 h-5" />
                </a>
              ))}
            </div>
          </div>

          {/* Links Sections */}
          {Object.entries(footerLinks).map(([category, links]) => (
            <div key={category}>
              <h4 className="text-white font-bold text-sm uppercase tracking-widest mb-4 font-display">
                {category === 'company' && 'Company'}
                {category === 'shop' && 'Shop'}
                {category === 'support' && 'Support'}
                {category === 'legal' && 'Legal'}
              </h4>
              <ul className="space-y-2">
                {links.map(({ label, href }) => (
                  <li key={label}>
                    <Link
                      to={href}
                      className="text-sm text-gray-400 hover:text-brand-400 transition-colors duration-300 relative group"
                    >
                      {label}
                      <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-brand-500 group-hover:w-full transition-all duration-300" />
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Middle Section - Contact Info */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 py-8 border-t border-b border-gray-800 mb-8">
          <div className="flex items-start gap-3 group">
            <div className="w-10 h-10 rounded-lg bg-brand-600 flex items-center justify-center text-white flex-shrink-0 group-hover:shadow-brand transition-shadow">
              <FiMapPin className="w-5 h-5" />
            </div>
            <div>
              <p className="text-sm text-gray-400">Address</p>
              <p className="text-white font-semibold">123 Snack Street, Food City, FC 12345</p>
            </div>
          </div>

          <div className="flex items-start gap-3 group">
            <div className="w-10 h-10 rounded-lg bg-brand-600 flex items-center justify-center text-white flex-shrink-0 group-hover:shadow-brand transition-shadow">
              <FiPhone className="w-5 h-5" />
            </div>
            <div>
              <p className="text-sm text-gray-400">Phone</p>
              <p className="text-white font-semibold">+1 (555) 123-4567</p>
            </div>
          </div>

          <div className="flex items-start gap-3 group">
            <div className="w-10 h-10 rounded-lg bg-brand-600 flex items-center justify-center text-white flex-shrink-0 group-hover:shadow-brand transition-shadow">
              <FiMail className="w-5 h-5" />
            </div>
            <div>
              <p className="text-sm text-gray-400">Email</p>
              <p className="text-white font-semibold">hello@snacky.com</p>
            </div>
          </div>
        </div>

        {/* Bottom Section */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <p className="text-sm text-gray-400">
            &copy; {currentYear} SNACKY. All rights reserved.
          </p>

          {/* Newsletter */}
          <div className="flex gap-2">
            <input
              type="email"
              placeholder="Enter your email"
              className="px-4 py-2 bg-gray-800 text-white rounded-lg border border-gray-700 focus:border-brand-600 focus:ring-2 focus:ring-brand-200 transition-all duration-300 text-sm"
            />
            <button className="px-4 py-2 bg-gradient-brand text-white rounded-lg font-semibold hover:shadow-brand transition-all duration-300 transform hover:scale-105 active:scale-95 text-sm">
              Subscribe
            </button>
          </div>

          {/* Payment Methods */}
          <div className="flex items-center gap-3">
            <span className="text-xs text-gray-400 font-semibold">WE ACCEPT:</span>
            <div className="flex gap-2">
              {['💳', '🏦', '📱'].map((emoji, i) => (
                <div
                  key={i}
                  className="w-8 h-8 bg-gray-800 rounded flex items-center justify-center text-lg hover:bg-gray-700 transition-colors cursor-pointer"
                >
                  {emoji}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Floating Badge */}
        <div className="mt-8 text-center">
          <div className="inline-block bg-gradient-to-r from-brand-600 to-accent-600 text-white px-4 py-2 rounded-full text-sm font-semibold">
            ✨ Made with love for snack lovers ✨
          </div>
        </div>
      </div>

      {/* Scroll to Top Button */}
      <button
        onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
        className="fixed bottom-8 right-8 w-12 h-12 bg-gradient-brand text-white rounded-full flex items-center justify-center shadow-lg hover:shadow-brand hover:scale-110 transition-all duration-300 opacity-0 hover:opacity-100"
        aria-label="Scroll to top"
      >
        <span className="text-xl">↑</span>
      </button>
    </footer>
  );
};

export default Footer;
