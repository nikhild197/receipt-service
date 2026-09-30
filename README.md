# Receipt Service

A focused Spring Boot implementation of receipt upload, stub OCR, tax extraction, auto-itemization, reconciliation, re-itemization, and manual line-item overrides.

## Design goals

- Java 21 + Spring Boot
- In-memory `ConcurrentHashMap` persistence
- Immutable domain records
- `BigDecimal` for money
- Raw OCR retained and reused for re-itemization
- Atomic per-key mutations with `ConcurrentHashMap.compute`
- No global `synchronized` lock or lock registry
- Thin controllers with explicit request/response DTOs
- Domain/API mapping isolated in `TransactionMapper`
- Receipt reconciliation isolated as a domain policy
- OCR and parsing behind replaceable interfaces
- No unnecessary database, queue, cache, or OCR vendor dependency

## Intentional patterns

Only patterns that create a useful boundary are used:

- **Repository pattern**: `ReceiptRepository`, `TransactionRepository`
- **Strategy/port boundary**: `OcrService`, `ReceiptTextParser`
- **Domain policy**: `ReceiptReconciliationPolicy`
- **Mapper pattern**: `TransactionMapper`
- **Dependency injection**: constructor injection throughout

The implementation intentionally avoids generic base repositories, abstract CRUD services, factories, builders, CQRS, event sourcing, and other abstractions that do not add value to this take-home.

## Run

```bash
./mvnw spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/health
```

## API examples

Upload:

```bash
curl -F "file=@src/main/resources/fixtures/receipt-clean.txt" http://localhost:8080/receipts
```

Process:

```bash
curl -X POST http://localhost:8080/receipts/<receipt-id>/process
```

Get transaction:

```bash
curl http://localhost:8080/transactions/<transaction-id>
```

Re-itemize from stored OCR:

```bash
curl -X POST http://localhost:8080/transactions/<transaction-id>/itemize
```

Replace / merge / split items:

```bash
curl -X PATCH http://localhost:8080/transactions/<transaction-id>/items \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"description":"Coffee and sandwich","amount":12.40},{"description":"Mineral water","amount":2.60}]}'
```

A non-reconciling edit returns HTTP `409` and leaves the existing transaction unchanged.

## Reconciliation rule

The service accepts either:

1. net line items + stored taxes = grand total, or
2. gross line items = grand total.

A one-cent tolerance is allowed for ordinary receipt rounding. Empty or mismatched line items produce `NEEDS_REVIEW`. The service never invents a balancing line and never changes the receipt grand total to force reconciliation.

## Concurrency and idempotency

`POST /receipts/{id}/process` is safe to repeat and can be called concurrently in this single-JVM implementation.

OCR/parsing happens outside the atomic map mutation. The receipt repository then uses `ConcurrentHashMap.compute` to install a transaction ID only if one is not already present. Racing requests therefore converge on the same transaction identity instead of relying on a global lock.

PATCH and re-itemization also use per-key `compute`, keeping read/validate/replace atomic for a transaction.

## Tests

```bash
./mvnw test
```

Coverage includes:

- clean fixture -> `COMPLETE`
- tax-only fixture -> `NEEDS_REVIEW`
- mismatch fixture -> `NEEDS_REVIEW` without balancing data
- repeated processing keeps the same transaction ID
- concurrent processing keeps one transaction identity
- invalid PATCH returns `409` without mutation
- valid merge-style PATCH
- re-itemization keeps transaction identity and header data
- parser behavior
- net and gross reconciliation modes

## OCR choice

OCR is intentionally **stubbed** for this take-home. `StubOcrService` treats the uploaded fixture text as already-extracted OCR text, and `FixtureReceiptTextParser` extracts the structured fields from that text. No external OCR/VLM vendor or API key is required.

This follows the exercise's fixture-driven path: the supplied `.txt` files represent known OCR output for the receipts. A production implementation can replace `OcrService` without changing the HTTP or domain contracts.

## Verification

See [`TESTING.md`](TESTING.md) for the automated test inventory, the manual API scenarios exercised against the supplied fixtures, expected statuses, and the final pre-submission verification checklist.

See [`SUBMISSION_CHECKLIST.md`](SUBMISSION_CHECKLIST.md) for the brief-to-deliverable checklist and the two submission values that must be filled in after pushing the repository: repository URL and commit SHA.
