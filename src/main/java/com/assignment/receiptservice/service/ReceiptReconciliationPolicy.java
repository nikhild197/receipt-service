package com.assignment.receiptservice.service;

import com.assignment.receiptservice.domain.LineItem;
import com.assignment.receiptservice.domain.TaxLine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public final class ReceiptReconciliationPolicy {
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal RECONCILIATION_TOLERANCE = new BigDecimal("0.01");

    public ReconciliationResult evaluate(
            BigDecimal grandTotal,
            List<TaxLine> taxes,
            List<LineItem> lineItems
    ) {
        if (grandTotal == null) {
            throw new IllegalArgumentException("Grand total is required");
        }

        List<TaxLine> safeTaxes = taxes == null ? List.of() : taxes;
        List<LineItem> safeItems = lineItems == null ? List.of() : lineItems;

        BigDecimal normalizedGrandTotal = normalize(grandTotal);
        BigDecimal itemTotal = normalize(safeItems.stream()
                .map(LineItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal taxTotal = normalize(safeTaxes.stream()
                .map(TaxLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal expectedTotal = normalize(itemTotal.add(taxTotal));

        boolean grossItemsMatch = withinTolerance(itemTotal, normalizedGrandTotal);
        boolean netItemsMatch = withinTolerance(expectedTotal, normalizedGrandTotal);
        boolean reconciled = !safeItems.isEmpty() && (grossItemsMatch || netItemsMatch);

        return new ReconciliationResult(
                reconciled,
                normalizedGrandTotal,
                itemTotal,
                taxTotal,
                expectedTotal,
                normalize(normalizedGrandTotal.subtract(expectedTotal))
        );
    }

    private boolean withinTolerance(BigDecimal left, BigDecimal right) {
        return left.subtract(right).abs().compareTo(RECONCILIATION_TOLERANCE) <= 0;
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
