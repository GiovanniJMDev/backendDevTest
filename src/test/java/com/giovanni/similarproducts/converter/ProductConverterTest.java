package com.giovanni.similarproducts.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.giovanni.similarproducts.dto.ExternalDataDto;
import com.giovanni.similarproducts.dto.ProductDetailResponseDto;

class ProductConverterTest {

    private final ProductConverter converter = new ProductConverter();

    @Test
    void toDto_mapsKnownFieldsAndIgnoresUnknownOnes() {
        ExternalDataDto external = new ExternalDataDto();
        external.put("id", 1);
        external.put("name", "Shirt");
        external.put("price", 9.99);
        external.put("availability", true);
        external.put("somethingNew", "ignored");

        ProductDetailResponseDto dto = converter.toDto(external);

        assertThat(dto).isEqualTo(new ProductDetailResponseDto("1", "Shirt", 9.99, true));
    }

    @Test
    void toDto_returnsNullWhenInputIsNull() {
        assertThat(converter.toDto(null)).isNull();
    }
}
