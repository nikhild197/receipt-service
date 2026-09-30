package com.assignment.receiptservice.extraction;

import com.assignment.receiptservice.domain.LineItem;
import com.assignment.receiptservice.domain.TaxLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReceiptExtraction(
        String merchant,
        LocalDate date,
        String currency,
        BigDecimal grandTotal,
        List<TaxLine> taxes,
        List<LineItem> lineItems
) {
    public ReceiptExtraction {
        taxes = taxes == null ? List.of() : List.copyOf(taxes);
        lineItems = lineItems == null ? List.of() : List.copyOf(lineItems);
    }
}
