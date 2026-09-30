package com.assignment.receiptservice.extraction;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FixtureReceiptTextParserTest {
    private final ReceiptTextParser parser = new FixtureReceiptTextParser();

    @Test
    void parsesCleanFixtureIntoExpectedStructuredData() throws Exception {
        ReceiptExtraction extraction = parser.parse(resource("/fixtures/receipt-clean.txt"));

        assertThat(extraction.merchant()).isEqualTo("Cafe Mitte");
        assertThat(extraction.currency()).isEqualTo("EUR");
        assertThat(extraction.grandTotal()).isEqualByComparingTo("17.85");
        assertThat(extraction.taxes()).hasSize(1);
        assertThat(extraction.lineItems()).hasSize(3);
    }

    @Test
    void taxOnlyFixtureDoesNotInventLineItems() throws Exception {
        ReceiptExtraction extraction = parser.parse(resource("/fixtures/receipt-tax-only.txt"));

        assertThat(extraction.grandTotal()).isEqualByComparingTo("24.00");
        assertThat(extraction.taxes()).hasSize(1);
        assertThat(extraction.lineItems()).isEmpty();
    }

    private String resource(String path) throws Exception {
        try (var stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing test resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
