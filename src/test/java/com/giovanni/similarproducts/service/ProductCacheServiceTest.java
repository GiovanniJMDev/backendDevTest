package com.giovanni.similarproducts.service;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.exception.ProductNotFoundException;
import com.giovanni.similarproducts.service.impl.ProductCacheServiceImpl;

class ProductCacheServiceTest {

    private ExternalProductService external;
    private ProductCacheService cache;

    @BeforeEach
    void setUp() {
        external = mock(ExternalProductService.class);
        cache = new ProductCacheServiceImpl(external, Duration.ofMinutes(5));
    }

    @Test
    void getSimilarIds_secondCallIsServedFromCache() {
        when(external.getSimilarIds("1")).thenReturn(List.of(2, 3));

        assertThat(cache.getSimilarIds("1")).containsExactly(2, 3);
        assertThat(cache.getSimilarIds("1")).containsExactly(2, 3);

        verify(external, times(1)).getSimilarIds("1");
    }

    @Test
    void getProduct_secondCallIsServedFromCache() {
        when(external.getProduct("1")).thenReturn(new ExternalDataDto());

        cache.getProduct("1");
        cache.getProduct("1");

        verify(external, times(1)).getProduct("1");
    }

    @Test
    void failures_areNotCached() {
        when(external.getProduct("1")).thenThrow(new IllegalStateException("boom")).thenReturn(new ExternalDataDto());

        assertThatThrownBy(() -> cache.getProduct("1")).isInstanceOf(IllegalStateException.class);
        // the cache evicts a failed entry asynchronously, so the retry may need a moment
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(cache.getProduct("1")).isNotNull());

        verify(external, times(2)).getProduct("1");
    }

    @Test
    void originalExceptionIsUnwrapped() {
        when(external.getSimilarIds("9")).thenThrow(new ProductNotFoundException("nope"));

        assertThatThrownBy(() -> cache.getSimilarIds("9")).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void clear_forcesANewExternalCall() {
        when(external.getProduct("1")).thenReturn(new ExternalDataDto());

        cache.getProduct("1");
        cache.clear();
        cache.getProduct("1");

        verify(external, times(2)).getProduct("1");
    }

    @Test
    void zeroTtl_disablesCaching() {
        ProductCacheService noCache = new ProductCacheServiceImpl(external, Duration.ZERO);
        when(external.getProduct("1")).thenReturn(new ExternalDataDto());

        noCache.getProduct("1");
        noCache.getProduct("1");

        verify(external, times(2)).getProduct("1");
    }

    @Test
    void entriesExpireAfterTtl() {
        ProductCacheService shortLived = new ProductCacheServiceImpl(external, Duration.ofMillis(50));
        when(external.getProduct("1")).thenReturn(new ExternalDataDto());

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            shortLived.getProduct("1");
            verify(external, times(2)).getProduct("1");
        });
    }
}
