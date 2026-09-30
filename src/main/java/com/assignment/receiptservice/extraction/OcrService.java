package com.assignment.receiptservice.extraction;

import com.assignment.receiptservice.domain.Receipt;

public interface OcrService {
    String extractText(Receipt receipt);
}
