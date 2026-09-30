# Submission Checklist

This checklist maps the final repository to the take-home brief.

## Required implementation

- [x] `POST /receipts` - multipart receipt upload, returns `receiptId`
- [x] `POST /receipts/{id}/process` - OCR/extraction, transaction/taxes/items, raw OCR stored
- [x] `GET /transactions/{id}` - transaction with taxes, line items, and itemize status
- [x] `POST /transactions/{id}/itemize` - reuses stored OCR and replaces line items only
- [x] `PATCH /transactions/{id}/items` - manual edit/merge/split through full-list replacement
- [x] Non-reconciling manual edits return HTTP `409`
- [x] One receipt maps to one transaction identity during repeat/concurrent processing
- [x] Taxes represented as a list of tax records
- [x] `COMPLETE` / `NEEDS_REVIEW` / `FAILED` domain status available
- [x] Raw OCR retained for re-itemization
- [x] No fake balancing line for mismatched receipts
- [x] Receipt grand total remains authoritative
- [x] Optional `GET /health`

## Supplied fixtures

- [x] `receipt-clean.txt`
- [x] `receipt-tax-only.txt`
- [x] `receipt-mismatch.txt`
- [x] `gold.json`
- [x] Automated fixture coverage

## Submission/documentation

- [x] `README.md`
- [x] One-command run instructions
- [x] Example curl for every required endpoint
- [x] OCR approach explicitly documented as stubbed
- [x] `ARCHITECTURE.md`
- [x] Architecture diagram
- [x] Test matrix / manual verification notes (`TESTING.md`)
- [ ] Git repository URL - fill in after pushing
- [ ] Commit SHA - fill in after final commit

## Final commands

```bash
./mvnw clean test
./mvnw spring-boot:run
```

After the final commit:

```bash
git rev-parse HEAD
```

Submit the repository URL together with that exact commit SHA.
