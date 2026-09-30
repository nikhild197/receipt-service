package com.assignment.receiptservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateItemsRequest(
        @NotNull List<@Valid UpdateLineItemRequest> items
) {
    public UpdateItemsRequest {
        items = items == null ? null : List.copyOf(items);
    }
}
