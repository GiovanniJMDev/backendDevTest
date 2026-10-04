package com.giovanni.similarproducts.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;

import com.giovanni.similarproducts.exception.ProductNotFoundException;

class ExternalApiErrorUtilTest {

    @Test
    void isNotFound_onlyForHttp404() {
        assertThat(ExternalApiErrorUtil.isNotFound(HttpStatus.NOT_FOUND)).isTrue();
        assertThat(ExternalApiErrorUtil.isNotFound(HttpStatus.INTERNAL_SERVER_ERROR)).isFalse();
    }

    @Test
    void throwNotFound_throwsProductNotFoundWithUri() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("http://localhost:3001/product/9"));

        assertThatThrownBy(() -> ExternalApiErrorUtil.throwNotFound(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("/product/9");
    }
}
