# Testing and Verification

This document records the tests used to verify the receipt service against the supplied take-home fixtures and the important failure/concurrency cases.

## Run the automated suite

```bash
./mvnw test
```

The automated suite is under `src/test/java` and uses the supplied fixture text from `src/main/resources/fixtures`.

## Automated test coverage

| Area | Test / behavior | Expected |
|---|---|---|
| Application | Spring context loads | Pass |
| Clean fixture | Upload + process `receipt-clean.txt` | `COMPLETE`, 3 line items |
| Processing idempotency | Process same receipt twice | Same transaction ID |
| Tax-only fixture | Process `receipt-tax-only.txt` | `NEEDS_REVIEW`, no invented items |
| Mismatch fixture | Process `receipt-mismatch.txt` | `NEEDS_REVIEW`, 2 extracted items, total remains `18.50` |
| PATCH conflict | Non-reconciling item replacement | HTTP `409` |
| PATCH atomicity | GET after failed PATCH | Previous items unchanged |
| Valid merge | Replace 3 items with 2 reconciling items | HTTP `200`, `COMPLETE` |
| Re-itemize | Re-itemize after manual merge | Same transaction ID, original extracted items restored |
| Parser | Clean fixture parsing | Expected merchant/currency/total/tax/items |
| Parser | Tax-only parsing | No fabricated line items |
| Reconciliation | Net items + tax = total | Accepted |
| Reconciliation | Gross items = total | Accepted |
| Reconciliation | Empty itemization | Rejected |
| Reconciliation | Mismatched itemization | Rejected |
| Concurrency | 32 concurrent process calls for one receipt | One unique transaction ID |

## Manual API scenarios exercised

These scenarios were also tested against the running service on port `8080`.

| Scenario | Result verified |
|---|---|
| `GET /health` | Service reachable |
| Upload clean receipt | New `receiptId` returned |
| Process clean receipt | `Cafe Mitte`, `17.85`, VAT `2.85`, 3 items, `COMPLETE` |
| Upload same file again | New receipt ID (expected: upload creates a new resource) |
| Process same receipt ID again | Same transaction ID |
| `GET /transactions/{id}` | Stored transaction returned unchanged |
| Tax-only receipt | `Berlin Taxi GmbH`, `24.00`, VAT `3.83`, no items, `NEEDS_REVIEW` |
| Mismatch receipt | `Hotel Shop`, `18.50`, VAT `1.90`, Water + Snacks, `NEEDS_REVIEW` |
| Valid merge PATCH | Succeeds and remains `COMPLETE` |
| Invalid PATCH | HTTP `409`, `ITEM_TOTAL_MISMATCH` |
| GET after failed PATCH | Prior valid state preserved |
| Re-itemize clean receipt | Same transaction ID; source OCR items restored |
| Re-itemize mismatch | Remains `NEEDS_REVIEW` |
| Re-itemize tax-only | Remains `NEEDS_REVIEW`, no items |
| Unknown receipt | HTTP `404`, `NOT_FOUND` |
| Unknown transaction | HTTP `404`, `NOT_FOUND` |
| Blank item description | HTTP `400`, `VALIDATION_ERROR` |
| Negative amount | HTTP `400`, `VALIDATION_ERROR` |
| Empty replacement list | HTTP `409`, reconciliation details returned |

## Fixture expectations

### `receipt-clean.txt`

- merchant: `Cafe Mitte`
- total: `17.85 EUR`
- VAT: `19% / 2.85`
- items: Espresso `3.50`, Sandwich `8.90`, Mineral water `2.60`
- status: `COMPLETE`

### `receipt-tax-only.txt`

- merchant: `Berlin Taxi GmbH`
- total: `24.00 EUR`
- VAT: `19% / 3.83`
- no reliable itemized list
- status: `NEEDS_REVIEW`

### `receipt-mismatch.txt`

- merchant: `Hotel Shop`
- total: `18.50 EUR`
- VAT: `19% / 1.90`
- items: Water `4.00`, Snacks `6.00`
- status: `NEEDS_REVIEW`
- the implementation must not change the grand total or invent a balancing item

## Status-code contract verified

- `201 Created` - successful receipt upload
- `200 OK` - successful processing, retrieval, re-itemization, or valid item replacement
- `400 Bad Request` - malformed/invalid input such as blank description or negative amount
- `404 Not Found` - unknown receipt or transaction
- `409 Conflict` - syntactically valid item replacement that does not reconcile with the stored transaction

## Final local verification before submission

Run from the repository root:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

Then exercise the README curls once against the running application.
