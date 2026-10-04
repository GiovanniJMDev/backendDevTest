package com.giovanni.similarproducts.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;

import com.giovanni.similarproducts.dto.ProductDetailResponseDto;
import com.giovanni.similarproducts.exception.GlobalExceptionHandler;
import com.giovanni.similarproducts.exception.ProductNotFoundException;
import com.giovanni.similarproducts.service.SimilarProductsService;

class SimilarProductsControllerTest {

    private SimilarProductsService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SimilarProductsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SimilarProductsController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returns200WithProducts() throws Exception {
        when(service.getSimilarProducts("1"))
                .thenReturn(List.of(new ProductDetailResponseDto("2", "Dress", 19.99, true)));

        mockMvc.perform(get("/product/1/similar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Dress"))
                .andExpect(jsonPath("$[0].price").value(19.99))
                .andExpect(jsonPath("$[0].availability").value(true));
    }

    @Test
    void returns404WhenProductDoesNotExist() throws Exception {
        when(service.getSimilarProducts("9")).thenThrow(new ProductNotFoundException("nope"));

        mockMvc.perform(get("/product/9/similar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returns502WhenExternalApiIsDown() throws Exception {
        when(service.getSimilarProducts("1")).thenThrow(new ResourceAccessException("timeout"));

        mockMvc.perform(get("/product/1/similar"))
                .andExpect(status().isBadGateway());
    }
}
