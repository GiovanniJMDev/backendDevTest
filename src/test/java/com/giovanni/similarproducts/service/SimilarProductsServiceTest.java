package com.giovanni.similarproducts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.giovanni.similarproducts.converter.ProductConverter;
import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.dto.ProductDetailResponseDto;
import com.giovanni.similarproducts.exception.ProductNotFoundException;
import com.giovanni.similarproducts.service.impl.SimilarProductsServiceImpl;

class SimilarProductsServiceTest {

    private ProductCacheService cache;
    private SimilarProductsService service;

    @BeforeEach
    void setUp() {
        cache = mock(ProductCacheService.class);
        service = new SimilarProductsServiceImpl(cache, new ProductConverter());
    }

    @Test
    void returnsProductsKeepingSimilarityOrder() {
        when(cache.getSimilarIds("1")).thenReturn(List.of(2, 3, 4));
        when(cache.getProduct("2")).thenReturn(product("2", "Dress"));
        when(cache.getProduct("3")).thenReturn(product("3", "Blazer"));
        when(cache.getProduct("4")).thenReturn(product("4", "Boots"));

        List<ProductDetailResponseDto> result = service.getSimilarProducts("1");

        assertThat(result).extracting(ProductDetailResponseDto::id).containsExactly("2", "3", "4");
    }

    @Test
    void skipsProductsThatFail() {
        when(cache.getSimilarIds("1")).thenReturn(List.of(2, 5, 6, 7));
        when(cache.getProduct("2")).thenReturn(product("2", "Dress"));
        when(cache.getProduct("5")).thenThrow(new IllegalStateException("500"));
        when(cache.getProduct("6")).thenThrow(new ProductNotFoundException("404"));
        when(cache.getProduct("7")).thenReturn(product("7", "Coat"));

        assertThat(service.getSimilarProducts("1"))
                .extracting(ProductDetailResponseDto::id).containsExactly("2", "7");
    }

    @Test
    void emptyWhenNoSimilarIds() {
        when(cache.getSimilarIds("1")).thenReturn(null);
        when(cache.getSimilarIds("2")).thenReturn(List.of());

        assertThat(service.getSimilarProducts("1")).isEmpty();
        assertThat(service.getSimilarProducts("2")).isEmpty();
    }

    @Test
    void unknownProductPropagatesNotFound() {
        when(cache.getSimilarIds("9")).thenThrow(new ProductNotFoundException("nope"));

        assertThatThrownBy(() -> service.getSimilarProducts("9")).isInstanceOf(ProductNotFoundException.class);
    }

    private static ExternalDataDto product(String id, String name) {
        ExternalDataDto dto = new ExternalDataDto();
        dto.put("id", id);
        dto.put("name", name);
        dto.put("price", 9.99);
        dto.put("availability", true);
        return dto;
    }
}
