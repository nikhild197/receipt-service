package com.assignment.receiptservice.dto;

import java.math.BigDecimal;

public record TaxLineResponse(String name, BigDecimal rate, BigDecimal amount) {
}
