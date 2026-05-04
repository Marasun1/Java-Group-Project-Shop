package com.store.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    @Test
    void constructorWithCoreFieldsSetsValues() {
        Product product = new Product("SKU-101", "Ноутбук", "Опис");

        assertEquals("SKU-101", product.getSku());
        assertEquals("Ноутбук", product.getName());
        assertEquals("Опис", product.getDescription());
    }

    @Test
    void fullConstructorSetsAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 4, 10, 15);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 4, 12, 45);

        Product product = new Product(7L, "SKU-777", "Монітор", "27 дюймів", createdAt, updatedAt);

        assertEquals(7L, product.getId());
        assertEquals("SKU-777", product.getSku());
        assertEquals("Монітор", product.getName());
        assertEquals("27 дюймів", product.getDescription());
        assertEquals(createdAt, product.getCreatedAt());
        assertEquals(updatedAt, product.getUpdatedAt());
    }

    @Test
    void toStringContainsKeyFields() {
        Product product = new Product("SKU-202", "Клавіатура", "Механічна");
        product.setId(3L);

        String result = product.toString();

        assertTrue(result.contains("id=3"));
        assertTrue(result.contains("SKU-202"));
        assertTrue(result.contains("Клавіатура"));
    }
}
