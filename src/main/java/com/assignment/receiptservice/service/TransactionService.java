package com.assignment.receiptservice.service;

import com.assignment.receiptservice.domain.ItemizeStatus;
import com.assignment.receiptservice.domain.LineItem;
import com.assignment.receiptservice.domain.Receipt;
import com.assignment.receiptservice.domain.Transaction;
import com.assignment.receiptservice.exception.ItemizationMismatchException;
import com.assignment.receiptservice.exception.ReceiptNotFoundException;
import com.assignment.receiptservice.exception.TransactionNotFoundException;
import com.assignment.receiptservice.extraction.ReceiptExtraction;
import com.assignment.receiptservice.extraction.ReceiptTextParser;
import com.assignment.receiptservice.repository.ReceiptRepository;
import com.assignment.receiptservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public final class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptTextParser receiptTextParser;
    private final ReceiptReconciliationPolicy reconciliationPolicy;

    public TransactionService(
            TransactionRepository transactionRepository,
            ReceiptRepository receiptRepository,
            ReceiptTextParser receiptTextParser,
            ReceiptReconciliationPolicy reconciliationPolicy
    ) {
        this.transactionRepository = transactionRepository;
        this.receiptRepository = receiptRepository;
        this.receiptTextParser = receiptTextParser;
        this.reconciliationPolicy = reconciliationPolicy;
    }

    public Transaction get(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    public Transaction reItemize(UUID transactionId) {
        Transaction transaction = get(transactionId);
        Receipt receipt = receiptRepository.findById(transaction.receiptId())
                .orElseThrow(() -> new ReceiptNotFoundException(transaction.receiptId()));

        if (receipt.rawOcrText() == null || receipt.rawOcrText().isBlank()) {
            throw new IllegalStateException("Stored OCR text is unavailable");
        }

        ReceiptExtraction extraction = receiptTextParser.parse(receipt.rawOcrText());

        return transactionRepository.update(transactionId, current -> {
            ReconciliationResult result = reconciliationPolicy.evaluate(
                    current.grandTotal(),
                    current.taxes(),
                    extraction.lineItems()
            );
            ItemizeStatus status = result.reconciled()
                    ? ItemizeStatus.COMPLETE
                    : ItemizeStatus.NEEDS_REVIEW;
            return current.withLineItems(extraction.lineItems(), status);
        });
    }

    public Transaction replaceItems(UUID transactionId, List<LineItem> requestedItems) {
        List<LineItem> lineItems = List.copyOf(requestedItems);

        return transactionRepository.update(transactionId, current -> {
            ReconciliationResult result = reconciliationPolicy.evaluate(
                    current.grandTotal(),
                    current.taxes(),
                    lineItems
            );

            if (!result.reconciled()) {
                throw new ItemizationMismatchException(result);
            }

            return current.withLineItems(lineItems, ItemizeStatus.COMPLETE);
        });
    }
}
