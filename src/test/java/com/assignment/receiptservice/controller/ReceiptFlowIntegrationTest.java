package com.assignment.receiptservice.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReceiptFlowIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void cleanReceiptProcessesAndReprocessingKeepsSameTransaction() throws Exception {
        String receiptId = upload("receipt-clean.txt", resource("/fixtures/receipt-clean.txt"));

        String first = mockMvc.perform(post("/receipts/{id}/process", receiptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchant").value("Cafe Mitte"))
                .andExpect(jsonPath("$.itemizeStatus").value("COMPLETE"))
                .andExpect(jsonPath("$.lineItems.length()").value(3))
                .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(post("/receipts/{id}/process", receiptId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(first).get("id").asText())
                .isEqualTo(objectMapper.readTree(second).get("id").asText());
    }

    @Test
    void taxOnlyNeedsReview() throws Exception {
        String receiptId = upload("receipt-tax-only.txt", resource("/fixtures/receipt-tax-only.txt"));
        mockMvc.perform(post("/receipts/{id}/process", receiptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemizeStatus").value("NEEDS_REVIEW"))
                .andExpect(jsonPath("$.lineItems.length()").value(0));
    }

    @Test
    void mismatchNeedsReviewWithoutBalancingItem() throws Exception {
        String receiptId = upload("receipt-mismatch.txt", resource("/fixtures/receipt-mismatch.txt"));
        mockMvc.perform(post("/receipts/{id}/process", receiptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(18.5))
                .andExpect(jsonPath("$.itemizeStatus").value("NEEDS_REVIEW"))
                .andExpect(jsonPath("$.lineItems.length()").value(2));
    }

    @Test
    void invalidPatchReturns409AndDoesNotMutateItems() throws Exception {
        String receiptId = upload("receipt-clean.txt", resource("/fixtures/receipt-clean.txt"));
        String processed = mockMvc.perform(post("/receipts/{id}/process", receiptId))
                .andReturn().getResponse().getContentAsString();
        String transactionId = objectMapper.readTree(processed).get("id").asText();

        mockMvc.perform(patch("/transactions/{id}/items", transactionId)
                        .contentType("application/json")
                        .content("""
                                {"items":[{"description":"Coffee","amount":2.00}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ITEM_TOTAL_MISMATCH"));

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineItems.length()").value(3));
    }

    @Test
    void validPatchCanMergeItemsAndReitemizeKeepsTransactionId() throws Exception {
        String receiptId = upload("receipt-clean.txt", resource("/fixtures/receipt-clean.txt"));
        JsonNode tx = objectMapper.readTree(mockMvc.perform(post("/receipts/{id}/process", receiptId))
                .andReturn().getResponse().getContentAsString());
        String transactionId = tx.get("id").asText();

        mockMvc.perform(patch("/transactions/{id}/items", transactionId)
                        .contentType("application/json")
                        .content("""
                                {"items":[
                                  {"description":"Coffee and sandwich","amount":12.40},
                                  {"description":"Mineral water","amount":2.60}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineItems.length()").value(2))
                .andExpect(jsonPath("$.itemizeStatus").value("COMPLETE"));

        mockMvc.perform(post("/transactions/{id}/itemize", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.merchant").value("Cafe Mitte"))
                .andExpect(jsonPath("$.lineItems.length()").value(3));
    }

    private String upload(String filename, String body) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, "text/plain",
                body.getBytes(StandardCharsets.UTF_8));
        String response = mockMvc.perform(multipart("/receipts").file(file))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("receiptId").asText();
    }

    private String resource(String path) throws Exception {
        try (var stream = getClass().getResourceAsStream(path)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
