package com.store.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuantityTest {

    @Test
    void gettersAndSettersWorkCorrectly() {
        LocalDate expiresAt = LocalDate.of(2026, 6, 15);
        LocalDateTime lastUpdated = LocalDateTime.of(2026, 5, 4, 11, 20);

        Quantity quantity = new Quantity();
        quantity.setId(4L);
        quantity.setProductId(11L);
        quantity.setLocation("MAIN_STORAGE");
        quantity.setQty(new BigDecimal("25.500"));
        quantity.setExpiresAt(expiresAt);
        quantity.setLastUpdated(lastUpdated);

        assertEquals(4L, quantity.getId());
        assertEquals(11L, quantity.getProductId());
        assertEquals("MAIN_STORAGE", quantity.getLocation());
        assertEquals(new BigDecimal("25.500"), quantity.getQty());
        assertEquals(expiresAt, quantity.getExpiresAt());
        assertEquals(lastUpdated, quantity.getLastUpdated());
    }
}
