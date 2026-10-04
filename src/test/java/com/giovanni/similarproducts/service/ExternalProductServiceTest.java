package com.giovanni.similarproducts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.exception.ProductNotFoundException;
import com.giovanni.similarproducts.service.impl.ExternalProductServiceImpl;

class ExternalProductServiceTest {

    private MockRestServiceServer server;
    private ExternalProductService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://external");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new ExternalProductServiceImpl(builder.build());
    }

    @Test
    void getSimilarIds_returnsGenericList() {
        server.expect(requestTo("http://external/product/1/similarids"))
                .andRespond(withSuccess("[2,3,4]", MediaType.APPLICATION_JSON));

        assertThat(service.getSimilarIds("1")).containsExactly(2, 3, 4);
    }

    @Test
    void getProduct_acceptsAnyFields() {
        server.expect(requestTo("http://external/product/1"))
                .andRespond(withSuccess("{\"id\":\"1\",\"extra\":{\"a\":1}}", MediaType.APPLICATION_JSON));

        ExternalDataDto product = service.getProduct("1");

        assertThat(product).containsEntry("id", "1").containsKey("extra");
    }

    @Test
    void notFound_throwsProductNotFoundException() {
        server.expect(requestTo("http://external/product/9")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> service.getProduct("9")).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void serverError_throwsRestClientException() {
        server.expect(requestTo("http://external/product/1/similarids")).andRespond(withServerError());

        assertThatThrownBy(() -> service.getSimilarIds("1")).isInstanceOf(RestClientException.class);
    }
}
