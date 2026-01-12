import React from 'react';
import { Card, Avatar, Badge } from '../common';
import { FiStar } from 'react-icons/fi';

/**
 * ReviewSection / Testimonials Component
 * Displays customer reviews and testimonials in a carousel-like grid
 */
export const ReviewSection = ({
  title = 'What Our Customers Love ❤️',
  subtitle = 'Real reviews from real snack lovers',
  reviews = [],
  itemsPerRow = { xs: 1, md: 2, lg: 3 },
}) => {
  // Default reviews if none provided
  const defaultReviews = [
    {
      id: 1,
      author: 'Priya Sharma',
      rating: 5,
      title: 'Absolutely Delicious!',
      content:
        'The quality of snacks is outstanding! They arrived fresh and the packaging was excellent. Will definitely order again.',
      avatar: 'PS',
      date: '2 weeks ago',
      verified: true,
    },
    {
      id: 2,
      author: 'Arjun Patel',
      rating: 5,
      title: 'Best Online Snack Store',
      content:
        'Amazing variety and fast delivery. The customer service is responsive and helpful. Highly recommended!',
      avatar: 'AP',
      date: '1 month ago',
      verified: true,
    },
    {
      id: 3,
      author: 'Sneha Gupta',
      rating: 4,
      title: 'Great Quality, Competitive Prices',
      content:
        'Really impressed with the product quality and pricing. Had a small issue with one order but they resolved it immediately.',
      avatar: 'SG',
      date: '1 month ago',
      verified: true,
    },
    {
      id: 4,
      author: 'Rahul Singh',
      rating: 5,
      title: 'Perfect for Parties!',
      content:
        'Ordered a variety pack for my birthday party. Guests loved the snacks and the quantities were perfect.',
      avatar: 'RS',
      date: '2 months ago',
      verified: true,
    },
    {
      id: 5,
      author: 'Meera Nair',
      rating: 5,
      title: 'Consistent Quality',
      content:
        'Been a regular customer for 3 months now. The quality is always consistent and delivery is always on time.',
      avatar: 'MN',
      date: '3 months ago',
      verified: true,
    },
    {
      id: 6,
      author: 'Aditya Kumar',
      rating: 4,
      title: 'Good Variety, Quick Delivery',
      content:
        'Love the variety of snacks available. Quick delivery to my doorstep. Packaging could be a bit more eco-friendly though.',
      avatar: 'AK',
      date: '3 months ago',
      verified: true,
    },
  ];

  const reviewsToShow = reviews.length > 0 ? reviews : defaultReviews;

  return (
    <section className="py-16 md:py-24 bg-gradient-page-bg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center mb-16 animate-fade-in-up">
          <h2 className="text-4xl md:text-5xl font-black font-display text-gray-900 mb-4">
            {title}
          </h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">{subtitle}</p>
          <div className="mt-4 flex items-center justify-center gap-1">
            {Array(5)
              .fill(null)
              .map((_, i) => (
                <FiStar
                  key={i}
                  className="w-6 h-6 fill-yellow-400 text-yellow-400"
                />
              ))}
            <span className="ml-2 text-lg font-bold text-gray-900">4.9/5 Average Rating</span>
          </div>
        </div>

        {/* Reviews Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {reviewsToShow.map((review, index) => (
            <Card
              key={review.id}
              className="flex flex-col hover:shadow-lg hover:scale-105 transition-all duration-300 hover:border-brand-500"
              padding="p-6"
              style={{
                animation: `fade-in-up 0.6s ease-out ${index * 0.1}s backwards`,
              }}
            >
              {/* Rating Stars */}
              <div className="flex items-center justify-between mb-3">
                <div className="flex gap-1">
                  {Array(5)
                    .fill(null)
                    .map((_, i) => (
                      <FiStar
                        key={i}
                        className={`w-4 h-4 ${
                          i < review.rating
                            ? 'fill-yellow-400 text-yellow-400'
                            : 'text-gray-300'
                        }`}
                      />
                    ))}
                </div>
                {review.verified && (
                  <Badge variant="success" size="sm">
                    ✓ Verified
                  </Badge>
                )}
              </div>

              {/* Review Title */}
              <h3 className="text-lg font-bold text-gray-900 mb-2">{review.title}</h3>

              {/* Review Content */}
              <p className="text-gray-600 text-sm leading-relaxed mb-4 flex-1">
                {review.content}
              </p>

              {/* Divider */}
              <div className="border-t border-gray-200 pt-4 mt-4" />

              {/* Author Info */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Avatar size="sm" name={review.author} />
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">{review.author}</p>
                    <p className="text-xs text-gray-500">{review.date}</p>
                  </div>
                </div>
              </div>
            </Card>
          ))}
        </div>

        {/* Call to Action */}
        <div className="mt-16 text-center">
          <div className="inline-block bg-gradient-to-r from-brand-600 to-accent-600 text-white px-8 py-4 rounded-lg hover:shadow-brand hover:shadow-lg transition-all duration-300 transform hover:scale-105">
            <p className="font-bold text-lg mb-1">Join Thousands of Happy Customers!</p>
            <p className="text-sm opacity-90">Shop now and share your experience</p>
          </div>
        </div>

        {/* Additional Stats */}
        <div className="mt-12 grid grid-cols-3 gap-4 md:gap-8 text-center">
          <div className="group">
            <p className="text-3xl md:text-4xl font-black text-brand-600 group-hover:scale-110 transition-transform">
              10K+
            </p>
            <p className="text-gray-600 text-sm font-semibold mt-1">Positive Reviews</p>
          </div>
          <div className="group">
            <p className="text-3xl md:text-4xl font-black text-yellow-500 group-hover:scale-110 transition-transform">
              4.9★
            </p>
            <p className="text-gray-600 text-sm font-semibold mt-1">Average Rating</p>
          </div>
          <div className="group">
            <p className="text-3xl md:text-4xl font-black text-cta-600 group-hover:scale-110 transition-transform">
              98%
            </p>
            <p className="text-gray-600 text-sm font-semibold mt-1">Would Recommend</p>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ReviewSection;
