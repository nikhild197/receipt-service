package com.assignment.receiptservice.dto;

import com.assignment.receiptservice.domain.ItemizeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID receiptId,
        String merchant,
        LocalDate date,
        String currency,
        BigDecimal grandTotal,
        List<TaxLineResponse> taxes,
        List<LineItemResponse> lineItems,
        ItemizeStatus itemizeStatus
) {
    public TransactionResponse {
        taxes = List.copyOf(taxes);
        lineItems = List.copyOf(lineItems);
    }
}
