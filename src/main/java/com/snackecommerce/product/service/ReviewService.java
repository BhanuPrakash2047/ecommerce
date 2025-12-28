package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.ProductNotFoundException;
import com.snackecommerce.common.exception.ReviewNotFoundException;
import com.snackecommerce.product.dto.ReviewRequest;
import com.snackecommerce.product.dto.ReviewResponse;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.entity.Review;
import com.snackecommerce.product.repository.ProductRepository;
import com.snackecommerce.product.repository.ReviewRepository;
import com.snackecommerce.user.entity.User;
import com.snackecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public ReviewResponse createReview(Long productId, ReviewRequest request, String userEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already reviewed this product
        if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .text(request.getText())
                .verified(false)  // Can be set to true by admin if user purchased
                .build();

        review = reviewRepository.save(review);
        return mapToResponse(review);
    }

    public Page<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        return reviewRepository.findByProductId(productId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user owns the review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setText(request.getText());
        review.setUpdatedAt(LocalDateTime.now());

        review = reviewRepository.save(review);
        return mapToResponse(review);
    }

    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user owns the review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userEmail(review.getUser().getEmail())
                .rating(review.getRating())
                .title(review.getTitle())
                .text(review.getText())
                .verified(review.getVerified())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
