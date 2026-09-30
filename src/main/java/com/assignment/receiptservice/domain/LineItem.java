package com.assignment.receiptservice.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record LineItem(String description, BigDecimal amount) {
    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Item description is required");
        }
        Objects.requireNonNull(amount, "Item amount is required");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Item amount must be non-negative");
        }
    }
}
