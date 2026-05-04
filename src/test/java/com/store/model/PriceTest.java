package com.store.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceTest {

    @Test
    void gettersAndSettersWorkCorrectly() {
        LocalDateTime validFrom = LocalDateTime.of(2026, 5, 4, 9, 0);
        LocalDateTime validTo = LocalDateTime.of(2026, 6, 1, 0, 0);

        Price price = new Price();
        price.setId(6L);
        price.setProductId(8L);
        price.setAmount(new BigDecimal("199.99"));
        price.setRetailPrice(new BigDecimal("249.99"));
        price.setValidFrom(validFrom);
        price.setValidTo(validTo);

        assertEquals(6L, price.getId());
        assertEquals(8L, price.getProductId());
        assertEquals(new BigDecimal("199.99"), price.getAmount());
        assertEquals(new BigDecimal("249.99"), price.getRetailPrice());
        assertEquals(validFrom, price.getValidFrom());
        assertEquals(validTo, price.getValidTo());
    }
}
