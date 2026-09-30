package com.assignment.receiptservice.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record TaxLine(String name, BigDecimal rate, BigDecimal amount) {
    public TaxLine {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tax name is required");
        }
        Objects.requireNonNull(rate, "Tax rate is required");
        Objects.requireNonNull(amount, "Tax amount is required");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("Tax rate must be non-negative");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Tax amount must be non-negative");
        }
    }
}
