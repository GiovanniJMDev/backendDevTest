package com.giovanni.similarproducts.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.giovanni.similarproducts.converter.ProductConverter;
import com.giovanni.similarproducts.dto.ProductDetailResponseDto;
import com.giovanni.similarproducts.service.ProductCacheService;
import com.giovanni.similarproducts.service.SimilarProductsService;

@Service
public class SimilarProductsServiceImpl implements SimilarProductsService {

    private final ProductCacheService productCacheService;
    private final ProductConverter productConverter;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public SimilarProductsServiceImpl(ProductCacheService productCacheService,
            ProductConverter productConverter) {
        this.productCacheService = productCacheService;
        this.productConverter = productConverter;
    }

    @Override
    public List<ProductDetailResponseDto> getSimilarProducts(String productId) {
        List<Object> ids = productCacheService.getSimilarIds(productId);
        if (ids == null) {
            return List.of();
        }
        return joinResults(fetchAllAsync(ids));
    }

    private List<CompletableFuture<ProductDetailResponseDto>> fetchAllAsync(List<Object> ids) {
        return ids.stream()
                .map(String::valueOf)
                .map(this::fetchAsync)
                .toList();
    }

    private CompletableFuture<ProductDetailResponseDto> fetchAsync(String id) {
        return CompletableFuture.supplyAsync(() -> fetchProduct(id), executor);
    }

    private List<ProductDetailResponseDto> joinResults(List<CompletableFuture<ProductDetailResponseDto>> futures) {
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }

    private ProductDetailResponseDto fetchProduct(String id) {
        try {
            return productConverter.toDto(productCacheService.getProduct(id));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
