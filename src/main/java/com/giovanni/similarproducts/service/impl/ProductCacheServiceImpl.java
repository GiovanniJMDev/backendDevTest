package com.giovanni.similarproducts.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.service.ExternalProductService;
import com.giovanni.similarproducts.service.ProductCacheService;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class ProductCacheServiceImpl implements ProductCacheService {

    private static final long MAX_ENTRIES = 10_000;

    private final ExternalProductService externalProductService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AsyncCache<String, List<Object>> similarIdsCache;
    private final AsyncCache<String, ExternalDataDto> productCache;

    public ProductCacheServiceImpl(ExternalProductService externalProductService,
            @Value("${products.cache.ttl}") Duration ttl) {
        this.externalProductService = externalProductService;
        this.similarIdsCache = newCache(ttl);
        this.productCache = newCache(ttl);
    }

    @Override
    public List<Object> getSimilarIds(String productId) {
        return await(similarIdsCache.get(productId, externalProductService::getSimilarIds));
    }

    @Override
    public ExternalDataDto getProduct(String productId) {
        return await(productCache.get(productId, externalProductService::getProduct));
    }

    @Override
    public void clear() {
        similarIdsCache.synchronous().invalidateAll();
        productCache.synchronous().invalidateAll();
    }

    private <V> AsyncCache<String, V> newCache(Duration ttl) {
        return Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(ttl)
                .maximumSize(MAX_ENTRIES)
                .buildAsync();
    }

    private static <V> V await(CompletableFuture<V> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
