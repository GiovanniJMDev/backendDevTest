package com.giovanni.similarproducts.converter;

import org.springframework.stereotype.Component;

import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.dto.ProductDetailResponseDto;

@Component
public class ProductConverter {

    public ProductDetailResponseDto toDto(ExternalDataDto external) {
        if (external == null) {
            return null;
        }
        return new ProductDetailResponseDto(
                String.valueOf(external.get("id")),
                String.valueOf(external.get("name")),
                external.get("price") instanceof Number price ? price.doubleValue() : 0,
                Boolean.parseBoolean(String.valueOf(external.get("availability"))));
    }
}
