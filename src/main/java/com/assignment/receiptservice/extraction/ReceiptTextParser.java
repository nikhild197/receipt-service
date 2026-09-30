package com.assignment.receiptservice.extraction;

public interface ReceiptTextParser {
    ReceiptExtraction parse(String rawOcrText);
}
