## Why

Story C2 requires the ability to consult/view invoices by ID or payment ID. This functionality was implemented as part of Story C1 (invoice creation), but Story C2 should be explicitly tracked and validated to ensure all requirements are met. The invoice retrieval capability needs to be clearly documented with explicit reference to Story C2 requirements (montant, devise, dates, statut, références paiement).

## What Changes

- **MODIFIED**: Invoice Retrieval requirement in invoice spec to explicitly reference Story C2 requirements
- **DOCUMENTATION**: Enhance specification to clearly state that retrieval enables consultation of invoice details for accounting, legal, and administrative purposes
- **VALIDATION**: Verify that all Story C2 requirements are covered by existing scenarios
- **TESTING**: Add integration tests if missing to fully validate Story C2 requirements

## Impact

- **Affected specs**: 
  - Modified capability `invoice` (enhance Invoice Retrieval requirement with Story C2 context)
- **Affected code**: 
  - No code changes required (functionality already exists)
  - May add integration tests for invoice retrieval endpoints to ensure Story C2 is fully validated
- **Database**: No changes

