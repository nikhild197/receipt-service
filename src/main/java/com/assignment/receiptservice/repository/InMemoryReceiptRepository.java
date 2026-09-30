package com.assignment.receiptservice.repository;

import com.assignment.receiptservice.domain.Receipt;
import com.assignment.receiptservice.exception.ReceiptNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Repository
public class InMemoryReceiptRepository implements ReceiptRepository {
    private final ConcurrentHashMap<UUID, Receipt> storage = new ConcurrentHashMap<>();

    @Override
    public Receipt save(Receipt receipt) {
        storage.put(receipt.id(), receipt);
        return receipt;
    }

    @Override
    public Optional<Receipt> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Receipt update(UUID id, UnaryOperator<Receipt> updater) {
        return storage.compute(id, (key, current) -> {
            if (current == null) throw new ReceiptNotFoundException(id);
            return updater.apply(current);
        });
    }
}
