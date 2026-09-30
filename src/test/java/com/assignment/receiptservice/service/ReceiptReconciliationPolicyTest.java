package com.assignment.receiptservice.service;

import com.assignment.receiptservice.domain.LineItem;
import com.assignment.receiptservice.domain.TaxLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptReconciliationPolicyTest {
    private final ReceiptReconciliationPolicy policy = new ReceiptReconciliationPolicy();

    @Test
    void acceptsNetItemsPlusTaxWhenTheyMatchGrandTotal() {
        var result = policy.evaluate(
                new BigDecimal("17.85"),
                List.of(new TaxLine("VAT", new BigDecimal("0.19"), new BigDecimal("2.85"))),
                List.of(
                        new LineItem("Espresso", new BigDecimal("3.50")),
                        new LineItem("Sandwich", new BigDecimal("8.90")),
                        new LineItem("Mineral water", new BigDecimal("2.60"))
                )
        );

        assertThat(result.reconciled()).isTrue();
    }

    @Test
    void acceptsGrossItemsThatAlreadyEqualGrandTotal() {
        var result = policy.evaluate(
                new BigDecimal("17.85"),
                List.of(new TaxLine("VAT", new BigDecimal("0.19"), new BigDecimal("2.85"))),
                List.of(new LineItem("Gross total itemization", new BigDecimal("17.85")))
        );

        assertThat(result.reconciled()).isTrue();
    }

    @Test
    void rejectsMissingOrMismatchedItems() {
        assertThat(policy.evaluate(
                new BigDecimal("24.00"),
                List.of(new TaxLine("VAT", new BigDecimal("0.19"), new BigDecimal("3.83"))),
                List.of()
        ).reconciled()).isFalse();

        assertThat(policy.evaluate(
                new BigDecimal("18.50"),
                List.of(new TaxLine("VAT", new BigDecimal("0.19"), new BigDecimal("1.90"))),
                List.of(
                        new LineItem("Water", new BigDecimal("4.00")),
                        new LineItem("Snacks", new BigDecimal("6.00"))
                )
        ).reconciled()).isFalse();
    }
}
