package com.giovanni.similarproducts.util;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.giovanni.similarproducts.exception.ProductNotFoundException;

public final class ExternalApiErrorUtil {

    private ExternalApiErrorUtil() {
    }

    public static boolean isNotFound(HttpStatusCode status) {
        return status.isSameCodeAs(HttpStatus.NOT_FOUND);
    }

    public static void throwNotFound(HttpRequest request) {
        throw new ProductNotFoundException("Product not found in external API: " + request.getURI());
    }
}
