/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Header, Footer } from '@/components/layout';
import { ProductCard } from '@/components/features';
import { Button } from '@/components/common';
import { fetchAllProducts, fetchProductImages } from '@/store/thunks/productThunks';
import { ToastContainer } from '@/components/common/Toast';
import { Flame, TrendingUp, Gift, Zap, Star, ArrowRight, ChevronRight, Sparkles, Truck, Shield, Award } from 'lucide-react';

export const HomePage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items: products, loading, productImages } = useSelector(state => state.products);
  const [displayCount, setDisplayCount] = useState(8);
  const [scrollY, setScrollY] = useState(0);

  // Fetch all products
  useEffect(() => {
    dispatch(fetchAllProducts());
  }, [dispatch]);

  // Handle scroll for parallax effect
  useEffect(() => {
    const handleScroll = () => setScrollY(window.scrollY);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Fetch images for all products after they load
  useEffect(() => {
    if (products && products.length > 0) {
      products.forEach(product => {
        // Only fetch if image not already in Redux state
        if (!productImages[product.id]) {
          dispatch(fetchProductImages(product.id))
            .catch(err => {
              console.error(`Failed to fetch images for product ${product.id}`, err);
            });
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
        @keyframes slide-in-up {
          from { opacity: 0; transform: translateY(30px); }
          to { opacity: 1; transform: translateY(0); }
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
        @keyframes gradient-flow {
          0%, 100% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
        }
        @keyframes flip-in {
          from { opacity: 0; transform: rotateY(-90deg); }
          to { opacity: 1; transform: rotateY(0deg); }
        }
        .animate-float { animation: float 3s cubic-bezier(0.4, 0.0, 0.2, 1) infinite; }
        .animate-shimmer { animation: shimmer 2s ease-in-out infinite; }
        .animate-glow-pulse { animation: glow-pulse 2s cubic-bezier(0.4, 0.0, 0.6, 1) infinite; }
        .animate-slide-in-left { animation: slide-in-left 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-slide-in-right { animation: slide-in-right 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-slide-in-up { animation: slide-in-up 0.6s cubic-bezier(0.4, 0.0, 0.2, 1) forwards; }
        .animate-bounce-in { animation: bounce-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; }
        .animate-rotate-slow { animation: rotate-slow 20s linear infinite; }
        .animate-gradient-flow { animation: gradient-flow 3s ease infinite; background-size: 200% 200%; }
        .animate-flip-in { animation: flip-in 0.7s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; }
      `}</style>
      <ToastContainer />
      <Header />

      {/* PREMIUM HERO SECTION */}
      <section className="relative overflow-hidden pt-12 md:pt-20 pb-20 md:pb-32">
        {/* Animated Background Gradient with Parallax */}
        <div className="absolute inset-0 -z-10">
          <div 
            className="absolute top-0 left-1/4 w-64 md:w-96 h-64 md:h-96 bg-orange-400/40 rounded-full blur-3xl animate-pulse"
            style={{ transform: `translateY(${scrollY * 0.5}px)` }}
          ></div>
          <div 
            className="absolute bottom-0 right-1/4 w-64 md:w-96 h-64 md:h-96 bg-emerald-400/40 rounded-full blur-3xl animate-pulse"
            style={{ transform: `translateY(${-scrollY * 0.3}px)` }}
          ></div>
          <div className="absolute top-1/2 right-0 w-64 md:w-80 h-64 md:h-80 bg-blue-300/20 rounded-full blur-3xl animate-float"></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 md:gap-12 items-center">
            {/* Left: Hero Image Showcase - FIRST ON MOBILE */}
            <div className="relative h-64 md:h-96 lg:h-[500px] animate-slide-in-right will-change-transform order-first lg:order-last" style={{ animationDelay: '0.2s' }}>
              <div className="absolute -inset-6 bg-gradient-to-br from-orange-300 via-red-300 to-emerald-300 rounded-3xl opacity-30 blur-2xl animate-pulse"></div>
              <div className="absolute inset-0 bg-gradient-to-br from-orange-400/20 to-emerald-400/20 rounded-3xl"></div>
              <div className="relative w-full h-full rounded-3xl overflow-hidden shadow-2xl border-4 border-white/50 group">
                <img
                  src="/hero.png"
                  alt="Premium Snacks"
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-out group-hover:rotate-1"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
              </div>
            </div>

            {/* Right: Hero Text - SECOND ON MOBILE */}
            <div className="space-y-6 md:space-y-8 order-last lg:order-first">
              {/* Badge */}
              <div className="inline-flex items-center gap-2 bg-gradient-to-r from-orange-100 to-red-100 text-orange-700 px-3 md:px-4 py-2 rounded-full w-fit border border-orange-200 animate-slide-in-left text-sm md:text-base">
                <Sparkles className="w-4 h-4 animate-rotate-slow flex-shrink-0" />
                <span className="font-semibold">Welcome to Snacky Paradise 🎉</span>
              </div>

              {/* Main Headline */}
              <h1 className="text-4xl md:text-5xl lg:text-7xl font-black bg-gradient-to-r from-orange-600 via-red-500 to-emerald-600 bg-clip-text text-transparent leading-tight animate-slide-in-left" style={{ animationDelay: '0.1s' }}>
                Satisfy Your Cravings Instantly
              </h1>

              {/* Subheadline */}
              <p className="text-base md:text-lg lg:text-xl text-slate-600 leading-relaxed max-w-lg animate-slide-in-left" style={{ animationDelay: '0.2s' }}>
                Discover mouthwatering snacks curated just for you. Fresh, delicious, and delivered to your doorstep within hours.
              </p>

              {/* CTA Buttons */}
              <div className="flex flex-col sm:flex-row gap-3 md:gap-4 animate-slide-in-left" style={{ animationDelay: '0.3s' }}>
                <button
                  onClick={() => navigate('/products')}
                  className="group relative px-6 md:px-8 py-3 md:py-4 bg-gradient-to-r from-orange-500 via-orange-600 to-red-600 text-white font-bold rounded-xl overflow-hidden shadow-lg hover:shadow-2xl transform hover:scale-105 transition-all duration-300 ease-out animate-glow-pulse"
                >
                  <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-20 -skew-x-12 animate-shimmer"></div>
                  <span className="relative flex items-center gap-2 justify-center">
                    Explore Now <ArrowRight className="w-5 h-5 group-hover:translate-x-2 transition-transform duration-300 ease-out" />
                  </span>
                </button>
                <button
                  onClick={() => navigate('/products?sort=rating')}
                  className="px-6 md:px-8 py-3 md:py-4 border-2 border-orange-400 bg-white/80 backdrop-blur text-slate-700 font-bold rounded-xl hover:border-orange-500 hover:from-orange-500 hover:to-red-500 hover:bg-gradient-to-r hover:text-white transition-all duration-300 ease-out group"
                >
                  <span className="flex items-center gap-2 justify-center">
                    <Star className="w-5 h-5 group-hover:animate-spin" /> Top Rated
                  </span>
                </button>
              </div>

              {/* Trust Badges */}
              <div className="flex flex-col sm:flex-row gap-4 md:gap-6 pt-2 md:pt-4 animate-slide-in-left" style={{ animationDelay: '0.4s' }}>
                <div className="flex items-center gap-3 text-sm p-3 md:p-4 rounded-lg bg-emerald-50 border border-emerald-200 hover:shadow-lg transition-shadow">
                  <div className="w-8 md:w-10 h-8 md:h-10 rounded-full bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center text-white font-bold flex-shrink-0">✓</div>
                  <span className="text-slate-700 font-medium">Fresh & Premium</span>
                </div>
                <div className="flex items-center gap-3 text-sm p-3 md:p-4 rounded-lg bg-blue-50 border border-blue-200 hover:shadow-lg transition-shadow">
                  <div className="w-8 md:w-10 h-8 md:h-10 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white font-bold flex-shrink-0">⚡</div>
                  <span className="text-slate-700 font-medium">Best Prices</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FLASH DEALS BANNER */}
      {/* <section className="py-8 bg-gradient-to-r from-red-500 to-orange-500 text-white">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Flame className="w-8 h-8 animate-bounce" />
              <h3 className="text-2xl font-bold">🔥 FLASH DEALS - Limited Time!</h3>
            </div>
            <div className="text-lg font-bold animate-pulse">⏰ Only 24 Hours!</div>
          </div>
        </div>
      </section> */}


      {/* TRENDING NOW SECTION - ENHANCED */}
      <section className="py-12 md:py-16 lg:py-24 border-t-2 border-gradient-to-r from-orange-200 via-red-200 to-emerald-200 relative overflow-hidden">
        {/* Decorative Background Elements */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-10 right-20 w-72 h-72 bg-orange-400/15 rounded-full blur-3xl animate-float"></div>
          <div className="absolute bottom-10 left-20 w-72 h-72 bg-emerald-400/15 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          <div className="mb-12 md:mb-16 animate-slide-in-up">
            <div className="flex items-center gap-3 mb-3 md:mb-4">
              <div className="p-2 md:p-3 bg-gradient-to-br from-orange-500 to-red-600 rounded-lg shadow-lg">
                <TrendingUp className="w-5 md:w-6 h-5 md:h-6 text-white" />
              </div>
              <span className="text-xs md:text-sm font-bold text-orange-600 uppercase tracking-widest drop-shadow">✨ Featured Products</span>
            </div>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-black text-slate-900 mb-3 md:mb-4 bg-gradient-to-r from-orange-600 to-emerald-600 bg-clip-text text-transparent inline-block">
              Our Complete Snack Collection 🍿
            </h2>
            <p className="text-base md:text-lg text-slate-600 max-w-2xl">
              Hand-picked premium snacks with premium images and authentic reviews. Something for every craving!
            </p>
          </div>

          {/* All Products Grid with Enhanced Styling */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-12">
            {displayProducts.length > 0 ? (
              displayProducts.map((product, idx) => (
                <div 
                  key={product.id} 
                  className="group relative animate-bounce-in will-change-transform hover:-translate-y-3 transition-transform duration-300"
                  style={{ animationDelay: `${idx * 0.05}s` }}
                >
                  {idx < 4 && (
                    <div className="absolute -top-4 -right-4 bg-gradient-to-r from-orange-500 via-red-500 to-orange-600 text-white px-3 py-1.5 rounded-full text-xs font-bold z-20 shadow-lg animate-pulse transform hover:scale-110 transition-transform">
                      🔥 TRENDING
                    </div>
                  )}
                  <ProductCard product={product} />
                </div>
              ))
            ) : (
              <div className="col-span-full text-center py-16">
                <div className="inline-block">
                  <div className="w-16 h-16 bg-gradient-to-br from-orange-400 to-emerald-400 rounded-full opacity-20 animate-pulse mb-4"></div>
                  <p className="text-slate-600 text-lg font-semibold">Loading delicious products...</p>
                </div>
              </div>
            )}
          </div>

          {/* View All Button */}
          <div className="text-center mt-8">
            <button
              onClick={() => navigate('/products')}
              className="inline-flex items-center gap-2 px-8 py-4 bg-gradient-to-r from-orange-500 via-red-500 to-emerald-500 text-white font-bold rounded-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300 group relative overflow-hidden"
            >
              <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-20 -skew-x-12 animate-shimmer"></div>
              <span className="relative">View All Products</span>
              <ChevronRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </button>
          </div>
        </div>
      </section>

      {/* BESTSELLERS SECTION WITH FEATURE */}
      {/* <section className="py-16 md:py-24 bg-gradient-to-r from-slate-50 to-orange-50 rounded-3xl mx-4 lg:mx-auto lg:max-w-7xl">
        <div className="px-8 md:px-12"> */}
          {/* <div className="mb-12">
            <div className="flex items-center gap-3 mb-2">
              <Star className="w-6 h-6 text-yellow-500" />
              <span className="text-sm font-bold text-yellow-600 uppercase tracking-widest">Bestsellers</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-slate-900 mb-4">
              Customer Favorites ⭐
            </h2>
            <p className="text-lg text-slate-600">
              These snacks are flying off the shelves - don't miss out!
            </p>
          </div> */}

          {/* Best Sellers Grid */}
          {/* <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {bestSellingProducts.map(product => (
              <div key={product.id} className="group relative">
                <div className="absolute -top-3 -right-3 bg-gradient-to-r from-yellow-400 to-orange-400 text-slate-900 px-3 py-1 rounded-full text-xs font-bold z-10 flex items-center gap-1">
                  <Zap className="w-3 h-3" /> BESTSELLER
                </div>
                <ProductCard product={product} />
              </div>
            ))}
          </div>
        </div>
      </section> */}

      {/* LIMITED TIME OFFERS - COMMENTED OUT */}
      {/* <section className="py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {limitedTimeDeals.map(product => (
              <div key={product.id} className="group relative">
                <div className="absolute -top-3 -right-3 bg-gradient-to-r from-emerald-500 to-teal-500 text-white px-3 py-1 rounded-full text-xs font-bold z-10">
                  LIMITED
                </div>
                <ProductCard product={product} />
              </div>
            ))}
          </div>
        </div>
      </section> */}



      {/* WHY CHOOSE US - ENHANCED */}
      <section className="py-12 md:py-16 lg:py-24 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white rounded-2xl md:rounded-3xl mx-4 md:mx-6 lg:mx-auto lg:max-w-7xl relative overflow-hidden">
        {/* Animated Background Elements */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-0 right-0 w-64 md:w-96 h-64 md:h-96 bg-orange-500/10 rounded-full blur-3xl animate-float"></div>
          <div className="absolute bottom-0 left-0 w-64 md:w-96 h-64 md:h-96 bg-emerald-500/10 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        </div>

        <div className="px-4 md:px-6 lg:px-8 xl:px-12 relative z-10">
          <h2 className="text-3xl md:text-4xl lg:text-5xl font-black mb-12 md:mb-16 text-center bg-gradient-to-r from-orange-300 via-red-300 to-emerald-300 bg-clip-text text-transparent">
            Why 50K+ Choose Snacky 🌟
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
            {[
              { icon: Shield, title: '100% Authentic', desc: 'Picked by us, crafted with care, love, and passion', color: 'from-emerald-400 to-emerald-600' },
              { icon: Award, title: 'Best Prices', desc: 'Guaranteed lowest prices with exclusive deals', color: 'from-orange-400 to-orange-600' },
              { icon: Star, title: 'Premium Quality', desc: 'Money-back guarantee on every purchase', color: 'from-yellow-400 to-yellow-600' },
            ].map((item, idx) => {
              const Icon = item.icon;
              return (
                <div 
                  key={idx} 
                  className="group text-center p-6 md:p-8 rounded-2xl bg-white/10 backdrop-blur border border-white/20 hover:border-white/40 transition-all duration-300 hover:bg-white/20 hover:shadow-2xl transform hover:scale-105 animate-bounce-in"
                  style={{ animationDelay: `${idx * 0.1}s` }}
                >
                  <div className={`text-4xl md:text-5xl mb-4 p-3 md:p-4 rounded-xl bg-gradient-to-br ${item.color} w-fit mx-auto`}>
                    <Icon className="w-7 md:w-8 h-7 md:h-8 text-white" />
                  </div>
                  <h3 className="text-lg md:text-xl font-bold mb-2">{item.title}</h3>
                  <p className="text-sm md:text-base text-slate-300 leading-relaxed">{item.desc}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* CUSTOMER REVIEWS CAROUSEL - ENHANCED */}
      <section className="py-12 md:py-16 lg:py-24 relative overflow-hidden">
        <style>{`
          @keyframes scroll-left {
            0% { transform: translateX(0); }
            100% { transform: translateX(-100%); }
          }
          .animate-scroll { animation: scroll-left 50s linear infinite; }
          .review-container:hover .animate-scroll { animation-play-state: paused; }
        `}</style>

        <div className="absolute inset-0 -z-10">
          <div className="absolute top-0 left-0 w-96 h-96 bg-orange-400/15 rounded-full blur-3xl animate-float"></div>
          <div className="absolute bottom-0 right-0 w-96 h-96 bg-emerald-400/15 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          {/* Section Header */}
          <div className="mb-12 md:mb-16 text-center animate-slide-in-up">
            <div className="flex items-center justify-center gap-3 mb-4">
              <div className="p-2 md:p-3 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-lg shadow-lg">
                <Star className="w-5 md:w-6 h-5 md:h-6 text-white" />
              </div>
              <span className="text-xs md:text-sm font-bold text-orange-600 uppercase tracking-widest drop-shadow">⭐ Customer Love</span>
            </div>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-black text-slate-900 mb-3 md:mb-4 bg-gradient-to-r from-orange-600 to-emerald-600 bg-clip-text text-transparent inline-block">
              What Our Customers Say 💬
            </h2>
            <p className="text-base md:text-lg text-slate-600 max-w-2xl mx-auto">
              Join thousands of happy customers who love our premium snacks. Read their authentic reviews!
            </p>
          </div>

          {/* Reviews Carousel */}
          <div className="review-container overflow-hidden">
            <div className="animate-scroll flex gap-4 md:gap-6 pb-4">
              {[
                { name: "Amit S.", rating: 5, review: "Authentic homemade taste. Fresh, crunchy, and perfectly sweet." },
                { name: "Sneha R.", rating: 5, review: "Feels hand-made with love. Goes great with chai!" },
                { name: "Rahul K.", rating: 5, review: "Premium quality, not oily, and very well packed." },
                { name: "Priya M.", rating: 5, review: "Traditional thekua done right. Will reorder for sure." },
                { name: "Vikram P.", rating: 5, review: "Best snacks I've had in years. Definitely worth the money!" },
                { name: "Anjali N.", rating: 5, review: "Fresh as if just made. The packaging is amazing too!" },
                { name: "Deepak M.", rating: 5, review: "Tastes like my grandmother made it. Absolutely divine!" },
                { name: "Pooja S.", rating: 5, review: "Fast delivery and excellent quality. Highly recommended!" },
                // Duplicate for infinite scroll effect
                { name: "Amit S.", rating: 5, review: "Authentic homemade taste. Fresh, crunchy, and perfectly sweet." },
                { name: "Sneha R.", rating: 5, review: "Feels hand-made with love. Goes great with chai!" },
                { name: "Rahul K.", rating: 5, review: "Premium quality, not oily, and very well packed." },
                { name: "Priya M.", rating: 5, review: "Traditional thekua done right. Will reorder for sure." },
              ].map((review, idx) => (
                <div 
                  key={idx}
                  className="flex-shrink-0 w-80 md:w-96 group animate-bounce-in"
                  style={{ animationDelay: `${idx * 0.05}s` }}
                >
                  <div className="h-full bg-gradient-to-br from-white to-orange-50 border-2 border-gradient-to-r from-orange-200 to-emerald-200 rounded-2xl p-6 md:p-7 shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 hover:border-orange-400 flex flex-col">
                    {/* Rating Stars */}
                    <div className="flex gap-1 mb-3">
                      {Array(review.rating).fill(null).map((_, i) => (
                        <Star key={i} className="w-5 h-5 fill-yellow-400 text-yellow-400 drop-shadow-md" />
                      ))}
                    </div>

                    {/* Review Text */}
                    <p className="text-slate-700 font-medium text-sm md:text-base mb-4 flex-1 leading-relaxed">
                      "{review.review}"
                    </p>

                    {/* Reviewer Info */}
                    <div className="flex items-center gap-3 pt-4 border-t-2 border-orange-100">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-br from-orange-500 to-red-600 flex items-center justify-center text-white font-bold text-sm">
                        {review.name.charAt(0)}
                      </div>
                      <div>
                        <p className="font-bold text-slate-900 text-sm">{review.name}</p>
                        <p className="text-xs text-orange-600 font-semibold">Verified Customer ✓</p>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Scroll Indicator */}
          <div className="flex justify-center mt-8 gap-2">
            <div className="text-center">
              <p className="text-sm text-slate-600 font-semibold mb-3">👉 Swipe/Scroll to see more reviews</p>
              <div className="flex justify-center gap-2">
                <div className="w-2 h-2 rounded-full bg-orange-500 animate-pulse"></div>
                <div className="w-2 h-2 rounded-full bg-red-500 animate-pulse" style={{ animationDelay: '0.2s' }}></div>
                <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" style={{ animationDelay: '0.4s' }}></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CALL TO ACTION - ENHANCED */}
      <section className="py-12 md:py-16 lg:py-24 text-center relative overflow-hidden">
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 md:w-96 h-80 md:h-96 bg-gradient-to-r from-orange-400 to-red-400 rounded-full blur-3xl opacity-20 animate-pulse"></div>
        </div>

        <div className="max-w-3xl mx-auto px-4 md:px-6 lg:px-8 animate-slide-in-up">
          <h2 className="text-3xl md:text-4xl lg:text-6xl font-black mb-4 md:mb-6">
            <span className="bg-gradient-to-r from-orange-600 via-red-600 to-pink-600 bg-clip-text text-transparent inline-block hover:scale-110 transition-transform duration-300">
              Ready to Snack?
            </span>
          </h2>
          <p className="text-base md:text-lg lg:text-xl text-slate-600 mb-6 md:mb-8 leading-relaxed">
            Your favorite snacks are just a click away. Order now and get <span className="font-bold text-emerald-600">free delivery on orders over ₹500</span>!
          </p>
          <button
            onClick={() => navigate('/products')}
            className="inline-block px-6 md:px-10 py-3 md:py-5 bg-gradient-to-r from-orange-500 via-red-500 to-pink-500 text-white font-bold text-base md:text-lg rounded-2xl hover:shadow-2xl transform hover:scale-110 transition-all duration-300 group relative overflow-hidden"
          >
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-20 -skew-x-12 animate-shimmer"></div>
            <span className="relative flex items-center gap-2 justify-center">
              Shop Now <ChevronRight className="w-5 h-5 group-hover:translate-x-2 transition-transform" />
            </span>
          </button>
        </div>
      </section>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default HomePage;
