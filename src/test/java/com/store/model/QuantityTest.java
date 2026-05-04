package com.store.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuantityTest {

    @Test
    void gettersAndSettersWorkCorrectly() {
        LocalDateTime lastUpdated = LocalDateTime.of(2026, 5, 4, 11, 20);

        Quantity quantity = new Quantity();
        quantity.setId(4L);
        quantity.setProductId(11L);
        quantity.setLocation("Kyiv-A1");
        quantity.setQty(25L);
        quantity.setLastUpdated(lastUpdated);

        assertEquals(4L, quantity.getId());
        assertEquals(11L, quantity.getProductId());
        assertEquals("Kyiv-A1", quantity.getLocation());
        assertEquals(25L, quantity.getQty());
        assertEquals(lastUpdated, quantity.getLastUpdated());
    }
}
