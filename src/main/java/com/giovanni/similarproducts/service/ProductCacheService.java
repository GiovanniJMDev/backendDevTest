package com.giovanni.similarproducts.service;

import java.util.List;

import com.giovanni.similarproducts.dto.ExternalDataDto;

public interface ProductCacheService {

    List<Object> getSimilarIds(String productId);

    ExternalDataDto getProduct(String productId);

    void clear();
}
