package com.assignment.receiptservice.repository;

import com.assignment.receiptservice.domain.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    Transaction update(UUID id, UnaryOperator<Transaction> updater);
}
