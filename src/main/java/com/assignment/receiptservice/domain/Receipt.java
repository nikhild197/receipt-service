package com.assignment.receiptservice.domain;

import java.util.Arrays;
import java.util.UUID;

public record Receipt(
        UUID id,
        String originalFilename,
        String contentType,
        byte[] content,
        String rawOcrText,
        UUID transactionId
) {
    public Receipt {
        if (id == null) {
            throw new IllegalArgumentException("Receipt id is required");
        }
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    public Receipt withProcessingResult(String rawOcrText, UUID transactionId) {
        return new Receipt(id, originalFilename, contentType, content, rawOcrText, transactionId);
    }
}
