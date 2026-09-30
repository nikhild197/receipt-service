package com.assignment.receiptservice.exception;

import java.util.UUID;

public class ReceiptNotFoundException extends RuntimeException {
    public ReceiptNotFoundException(UUID id) {
        super("Receipt not found: " + id);
    }
}
