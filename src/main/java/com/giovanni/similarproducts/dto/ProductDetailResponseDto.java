package com.giovanni.similarproducts.dto;

public record ProductDetailResponseDto(
        String id,
        String name,
        double price,
        boolean availability) {
}
