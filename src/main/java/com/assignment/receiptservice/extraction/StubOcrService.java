package com.assignment.receiptservice.extraction;

import com.assignment.receiptservice.domain.Receipt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class StubOcrService implements OcrService {
    @Override
    public String extractText(Receipt receipt) {
        return new String(receipt.content(), StandardCharsets.UTF_8);
    }
}
