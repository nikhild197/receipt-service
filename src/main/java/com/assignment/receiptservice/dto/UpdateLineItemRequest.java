package com.assignment.receiptservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateLineItemRequest(
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal amount
) {
}
