package com.assignment.receiptservice.repository;

import com.assignment.receiptservice.domain.Transaction;
import com.assignment.receiptservice.exception.TransactionNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
    private final ConcurrentHashMap<UUID, Transaction> storage = new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        storage.put(transaction.id(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Transaction update(UUID id, UnaryOperator<Transaction> updater) {
        return storage.compute(id, (key, current) -> {
            if (current == null) throw new TransactionNotFoundException(id);
            return updater.apply(current);
        });
    }
}
