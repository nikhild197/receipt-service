package com.assignment.receiptservice.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Transaction(
        UUID id,
        UUID receiptId,
        String merchant,
        LocalDate date,
        String currency,
        BigDecimal grandTotal,
        List<TaxLine> taxes,
        List<LineItem> lineItems,
        ItemizeStatus itemizeStatus
) {
    public Transaction {
        if (id == null) throw new IllegalArgumentException("Transaction id is required");
        if (receiptId == null) throw new IllegalArgumentException("Receipt id is required");
        if (merchant == null || merchant.isBlank()) throw new IllegalArgumentException("Merchant is required");
        if (date == null) throw new IllegalArgumentException("Transaction date is required");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        if (grandTotal == null || grandTotal.signum() < 0) {
            throw new IllegalArgumentException("Grand total must be non-negative");
        }
        taxes = taxes == null ? List.of() : List.copyOf(taxes);
        lineItems = lineItems == null ? List.of() : List.copyOf(lineItems);
        if (itemizeStatus == null) throw new IllegalArgumentException("Itemize status is required");
    }

    public Transaction withLineItems(List<LineItem> newItems, ItemizeStatus newStatus) {
        return new Transaction(id, receiptId, merchant, date, currency, grandTotal, taxes, newItems, newStatus);
    }
}
