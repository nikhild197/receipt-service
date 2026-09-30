package com.assignment.receiptservice.controller;

import com.assignment.receiptservice.dto.ReceiptUploadResponse;
import com.assignment.receiptservice.dto.TransactionResponse;
import com.assignment.receiptservice.mapper.TransactionMapper;
import com.assignment.receiptservice.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/receipts")
public final class ReceiptController {
    private final ReceiptService receiptService;
    private final TransactionMapper transactionMapper;

    public ReceiptController(ReceiptService receiptService, TransactionMapper transactionMapper) {
        this.receiptService = receiptService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceiptUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return receiptService.upload(file);
    }

    @PostMapping("/{receiptId}/process")
    public TransactionResponse process(@PathVariable UUID receiptId) {
        return transactionMapper.toResponse(receiptService.process(receiptId));
    }
}
