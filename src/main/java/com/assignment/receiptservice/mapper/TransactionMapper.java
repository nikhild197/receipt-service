package com.assignment.receiptservice.mapper;

import com.assignment.receiptservice.domain.LineItem;
import com.assignment.receiptservice.domain.Transaction;
import com.assignment.receiptservice.dto.LineItemResponse;
import com.assignment.receiptservice.dto.TaxLineResponse;
import com.assignment.receiptservice.dto.TransactionResponse;
import com.assignment.receiptservice.dto.UpdateLineItemRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.receiptId(),
                transaction.merchant(),
                transaction.date(),
                transaction.currency(),
                transaction.grandTotal(),
                transaction.taxes().stream()
                        .map(tax -> new TaxLineResponse(tax.name(), tax.rate(), tax.amount()))
                        .toList(),
                transaction.lineItems().stream()
                        .map(item -> new LineItemResponse(item.description(), item.amount()))
                        .toList(),
                transaction.itemizeStatus()
        );
    }

    public List<LineItem> toDomainItems(List<UpdateLineItemRequest> requests) {
        return requests.stream()
                .map(request -> new LineItem(request.description(), request.amount()))
                .toList();
    }
}
