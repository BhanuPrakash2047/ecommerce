package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.ProductNotFoundException;
import com.snackecommerce.product.dto.FAQRequest;
import com.snackecommerce.product.dto.FAQResponse;
import com.snackecommerce.product.entity.FAQ;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.repository.FAQRepository;
import com.snackecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FAQService {

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @CacheEvict(value = "productFAQs", key = "#productId")
    public FAQResponse createFAQ(Long productId, FAQRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        FAQ faq = FAQ.builder()
                .product(product)
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        faq = faqRepository.save(faq);
        return mapToResponse(faq);
    }

    @Cacheable(value = "productFAQs", key = "#productId")
    public List<FAQResponse> getProductFAQs(Long productId) {
        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        return faqRepository.findByProductIdOrderByDisplayOrder(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FAQResponse updateFAQ(Long faqId, FAQRequest request) {
        FAQ faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new RuntimeException("FAQ not found with ID: " + faqId));

        Long productId = faq.getProduct().getId();

        if (request.getQuestion() != null) faq.setQuestion(request.getQuestion());
        if (request.getAnswer() != null) faq.setAnswer(request.getAnswer());
        if (request.getDisplayOrder() != null) faq.setDisplayOrder(request.getDisplayOrder());

        faq.setUpdatedAt(LocalDateTime.now());
        faq = faqRepository.save(faq);
        
        // Evict cache for this product's FAQs
        evictProductFAQsCache(productId);
        
        return mapToResponse(faq);
    }

    public void deleteFAQ(Long faqId) {
        FAQ faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new RuntimeException("FAQ not found with ID: " + faqId));
        Long productId = faq.getProduct().getId();
        faqRepository.delete(faq);
        
        // Evict cache for this product's FAQs
        evictProductFAQsCache(productId);
    }

    private void evictProductFAQsCache(Long productId) {
        if (cacheManager.getCache("productFAQs") != null) {
            cacheManager.getCache("productFAQs").evict(productId);
        }
    }

    private FAQResponse mapToResponse(FAQ faq) {
        return FAQResponse.builder()
                .id(faq.getId())
                .productId(faq.getProduct().getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .displayOrder(faq.getDisplayOrder())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
