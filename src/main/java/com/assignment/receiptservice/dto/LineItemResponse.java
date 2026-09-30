package com.assignment.receiptservice.dto;

import java.math.BigDecimal;

public record LineItemResponse(String description, BigDecimal amount) {
}
