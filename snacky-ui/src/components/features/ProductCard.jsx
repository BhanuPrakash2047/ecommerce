import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Card, Badge, Button, Skeleton } from '../common';
import { FiShoppingCart, FiHeart, FiStar } from 'react-icons/fi';
import { FaHeart } from 'react-icons/fa';
import { showToast } from '../common/Toast';
import { addToCart } from '@/store/thunks/cartThunks';

/**
 * ProductCard Component - Premium Edition
 * Displays individual product with stunning animations and beautiful styling
 */
export const ProductCard = ({
  product,
  categoryGradient = 'from-brand-500 to-accent-600',
  loading = false,
}) => {
  const dispatch = useDispatch();
  const [isFavorite, setIsFavorite] = useState(false);
  const [isAddingToCart, setIsAddingToCart] = useState(false);
  const [isHovered, setIsHovered] = useState(false);

  if (loading) {
    return (
      <Card padding="p-0 overflow-hidden" className="h-80">
        <Skeleton height="h-40" width="w-full" />
        <div className="p-4 space-y-2">
          <Skeleton height="h-4" width="w-3/4" />
          <Skeleton height="h-3" width="w-full" />
          <Skeleton height="h-3" width="w-1/2" />
          <Skeleton height="h-8" width="w-full" />
        </div>
      </Card>
    );
  }

  const handleAddToCart = async () => {
    setIsAddingToCart(true);
    try {
      await dispatch(addToCart({ productId: product.id, quantity: 1 })).unwrap();
      showToast(`${product.name} added to cart!`, 'success');
    } catch (error) {
      showToast(error || 'Failed to add to cart', 'error');
    } finally {
      setIsAddingToCart(false);
    }
  };

  const discountPercent = product.originalPrice
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : 0;

  return (
    <Card
      className="group relative overflow-hidden h-full flex flex-col transition-all duration-500 ease-out hover:shadow-2xl hover:border-brand-400 transform hover:-translate-y-2 will-change-transform"
      hoverable
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Gradient Glow Background - Premium Effect */}
      <div className={`absolute -inset-1 bg-gradient-to-r from-brand-500 via-accent-500 to-brand-500 rounded-lg opacity-0 group-hover:opacity-20 blur-lg transition-opacity duration-500 -z-10`} />

      {/* Image Container */}
      <div className="relative h-40 md:h-52 overflow-hidden bg-gradient-to-br from-gray-100 to-gray-200">
        {/* Animated Background Pattern */}
        <div className="absolute inset-0 opacity-0 group-hover:opacity-5 transition-opacity duration-500">
          <div className="absolute w-40 h-40 bg-brand-500 rounded-full -top-20 -right-20 blur-3xl" />
          <div className="absolute w-40 h-40 bg-accent-500 rounded-full -bottom-20 -left-20 blur-3xl" />
        </div>

        {/* Product Image with Zoom Effect */}
        <img
          src={product.image || 'https://via.placeholder.com/300x200?text=Product'}
          alt={product.name}
          className={`w-full h-full object-cover transition-transform duration-[600ms] ease-out will-change-transform ${
            isHovered ? 'scale-[1.15]' : 'scale-100'
          }`}
        />

        {/* Floating Gradient Overlay */}
        <div className={`absolute inset-0 bg-gradient-to-t ${categoryGradient} opacity-0 group-hover:opacity-10 transition-opacity duration-500 ease-out pointer-events-none`} />

        {/* Premium Badges with Animation */}
        <div className="absolute top-3 left-3 flex flex-col gap-2 z-10">
          {product.price < product.originalPrice && (
            <div className="bg-gradient-to-r from-red-500 to-orange-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-lg animate-bounce">
              {Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)}% Off
            </div>
          )}

          {!product.isAvailable && (
            <Badge variant="danger" size="sm" className="shadow-lg">
              Out of Stock
            </Badge>
          )}
        </div>

        {/* Premium Favorite Button with Animation */}
        <button
          onClick={() => setIsFavorite(!isFavorite)}
          className={`absolute top-3 right-3 w-10 h-10 rounded-full shadow-lg flex items-center justify-center hover:scale-125 transition-all duration-300 backdrop-blur-md ${
            isFavorite
              ? 'bg-gradient-to-r from-red-400 to-pink-500'
              : 'bg-white/90 hover:bg-white'
          }`}
          aria-label="Add to favorites"
        >
          {isFavorite ? (
            <FaHeart className="w-5 h-5 text-white animate-pulse" />
          ) : (
            <FiHeart className="w-5 h-5 text-gray-700" />
          )}
        </button>

        {/* Premium Quick View Overlay */}
        <Link
          to={`/products/${product.id}`}
          className={`absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent/20 flex flex-col items-center justify-center gap-3 transition-all duration-300 ease-out md:opacity-0 md:group-hover:opacity-100 opacity-100 z-20`}
        >
          <div className="transform transition-transform duration-400 ease-out md:translate-y-10" style={{
            transform: isHovered ? 'translateY(0)' : 'translateY(10px)',
          }}>
            <Button 
              variant="cta" 
              size="sm"
              className="!bg-white !text-gray-900 font-bold hover:!bg-gradient-to-r hover:!from-brand-500 hover:!to-accent-600 hover:!text-white shadow-xl"
            >
              Quick View
            </Button>
          </div>
          <p className="text-white text-xs font-semibold opacity-75 transition-opacity hidden md:block">Click to explore</p>
        </Link>
      </div>

      {/* Content Section - Premium Styling */}
      <div className="flex-1 flex flex-col p-4 gap-3 bg-gradient-to-b from-white to-gray-50">
        {/* Category Label with Animation */}
        <div className="flex items-center gap-2">
          <div className="w-1 h-4 bg-gradient-to-b from-brand-500 to-accent-500 rounded-full" />
          <p className="text-xs font-bold text-brand-600 uppercase tracking-widest opacity-80 group-hover:opacity-100 transition-opacity">
            {product.category || 'Snack'}
          </p>
        </div>

        {/* Product Name with Gradient Hover */}
        <h3 className="text-sm md:text-base font-bold transition-all duration-300 line-clamp-2 text-gray-900 group-hover:text-brand-700">
          {product.name}
        </h3>

        {/* Description with Fade */}
        {product.description && (
          <p className="text-xs text-gray-600 line-clamp-1 opacity-75 group-hover:opacity-100 transition-opacity">
            {product.description}
          </p>
        )}

        {/* Premium Rating Display */}
        {product.rating && (
          <div className="flex items-center gap-2 py-1">
            <div className="flex gap-0.5">
              {Array(5)
                .fill(null)
                .map((_, i) => (
                  <div key={i} className="relative">
                    <FiStar
                      className={`w-3.5 h-3.5 transition-all ${
                        i < Math.round(product.rating)
                          ? 'fill-yellow-400 text-yellow-400'
                          : 'text-gray-300'
                      }`}
                    />
                  </div>
                ))}
            </div>
            <span className="text-xs text-gray-600 font-semibold">
              {product.rating.toFixed(1)} ({product.reviewCount || 0})
            </span>
          </div>
        )}

        {/* Premium Price Display */}
        <div className="flex items-baseline gap-3 py-2 bg-gradient-to-r from-brand-50 to-accent-50 px-3 rounded-lg">
          <span className="text-lg md:text-2xl font-black transition-all duration-300 text-brand-600 group-hover:text-brand-700">
            ₹{product.price?.toFixed(2) || '0.00'}
          </span>
          {product.originalPrice && product.originalPrice > product.price && (
            <span className="text-sm text-gray-500 line-through font-semibold opacity-70">
              ₹{product.originalPrice?.toFixed(2)}
            </span>
          )}
        </div>

        {/* Premium Add to Cart Button */}
        <Button
          variant="cta"
          size="sm"
          fullWidth
          loading={isAddingToCart}
          disabled={!product.isAvailable}
          onClick={handleAddToCart}
          icon={FiShoppingCart}
          className={`mt-auto transition-all duration-300 transform ${
            isHovered ? 'scale-105 shadow-xl' : 'scale-100'
          } ${
            product.isAvailable
              ? 'bg-gradient-to-r from-orange-500 via-red-500 to-pink-500 hover:shadow-2xl'
              : 'bg-gray-400 cursor-not-allowed'
          }`}
        >
          {product.isAvailable ? 'Add to Cart' : 'Unavailable'}
        </Button>
      </div>
    </Card>
  );
};

export default ProductCard;
