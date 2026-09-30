package com.assignment.receiptservice.service;

import com.assignment.receiptservice.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReceiptServiceConcurrencyTest {
    @Autowired
    private ReceiptService receiptService;

    @Test
    void concurrentProcessingOfSameReceiptProducesSingleTransactionIdentity() throws Exception {
        String fixture = resource("/fixtures/receipt-clean.txt");
        var upload = receiptService.upload(new MockMultipartFile(
                "file",
                "receipt-clean.txt",
                "text/plain",
                fixture.getBytes(StandardCharsets.UTF_8)
        ));

        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Callable<Transaction>> tasks = java.util.stream.IntStream.range(0, 32)
                    .mapToObj(ignored -> (Callable<Transaction>) () -> receiptService.process(upload.receiptId()))
                    .toList();

            var futures = executor.invokeAll(tasks);
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

            var transactionIds = new HashSet<>();
            for (var future : futures) {
                transactionIds.add(future.get().id());
            }

            assertThat(transactionIds).hasSize(1);
        }
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
