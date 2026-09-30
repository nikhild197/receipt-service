package com.assignment.receiptservice.exception;

import com.assignment.receiptservice.service.ReconciliationResult;

public final class ItemizationMismatchException extends RuntimeException {
    private final ReconciliationResult reconciliationResult;

    public ItemizationMismatchException(ReconciliationResult reconciliationResult) {
        super("Line items do not reconcile with the transaction total");
        this.reconciliationResult = reconciliationResult;
    }

    public ReconciliationResult reconciliationResult() {
        return reconciliationResult;
    }
}
