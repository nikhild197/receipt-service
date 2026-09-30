package com.assignment.receiptservice.controller;

import com.assignment.receiptservice.dto.TransactionResponse;
import com.assignment.receiptservice.dto.UpdateItemsRequest;
import com.assignment.receiptservice.mapper.TransactionMapper;
import com.assignment.receiptservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public final class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse get(@PathVariable UUID transactionId) {
        return transactionMapper.toResponse(transactionService.get(transactionId));
    }

    @PostMapping("/{transactionId}/itemize")
    public TransactionResponse reItemize(@PathVariable UUID transactionId) {
        return transactionMapper.toResponse(transactionService.reItemize(transactionId));
    }

    @PatchMapping("/{transactionId}/items")
    public TransactionResponse replaceItems(
            @PathVariable UUID transactionId,
            @Valid @RequestBody UpdateItemsRequest request
    ) {
        return transactionMapper.toResponse(
                transactionService.replaceItems(
                        transactionId,
                        transactionMapper.toDomainItems(request.items())
                )
        );
    }
}
