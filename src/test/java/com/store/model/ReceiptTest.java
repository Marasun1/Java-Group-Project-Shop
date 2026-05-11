package com.store.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptTest {

    @Test
    void gettersAndSettersWorkCorrectly() {
        LocalDate expiresAt = LocalDate.of(2026, 7, 1);
        LocalDateTime receivedAt = LocalDateTime.of(2026, 5, 4, 15, 10);

        Receipt receipt = new Receipt();
        receipt.setId(2L);
        receipt.setProductId(5L);
        receipt.setUserId(7L);
        receipt.setRoleId(3L);
        receipt.setSupplier("ТОВ Постачальник");
        receipt.setInvoiceNumber("INV-101");
        receipt.setQtyReceived(new BigDecimal("30.500"));
        receipt.setCostPrice(new BigDecimal("1500.00"));
        receipt.setExpiresAt(expiresAt);
        receipt.setReceivedAt(receivedAt);
        receipt.setNote("Планове поповнення складу");

        assertEquals(2L, receipt.getId());
        assertEquals(5L, receipt.getProductId());
        assertEquals(7L, receipt.getUserId());
        assertEquals(3L, receipt.getRoleId());
        assertEquals("ТОВ Постачальник", receipt.getSupplier());
        assertEquals("INV-101", receipt.getInvoiceNumber());
        assertEquals(new BigDecimal("30.500"), receipt.getQtyReceived());
        assertEquals(new BigDecimal("1500.00"), receipt.getCostPrice());
        assertEquals(expiresAt, receipt.getExpiresAt());
        assertEquals(receivedAt, receipt.getReceivedAt());
        assertEquals("Планове поповнення складу", receipt.getNote());
    }
}
