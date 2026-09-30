package com.assignment.receiptservice.repository;

import com.assignment.receiptservice.domain.Receipt;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface ReceiptRepository {
    Receipt save(Receipt receipt);
    Optional<Receipt> findById(UUID id);
    Receipt update(UUID id, UnaryOperator<Receipt> updater);
}
