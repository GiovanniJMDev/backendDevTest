package com.giovanni.similarproducts.service.impl;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.service.ExternalProductService;
import com.giovanni.similarproducts.util.ExternalApiErrorUtil;

@Service
public class ExternalProductServiceImpl implements ExternalProductService {

    private final RestClient restClient;

    public ExternalProductServiceImpl(RestClient productsRestClient) {
        this.restClient = productsRestClient;
    }

    @Override
    public List<Object> getSimilarIds(String productId) {
        return get("/product/{id}/similarids", productId, new ParameterizedTypeReference<>() { });
    }

    @Override
    public ExternalDataDto getProduct(String productId) {
        return get("/product/{id}", productId, ParameterizedTypeReference.forType(ExternalDataDto.class));
    }

    private <T> T get(String uri, String productId, ParameterizedTypeReference<T> type) {
        return restClient.get()
                .uri(uri, productId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(ExternalApiErrorUtil::isNotFound, ExternalApiErrorUtil::throwNotFound)
                .body(type);
    }
}
