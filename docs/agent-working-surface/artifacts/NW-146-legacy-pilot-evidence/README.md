# NW-146 Legacy Pilot Evidence

Status: non-authoritative sanitized evidence packet
Document type: bounded evidence manifest
Authority: evidence only; not a product specification, platform specification,
contract, migration plan, runtime fixture, production approval, or import source

## Purpose

This directory tracks sanitized legacy form-definition samples for NW-146,
NW-093, and successor compatibility planning.

The files are committed because the form definitions contain useful structure
for online agents and reviewers: form identity/version metadata, sections,
fields, labels, option-set references, rule expressions, validation rules, and
repeatable-section pressure.

## Evidence Boundary

- Evidence only, not product spec / contract / authority.
- Sanitized/redacted by owner before commit.
- No raw submissions.
- No secrets.
- No real account import.
- No user/account export committed.
- User/account export remains untracked and must be classified by NW-093 before
  any use.
- Used only for NW-093 and successor compatibility planning.
- Do not treat legacy form names, field names, team labels, orgUnit labels,
  option values, or rule syntax as Datarun primitives.

## Included Form Definitions

All included samples are strict JSON form-definition exports.

| File | Evidence value |
|---|---|
| `chv-malaria_supply_consumption_form.json` | Malaria supply consumption form structure and repeatable movement pressure. |
| `chv_cases_register_form.json` | CHV case registration/management form structure, validation, option sets, and bilingual labels. |
| `chv_supervision_form.json` | Supervision form structure with many fields, repeatable sections, and rule pressure. |
| `health_facility_visit_form.json` | Facility visit form structure with repeatable sections and disease/reporting pressure. |
| `itns_distribution_household_form.json` | ITN household distribution form structure, validation rules, show/hide/error pressure, and household/campaign context. |
| `itns_issue_form_wh_keeper_form.json` | ITN issue/warehouse keeper form structure and stock movement pressure. |
| `itns_reconcile_team_disbursed_itns_form.json` | ITN reconciliation form structure and review/reconciliation pressure. |
| `itns_team_receipt_form.json` | ITN team receipt form structure and stock receipt pressure. |
| `supply_hf_mids_receipt_902_form.json` | Health-facility receipt form structure and invoice-detail repeatable pressure. |
| `supply_hf_mids_return_904_form.json` | Health-facility return form structure and invoice-detail repeatable pressure. |
| `supply_mids_disbursement_invoice_903_form.json` | Disbursement invoice form structure and invoice-detail repeatable pressure. |
| `supply_wh_mids_stocktake_901_form.json` | Warehouse stocktake form structure and inventory/reconciliation pressure. |

## Excluded Evidence

- `legacy_users_safe.csv` remains untracked because it is account/user export
  pressure and must be classified under NW-093 before any use.
- Raw submitted records are not committed.
- Runtime seed data, importer inputs, and production fixtures are not created
  by this evidence packet.
