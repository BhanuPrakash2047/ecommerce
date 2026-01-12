import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Header, Footer } from '@/components/layout';
import { ProductCard } from '@/components/features';
import { Spinner } from '@/components/common';
import { fetchAllProducts, fetchProductImages } from '@/store/thunks/productThunks';

const ProductsPage = () => {
  const dispatch = useDispatch();
  const { items: products, loading, productImages } = useSelector(state => state.products);

  // Fetch products on mount
  useEffect(() => {
    dispatch(fetchAllProducts());
  }, [dispatch]);

  // Fetch images for all products after they load
  useEffect(() => {
    if (products && products.length > 0) {
      products.forEach(product => {
        // Only fetch if image not already in Redux state
        if (!productImages[product.id]) {
          dispatch(fetchProductImages(product.id));
        }
      });
    }
  }, [products, dispatch, productImages]);

  // Merge products with their stored images from Redux
  const displayProducts = products.map(product => {
    const imageUrl = productImages[product.id] && productImages[product.id].length > 0 
      ? productImages[product.id][0] 
      : product.image || 'https://via.placeholder.com/300x200?text=' + encodeURIComponent(product.name || 'Product');
    
    return {
      ...product,
      image: imageUrl
    };
  });

  return (
    <div className="min-h-screen bg-gradient-to-b from-orange-50 via-white to-emerald-50 overflow-hidden">
      <style>{`
        @keyframes float {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-20px); }
        }
        @keyframes shimmer {
          0% { background-position: -1000px 0; }
          100% { background-position: 1000px 0; }
        }
        @keyframes glow-pulse {
          0%, 100% { box-shadow: 0 0 20px rgba(249, 115, 22, 0.5); }
          50% { box-shadow: 0 0 40px rgba(249, 115, 22, 0.8); }
        }
        @keyframes slide-in-left {
          from { opacity: 0; transform: translateX(-30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes slide-in-right {
          from { opacity: 0; transform: translateX(30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes bounce-in {
          0% { transform: scale(0.3); opacity: 0; }
          50% { opacity: 1; }
          70% { transform: scale(1.05); }
          100% { transform: scale(1); }
        }
        @keyframes rotate-slow {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
        @keyframes pulse-scale {
          0%, 100% { transform: scale(1); }
          50% { transform: scale(1.05); }
        }
        .animate-float { animation: float 3s cubic-bezier(0.4, 0.0, 0.2, 1) infinite; }
        .animate-shimmer { animation: shimmer 2s ease-in-out infinite; }
        .animate-glow-pulse { animation: glow-pulse 2s cubic-bezier(0.4, 0.0, 0.6, 1) infinite; }
        .animate-slide-in-left { animation: slide-in-left 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-slide-in-right { animation: slide-in-right 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-bounce-in { animation: bounce-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; }
        .animate-rotate-slow { animation: rotate-slow 20s linear infinite; }
        .animate-pulse-scale { animation: pulse-scale 2s ease-in-out infinite; }
      `}</style>
      <Header />

      {/* PREMIUM HERO SECTION */}
      <section className="relative overflow-hidden pt-16 pb-24">
        {/* Animated Background Elements */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-orange-400/40 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-emerald-400/40 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute top-1/2 right-0 w-80 h-80 bg-blue-300/20 rounded-full blur-3xl animate-float"></div>
        </div>

        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center space-y-8">
            {/* Badge */}
            <div className="inline-flex items-center gap-2 bg-gradient-to-r from-orange-100 to-red-100 text-orange-700 px-4 py-2 rounded-full w-fit border border-orange-200 animate-slide-in-left mx-auto">
              <span className="text-2xl animate-pulse-scale">🎉</span>
              <span className="text-sm font-semibold">Complete Snack Selection</span>
            </div>

            {/* Main Headline */}
            <h1 className="text-5xl md:text-7xl font-black bg-gradient-to-r from-orange-600 via-red-500 to-emerald-600 bg-clip-text text-transparent leading-tight animate-slide-in-left" style={{ animationDelay: '0.1s' }}>
              Indulge Your Cravings Now! 🍿
            </h1>

            {/* Subheadline */}
            <p className="text-xl text-slate-600 leading-relaxed max-w-2xl mx-auto animate-slide-in-left" style={{ animationDelay: '0.2s' }}>
              Every snack is carefully curated for maximum flavor. Fresh stock, unbeatable prices, and lightning-fast delivery!
            </p>

            {/* Trust Badges */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center animate-slide-in-left" style={{ animationDelay: '0.3s' }}>
              <div className="flex items-center gap-3 bg-emerald-50 border border-emerald-200 rounded-lg px-4 py-3">
                <span className="text-xl">✓</span>
                <span className="text-slate-700 font-medium text-sm">100% Fresh & Premium</span>
              </div>
              <div className="flex items-center gap-3 bg-blue-50 border border-blue-200 rounded-lg px-4 py-3">
                <span className="text-xl">⚡</span>
                <span className="text-slate-700 font-medium text-sm">Same Day Delivery</span>
              </div>
              <div className="flex items-center gap-3 bg-orange-50 border border-orange-200 rounded-lg px-4 py-3">
                <span className="text-xl">🎯</span>
                <span className="text-slate-700 font-medium text-sm">Best Prices Guaranteed</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* URGENCY BANNER */}
      {/* <section className="py-4 bg-gradient-to-r from-orange-500 via-red-500 to-orange-500 text-white sticky top-16 z-40 shadow-lg">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-center gap-3 text-center">
            <span className="text-xl animate-bounce">🔥</span>
            <p className="font-bold text-lg">Limited Stock Available - Order Now!</p>
            <span className="text-xl animate-bounce">🔥</span>
          </div>
        </div>
      </section> */}

      {/* PRODUCTS SECTION */}
      <section className="py-20 md:py-28">
        <div className="max-w-7xl mx-auto px-4">
          {/* Section Header */}
          <div className="mb-16 animate-slide-in-up">
            <div className="flex items-center gap-3 mb-3">
              <div className="p-3 bg-gradient-to-br from-orange-400 to-orange-600 rounded-lg">
                <span className="text-white text-2xl">🍴</span>
              </div>
              <span className="text-sm font-bold text-orange-600 uppercase tracking-widest drop-shadow">Explore Now</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-slate-900 mb-4">
              Our Handpicked Favorites
            </h2>
            <div className="w-20 h-1 bg-gradient-to-r from-orange-500 to-red-500 rounded-full"></div>
          </div>

          {/* Loading State */}
          {loading && (
            <div className="flex flex-col items-center justify-center py-32">
              <div className="relative w-20 h-20 mb-6">
                <div className="absolute inset-0 bg-gradient-to-r from-orange-400 to-red-400 rounded-full animate-spin opacity-75"></div>
                <div className="absolute inset-2 bg-white rounded-full flex items-center justify-center text-2xl">🍿</div>
              </div>
              <p className="mt-4 text-slate-600 text-lg font-semibold">Loading delicious snacks...</p>
            </div>
          )}

          {/* Products Grid */}
          {!loading && displayProducts.length > 0 && (
            <div>
              <div className="mb-8 flex items-center justify-between flex-wrap gap-4">
                <div>
                  <p className="text-slate-600 font-semibold text-lg">
                    <span className="text-3xl font-black text-orange-600">{displayProducts.length}</span>
                    <span className="ml-2 text-slate-700">{displayProducts.length === 1 ? 'Product' : 'Products'} Available</span>
                  </p>
                  <p className="text-sm text-slate-500 mt-1">All fresh, all premium, all irresistible 😋</p>
                </div>
                <div className="px-6 py-3 bg-gradient-to-r from-emerald-50 to-emerald-100 border border-emerald-200 rounded-full">
                  <p className="font-bold text-emerald-700 text-sm">✓ In Stock Ready to Ship</p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                {displayProducts.map((product, idx) => (
                  <div 
                    key={product.id}
                    className="group relative animate-bounce-in will-change-transform"
                    style={{ animationDelay: `${idx * 0.05}s` }}
                  >
                    {/* Hot Badge */}
                    {idx < 3 && (
                      <div className="absolute -top-3 -right-3 bg-gradient-to-r from-orange-500 via-red-500 to-orange-500 text-white px-3 py-1 rounded-full text-xs font-bold z-10 shadow-lg animate-pulse">
                        🔥 HOT
                      </div>
                    )}
                    
                    {/* Popular Badge */}
                    {idx % 3 === 0 && idx > 2 && (
                      <div className="absolute -top-3 -right-3 bg-gradient-to-r from-yellow-400 to-orange-400 text-slate-900 px-3 py-1 rounded-full text-xs font-bold z-10 shadow-lg animate-pulse">
                        ⭐ POPULAR
                      </div>
                    )}

                    <ProductCard product={product} />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Empty State */}
          {!loading && displayProducts.length === 0 && (
            <div className="text-center py-32">
              <div className="text-7xl mb-6">🥗</div>
              <h3 className="text-3xl font-black text-slate-900 mb-3">Coming Soon!</h3>
              <p className="text-slate-600 text-lg mb-8">Our amazing snacks are being prepared. Stay tuned!</p>
              <div className="inline-flex items-center gap-2 bg-orange-100 text-orange-700 px-6 py-3 rounded-full font-semibold">
                <span className="animate-pulse">●</span>
                We're restocking soon
              </div>
            </div>
          )}
        </div>
      </section>

      {/* CTA SECTION */}
      <section className="py-16 md:py-24 bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 text-white rounded-3xl mx-4 lg:mx-auto lg:max-w-6xl relative overflow-hidden mb-16">
        {/* Animated Background */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-0 right-0 w-96 h-96 bg-orange-500/10 rounded-full blur-3xl animate-float"></div>
          <div className="absolute bottom-0 left-0 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        </div>

        <div className="px-8 md:px-12 relative z-10 text-center space-y-8">
          <div>
            <h2 className="text-4xl md:text-5xl font-black mb-4 bg-gradient-to-r from-orange-300 via-red-300 to-emerald-300 bg-clip-text text-transparent">
              Why Snacky Lovers Choose Us 💝
            </h2>
            <p className="text-lg text-slate-300">Join 50K+ happy customers enjoying premium snacks</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4">
            {[
              { emoji: '🚀', title: 'Super Fast', desc: 'Same day delivery in most areas' },
              { emoji: '💎', title: 'Premium Quality', desc: 'Handpicked snacks you\'ll love' },
              { emoji: '💰', title: 'Best Prices', desc: 'Guaranteed lowest on the market' },
            ].map((item, idx) => (
              <div 
                key={idx}
                className="p-6 rounded-2xl bg-white/10 backdrop-blur border border-white/20 hover:border-white/40 hover:bg-white/20 transition-all duration-300 transform hover:scale-105 animate-bounce-in"
                style={{ animationDelay: `${idx * 0.1}s` }}
              >
                <div className="text-4xl mb-3">{item.emoji}</div>
                <h3 className="font-bold text-lg mb-2">{item.title}</h3>
                <p className="text-slate-300 text-sm">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default ProductsPage;
