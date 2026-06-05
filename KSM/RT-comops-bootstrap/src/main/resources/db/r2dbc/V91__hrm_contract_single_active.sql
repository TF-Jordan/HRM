-- V091: enforce the invariant "one ACTIVE contract per employee" (non-negotiable).
--
-- 1. Resolve any pre-existing duplicates so the constraint can be applied: keep the
--    most recent ACTIVE contract per (tenant_id, employee_id) — latest date_debut,
--    then created_at — and demote the others to EXPIRED.
-- 2. Add a partial unique index making a second ACTIVE contract physically impossible,
--    even outside the application API.

-- 1. Clean up existing duplicate active contracts
WITH ranked AS (
    SELECT id,
           row_number() OVER (
               PARTITION BY tenant_id, employee_id
               ORDER BY date_debut DESC, created_at DESC, id
           ) AS rn
    FROM hrm_contract
    WHERE status = 'ACTIVE'
)
UPDATE hrm_contract c
SET status     = 'EXPIRED',
    motif_fin  = COALESCE(c.motif_fin, 'Auto-cloture: un seul contrat actif autorise par employe'),
    updated_at = now()
FROM ranked r
WHERE c.id = r.id
  AND r.rn > 1;

-- 2. Guarantee uniqueness of the active contract per employee
CREATE UNIQUE INDEX IF NOT EXISTS idx_hrm_contract_one_active_per_employee
    ON hrm_contract (tenant_id, employee_id)
    WHERE status = 'ACTIVE';
