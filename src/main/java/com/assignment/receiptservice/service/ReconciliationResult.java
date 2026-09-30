package com.assignment.receiptservice.service;

import java.math.BigDecimal;

public record ReconciliationResult(
        boolean reconciled,
        BigDecimal grandTotal,
        BigDecimal itemTotal,
        BigDecimal taxTotal,
        BigDecimal expectedTotal,
        BigDecimal difference
) {
}
