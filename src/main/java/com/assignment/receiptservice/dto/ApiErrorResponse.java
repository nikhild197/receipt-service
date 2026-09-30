package com.assignment.receiptservice.dto;

public record ApiErrorResponse(String code, String message, Object details) {
}
