package com.assignment.receiptservice.service;

import com.assignment.receiptservice.domain.ItemizeStatus;
import com.assignment.receiptservice.domain.Receipt;
import com.assignment.receiptservice.domain.Transaction;
import com.assignment.receiptservice.dto.ReceiptUploadResponse;
import com.assignment.receiptservice.exception.ReceiptContentReadException;
import com.assignment.receiptservice.exception.ReceiptNotFoundException;
import com.assignment.receiptservice.extraction.OcrService;
import com.assignment.receiptservice.extraction.ReceiptExtraction;
import com.assignment.receiptservice.extraction.ReceiptTextParser;
import com.assignment.receiptservice.repository.ReceiptRepository;
import com.assignment.receiptservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public final class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final TransactionRepository transactionRepository;
    private final OcrService ocrService;
    private final ReceiptTextParser receiptTextParser;
    private final ReceiptReconciliationPolicy reconciliationPolicy;

    public ReceiptService(
            ReceiptRepository receiptRepository,
            TransactionRepository transactionRepository,
            OcrService ocrService,
            ReceiptTextParser receiptTextParser,
            ReceiptReconciliationPolicy reconciliationPolicy
    ) {
        this.receiptRepository = receiptRepository;
        this.transactionRepository = transactionRepository;
        this.ocrService = ocrService;
        this.receiptTextParser = receiptTextParser;
        this.reconciliationPolicy = reconciliationPolicy;
    }

    public ReceiptUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Receipt file is required");
        }

        try {
            UUID receiptId = UUID.randomUUID();
            Receipt receipt = new Receipt(
                    receiptId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    null,
                    null
            );
            receiptRepository.save(receipt);
            return new ReceiptUploadResponse(receiptId);
        } catch (IOException exception) {
            throw new ReceiptContentReadException("Unable to read uploaded receipt content", exception);
        }
    }

    public Transaction process(UUID receiptId) {
        Receipt receipt = getRequiredReceipt(receiptId);

        // Potentially expensive work stays outside the atomic repository mutation.
        String rawOcrText = ocrService.extractText(receipt);
        ReceiptExtraction extraction = receiptTextParser.parse(rawOcrText);
        ItemizeStatus itemizeStatus = reconciliationPolicy
                .evaluate(extraction.grandTotal(), extraction.taxes(), extraction.lineItems())
                .reconciled()
                ? ItemizeStatus.COMPLETE
                : ItemizeStatus.NEEDS_REVIEW;

        // Atomically establish the one-receipt/one-transaction identity.
        UUID candidateTransactionId = UUID.randomUUID();
        Receipt processedReceipt = receiptRepository.update(receiptId, current -> {
            UUID transactionId = current.transactionId() == null
                    ? candidateTransactionId
                    : current.transactionId();
            return current.withProcessingResult(rawOcrText, transactionId);
        });

        Transaction transaction = new Transaction(
                processedReceipt.transactionId(),
                processedReceipt.id(),
                extraction.merchant(),
                extraction.date(),
                extraction.currency(),
                extraction.grandTotal(),
                extraction.taxes(),
                extraction.lineItems(),
                itemizeStatus
        );

        return transactionRepository.save(transaction);
    }

    private Receipt getRequiredReceipt(UUID receiptId) {
        return receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ReceiptNotFoundException(receiptId));
    }
}
