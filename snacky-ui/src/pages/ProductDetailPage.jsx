import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Star, Heart, Share2, ShoppingCart, ChevronLeft, Plus, Minus } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Input, Card, Badge, Spinner, Modal } from '@/components/common';
import { fetchProductById, fetchProductImages, fetchProductReviews, fetchFAQs, fetchProductVideos, addReview } from '@/store/thunks/productThunks';
import { addToCart } from '@/store/thunks/cartThunks';
import { showToast } from '@/utils/toast';

const ProductDetailPage = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { selectedProduct, reviews, faqs, videos, error, productImages } = useSelector(state => state.products);
  const { user } = useSelector(state => state.auth);

  // Alias for compatibility
  const currentProduct = selectedProduct;

  const [quantity, setQuantity] = useState(1);
  const [selectedImage, setSelectedImage] = useState(0);
  const [isFavorite, setIsFavorite] = useState(false);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [reviewForm, setReviewForm] = useState({ rating: 5, title: '', text: '' });
  const [submittingReview, setSubmittingReview] = useState(false);
  const [expandedFAQ, setExpandedFAQ] = useState(null);
  const [loadingState, setLoadingState] = useState({
    product: true,
    images: false,
    reviews: false,
    faqs: false,
    videos: false
  });

  // Fetch product details and all related data in order
  useEffect(() => {
    if (productId) {
      // Step 1: Fetch product details first
      setLoadingState(prev => ({ ...prev, product: true }));
      
      dispatch(fetchProductById(productId))
        .unwrap()
        .then(() => {
          setLoadingState(prev => ({ ...prev, product: false }));
          
          // Step 2: After product loaded, fetch images
          setLoadingState(prev => ({ ...prev, images: true }));
          return dispatch(fetchProductImages(productId)).unwrap();
        })
        .then(() => {
          setLoadingState(prev => ({ ...prev, images: false }));
          
          // Step 3: After images loaded, fetch reviews
          setLoadingState(prev => ({ ...prev, reviews: true }));
          return dispatch(fetchProductReviews(productId)).unwrap();
        })
        .then(() => {
          setLoadingState(prev => ({ ...prev, reviews: false }));
          
          // Step 4: After reviews loaded, fetch FAQs
          setLoadingState(prev => ({ ...prev, faqs: true }));
          return dispatch(fetchFAQs(productId)).unwrap();
        })
        .then(() => {
          setLoadingState(prev => ({ ...prev, faqs: false }));
          
          // Step 5: Finally, fetch videos
          setLoadingState(prev => ({ ...prev, videos: true }));
          return dispatch(fetchProductVideos(productId)).unwrap();
        })
        .then(() => {
          setLoadingState(prev => ({ ...prev, videos: false }));
          showToast('Product details loaded successfully!', 'success');
        })
        .catch(err => {
          setLoadingState(prev => ({ 
            ...prev, 
            images: false, 
            reviews: false, 
            faqs: false, 
            videos: false 
          }));
          showToast(err || 'Failed to load product details', 'error');
        });
    }
  }, [productId, dispatch]);

  const handleAddToCart = () => {
    if (!user) {
      showToast('Please login to add items to cart', 'error');
      navigate('/login');
      return;
    }

    dispatch(addToCart({
      productId: currentProduct.id,
      quantity,
    }))
      .unwrap()
      .then(() => {
        showToast(`${quantity} ${currentProduct.name} added to cart!`, 'success');
        setQuantity(1);
      })
      .catch(err => {
        showToast(err || 'Failed to add to cart', 'error');
      });
  };

  const handleSubmitReview = async () => {
    if (!reviewForm.title.trim()) {
      showToast('Please write a review title', 'error');
      return;
    }

    if (!reviewForm.text.trim()) {
      showToast('Please write a review comment', 'error');
      return;
    }

    if (!user) {
      showToast('Please login to submit a review', 'error');
      navigate('/login');
      return;
    }

    setSubmittingReview(true);
    try {
      await dispatch(addReview({
        productId: currentProduct.id,
        rating: reviewForm.rating,
        title: reviewForm.title,
        text: reviewForm.text
      })).unwrap();

      showToast('Review submitted successfully!', 'success');
      setReviewForm({ rating: 5, title: '', text: '' });
      setShowReviewModal(false);
      
      // Refresh reviews after adding
      dispatch(fetchProductReviews(productId));
    } catch (err) {
      showToast(err || 'Failed to submit review', 'error');
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loadingState.product) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col">
        <Header />
        <div className="flex-1 flex flex-col items-center justify-center gap-4">
          <Spinner size="lg" />
          <p className="text-slate-600 font-medium">Loading product details...</p>
        </div>
        <Footer />
      </div>
    );
  }

  if (error || !currentProduct) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col">
        <Header />
        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="text-center">
            <p className="text-2xl font-bold text-slate-900 mb-4">Product not found</p>
            <Button onClick={() => navigate('/products')}>
              Back to Products
            </Button>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  const images = (productImages[productId] && productImages[productId].length > 0) 
    ? productImages[productId]
    : (currentProduct && currentProduct.image ? [currentProduct.image] : ['/placeholder.jpg']);
  const avgRating = reviews && reviews.length > 0
    ? (reviews.reduce((sum, r) => sum + (r.rating || 0), 0) / reviews.length).toFixed(1)
    : 0;

  return (
    <div className="min-h-screen bg-white">
      <Header />

      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <button
          onClick={() => navigate('/products')}
          className="flex items-center gap-2 text-orange-600 hover:text-orange-700 mb-8 transition"
        >
          <ChevronLeft className="w-4 h-4" />
          Back to Products
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-12">
          {/* Images */}
          <div>
            {loadingState.images ? (
              <div className="mb-4 bg-slate-100 rounded-xl overflow-hidden h-96 lg:h-[500px] flex items-center justify-center">
                <div className="flex flex-col items-center gap-2">
                  <Spinner size="md" />
                  <p className="text-slate-600 text-sm">Loading images...</p>
                </div>
              </div>
            ) : (
              <>
                {/* Main Image */}
                <div className="mb-4 bg-slate-100 rounded-xl overflow-hidden h-96 lg:h-[500px] flex items-center justify-center">
                  <img
                    src={images[selectedImage] || '/placeholder.jpg'}
                    alt={currentProduct.name}
                    className="w-full h-full object-contain"
                  />
                </div>

                {/* Thumbnails */}
                {images.length > 1 && (
                  <div className="flex gap-4 overflow-x-auto">
                    {images.map((img, idx) => (
                      <button
                        key={idx}
                        onClick={() => setSelectedImage(idx)}
                        className={`w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 border-2 transition ${
                          selectedImage === idx ? 'border-orange-500' : 'border-slate-200 hover:border-slate-300'
                        }`}
                      >
                        <img
                          src={img || '/placeholder.jpg'}
                          alt={`${currentProduct.name} ${idx + 1}`}
                          className="w-full h-full object-cover"
                        />
                      </button>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>

          {/* Product Info */}
          <div>
            <div className="flex items-start justify-between mb-4">
              <div>
                <h1 className="text-4xl font-bold text-slate-900 mb-2">
                  {currentProduct.name}
                </h1>
                <Badge label={currentProduct.category} variant="orange" />
              </div>
              <button
                onClick={() => setIsFavorite(!isFavorite)}
                className="p-2 hover:bg-slate-100 rounded-full transition"
              >

              </button>
            </div>

            {/* Rating */}
            <div className="flex items-center gap-4 mb-6">
              <div className="flex items-center gap-1">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={`w-5 h-5 ${
                      i < Math.round(avgRating)
                        ? 'fill-yellow-400 text-yellow-400'
                        : 'text-slate-300'
                    }`}
                  />
                ))}
              </div>
              <span className="text-lg font-semibold text-slate-900">{avgRating}/5</span>
              <span className="text-slate-600">({reviews?.length || 0} reviews)</span>
            </div>

            {/* Price */}
            <div className="mb-6">
              <div className="flex items-baseline gap-4 mb-2">
                <span className="text-4xl font-bold text-orange-600">
                  ₹{currentProduct.price}
                </span>
                {currentProduct.originalPrice && (
                  <>
                    <span className="text-xl text-slate-400 line-through">
                      ₹{currentProduct.originalPrice}
                    </span>
                    <Badge
                      label={`${Math.round(((currentProduct.originalPrice - currentProduct.price) / currentProduct.originalPrice) * 100)}% OFF`}
                      variant="red"
                    />
                  </>
                )}
              </div>
              {currentProduct.isAvailable? (
                <p className="text-green-600 font-medium">In Stock</p>
              ) : (
                <p className="text-red-600 font-medium">Out of Stock</p>
              )}
            </div>

            {/* Description */}
            <p className="text-slate-600 mb-8 leading-relaxed">
              {currentProduct.description}
            </p>

            {/* Quantity Selector */}
            {currentProduct.stock > 0 && (
              <div className="flex items-center gap-4 mb-8">
                <span className="font-semibold text-slate-900">Quantity:</span>
                <div className="flex items-center gap-3 bg-slate-100 rounded-lg w-fit p-2">
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="p-1 hover:bg-slate-200 rounded transition"
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <span className="w-8 text-center font-semibold">{quantity}</span>
                  <button
                    onClick={() => setQuantity(Math.min(currentProduct.stock, quantity + 1))}
                    className="p-1 hover:bg-slate-200 rounded transition"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex gap-4">
              <Button
                onClick={handleAddToCart}
                variant="cta"
                className="bg-gradient-to-r from-orange-500 via-orange-600 to-red-600 flex-1 flex items-center justify-center gap-2 py-3"
                disabled={!currentProduct.isAvailable}
              >
                <ShoppingCart className="w-5 h-5" />
                Add to Cart
              </Button>
              <Button
                onClick={() => {/* Share functionality */}}
                variant="outline"
                className="flex items-center justify-center gap-2 py-3"
              >
                <Share2 className="w-5 h-5" />
              </Button>
            </div>

            {/* Product Details */}
            <div className="mt-12 pt-8 border-t border-slate-200 grid grid-cols-2 gap-6">
              {[
                { label: 'Shelf Life', value: currentProduct.shelfLife || '45 Days' },
                // { label: 'Package', value: currentProduct.packaging || 'N/A' },
                // { label: 'Manufacturer', value: currentProduct.manufacturer || '' },
                { label: 'Available Since', value: new Date(currentProduct.createdAt).toLocaleDateString() },
              ].map((item, idx) => (
                <div key={idx}>
                  <p className="text-sm text-slate-600 mb-1">{item.label}</p>
                  <p className="font-semibold text-slate-900">{item.value}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Tabs Section */}
        <div className="border-t border-slate-200 pt-12">
          {/* Reviews Section */}
          <div className="mb-16">
            <div className="flex items-center justify-between mb-8">
              <div>
                <h2 className="text-3xl font-bold text-slate-900">Customer Reviews</h2>
                <p className="text-slate-600 mt-2">
                  {reviews && reviews.length > 0 ? `${reviews.length} review${reviews.length !== 1 ? 's' : ''}` : 'No reviews yet'}
                </p>
              </div>
            </div>

            {loadingState.reviews ? (
              <div className="flex flex-col items-center justify-center py-12 gap-2">
                <Spinner size="md" />
                <p className="text-slate-600 text-sm">Loading reviews...</p>
              </div>
            ) : reviews && reviews.length > 0 ? (
              <div className="space-y-6">
                {reviews.map(review => (
                  <Card key={review.id} className="p-6 border border-slate-200 hover:shadow-lg transition">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <h4 className="font-bold text-slate-900">
                          {review.title || 'Review'}
                        </h4>
                        <div className="flex items-center gap-3 mt-2">
                          <span className="text-sm text-slate-600">{review.userEmail || 'Anonymous User'}</span>
                          {review.verified && (
                            <span className="text-xs bg-emerald-100 text-emerald-700 px-2 py-1 rounded">✓ Verified Purchase</span>
                          )}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="flex items-center gap-1 mb-1">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              className={`w-4 h-4 ${
                                i < review.rating
                                  ? 'fill-yellow-400 text-yellow-400'
                                  : 'text-slate-300'
                              }`}
                            />
                          ))}
                        </div>
                        <p className="text-sm text-slate-600 font-semibold">{review.rating}/5</p>
                      </div>
                    </div>
                    <p className="text-slate-700 leading-relaxed">{review.text}</p>
                    <p className="text-xs text-slate-500 mt-4">
                      {review.createdAt ? new Date(review.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                      }) : 'Recently added'}
                    </p>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="text-center py-12 bg-slate-50 rounded-lg border border-slate-200">
                <Star className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                <p className="text-slate-600 mb-4 text-lg font-medium">No reviews yet</p>
                <p className="text-slate-500 mb-6">Be the first to share your experience with this product</p>
                {user ? (
                  <Button onClick={() => setShowReviewModal(true)} variant="primary">
                    Write Your Review
                  </Button>
                ) : (
                  <Button onClick={() => navigate('/login')} variant="primary">
                    Login to Review
                  </Button>
                )}
              </div>
            )}

            {/* Add Review Button - Only if user hasn't reviewed yet */}
            {reviews && reviews.length > 0 && user && !reviews.some(r => r.userEmail === user.email) && (
              <div className="mt-8  flex justify-center">
                <Button 
                  onClick={() => setShowReviewModal(true)} 
                  variant="primary"
                  className="px-6 py-2 text-sm bg-gradient-to-r from-orange-500 via-orange-600 to-red-600 "
                >
                  Add Your Review
                </Button>
              </div>
            )}
          </div>

          {/* FAQs Section */}
          <div className="mb-16">
            <h2 className="text-3xl font-bold text-slate-900 mb-8">Frequently Asked Questions</h2>
            
            {loadingState.faqs ? (
              <div className="flex flex-col items-center justify-center py-12 gap-2">
                <Spinner size="md" />
                <p className="text-slate-600 text-sm">Loading FAQs...</p>
              </div>
            ) : faqs && faqs.length > 0 ? (
              <div className="space-y-4">
                {faqs.map((faq, idx) => (
                  <div
                    key={idx}
                    className="border border-slate-200 rounded-lg overflow-hidden"
                  >
                    <button
                      onClick={() => setExpandedFAQ(expandedFAQ === idx ? null : idx)}
                      className="w-full px-6 py-4 flex items-center justify-between bg-slate-50 hover:bg-slate-100 transition font-semibold text-slate-900"
                    >
                      {faq.question}
                      <span className={`transition ${expandedFAQ === idx ? 'rotate-180' : ''}`}>
                        ▼
                      </span>
                    </button>
                    {expandedFAQ === idx && (
                      <div className="px-6 py-4 bg-white border-t border-slate-200">
                        <p className="text-slate-700">{faq.answer}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12 bg-slate-50 rounded-lg">
                <p className="text-slate-600">No FAQs available for this product</p>
              </div>
            )}
          </div>

          {/* Videos Section */}
          <div className="mb-16">
            <h2 className="text-3xl font-bold text-slate-900 mb-8">Product Videos</h2>
            
            {loadingState.videos ? (
              <div className="flex flex-col items-center justify-center py-12 gap-2">
                <Spinner size="md" />
                <p className="text-slate-600 text-sm">Loading videos...</p>
              </div>
            ) : videos && videos.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {videos.map((video, idx) => (
                  <Card key={idx} className="overflow-hidden hover:shadow-lg transition">
                    <div className="bg-slate-900 h-48 flex items-center justify-center">
                      <video
                        src={video.videoUrl || video.url}
                        controls
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="p-4">
                      <h3 className="font-semibold text-slate-900 mb-2">{video.title}</h3>
                      {video.description && (
                        <p className="text-sm text-slate-600 line-clamp-2">{video.description}</p>
                      )}
                      {video.duration && (
                        <p className="text-xs text-slate-500 mt-2">Duration: {video.duration}s</p>
                      )}
                    </div>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="text-center py-12 bg-slate-50 rounded-lg">
                <p className="text-slate-600">No videos available for this product</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Review Modal */}
      <Modal
        isOpen={showReviewModal}
        onClose={() => setShowReviewModal(false)}
        title="Write a Review"
      >
        <div className="space-y-6">
          {/* Rating */}
          <div>
            <label className="block text-sm font-semibold text-slate-900 mb-3">
              Rating
            </label>
            <div className="flex gap-2">
              {[1, 2, 3, 4, 5].map(i => (
                <button
                  key={i}
                  onClick={() => setReviewForm({ ...reviewForm, rating: i })}
                  className="transition transform hover:scale-110 "
                >
                  <Star
                    className={`w-8 h-8 ${
                      i <= reviewForm.rating
                        ? 'fill-yellow-400 text-yellow-400'
                        : 'text-slate-300'
                    }`}
                  />
                </button>
              ))}
            </div>
          </div>

          {/* Comment */}
          <div>
            <label className="block text-sm font-semibold text-slate-900 mb-3">
              Review Title
            </label>
            <input
              type="text"
              value={reviewForm.title}
              onChange={(e) => setReviewForm({ ...reviewForm, title: e.target.value })}
              placeholder="e.g., Great taste and quality!"
              className="w-full px-4 py-3 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 text-base"
            />
          </div>

          {/* Text Review */}
          <div>
            <label className="block text-sm font-semibold text-slate-900 mb-3">
              Your Review
            </label>
            <textarea
              value={reviewForm.text}
              onChange={(e) => setReviewForm({ ...reviewForm, text: e.target.value })}
              placeholder="Share your experience with this product..."
              className="w-full px-4 py-3 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 resize-none h-32"
            />
          </div>

          {/* Buttons */}
          <div className="flex gap-3">
            <Button
              onClick={handleSubmitReview}
              variant="primary"
              className="flex-1 bg-gradient-to-r from-orange-500 via-orange-600 to-red-600 "
              disabled={submittingReview}
            >
              {submittingReview ? 'Submitting...' : 'Submit Review'}
            </Button>
            <Button
              onClick={() => setShowReviewModal(false)}
              variant="outline"
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </div>
      </Modal>

      <Footer />
    </div>
  );
};

export default ProductDetailPage;
