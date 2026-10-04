package com.giovanni.similarproducts.service;

import java.util.List;

import com.giovanni.similarproducts.dto.ProductDetailResponseDto;

public interface SimilarProductsService {

    List<ProductDetailResponseDto> getSimilarProducts(String productId);
}
