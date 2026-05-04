package com.store.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Модель надходження товару.
 * Описує операцію приймання товару, що зберігається в таблиці {@code receipts}.
 */
public class Receipt {
    private Long id;
    private Long productId;
    private Long userId;
    private Long qtyReceived;
    private BigDecimal wholesalePrice;
    private LocalDateTime receivedAt;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQtyReceived() {
        return qtyReceived;
    }

    public void setQtyReceived(Long qtyReceived) {
        this.qtyReceived = qtyReceived;
    }

    public BigDecimal getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(BigDecimal wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
