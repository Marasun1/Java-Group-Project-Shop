package com.store.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptTest {

    @Test
    void gettersAndSettersWorkCorrectly() {
        LocalDateTime receivedAt = LocalDateTime.of(2026, 5, 4, 15, 10);

        Receipt receipt = new Receipt();
        receipt.setId(2L);
        receipt.setProductId(5L);
        receipt.setUserId(7L);
        receipt.setQtyReceived(30L);
        receipt.setWholesalePrice(new BigDecimal("1500.00"));
        receipt.setReceivedAt(receivedAt);
        receipt.setNote("Планове поповнення складу");

        assertEquals(2L, receipt.getId());
        assertEquals(5L, receipt.getProductId());
        assertEquals(7L, receipt.getUserId());
        assertEquals(30L, receipt.getQtyReceived());
        assertEquals(new BigDecimal("1500.00"), receipt.getWholesalePrice());
        assertEquals(receivedAt, receipt.getReceivedAt());
        assertEquals("Планове поповнення складу", receipt.getNote());
    }
}
