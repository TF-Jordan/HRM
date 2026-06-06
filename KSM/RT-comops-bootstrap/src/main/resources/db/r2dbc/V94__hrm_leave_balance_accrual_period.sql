-- V094: monthly leave-accrual idempotency marker.
--
-- Records the YYYY-MM period for which the scheduled monthly leave accrual last credited this
-- balance. The accrual job credits ANNUAL leave at most once per calendar month per employee:
-- it skips a balance whose last_accrual_period already equals the current month, so the job is
-- safe to run repeatedly (daily cron, catch-up after downtime, restarts) without over-crediting.

ALTER TABLE hrm_leave_balance
    ADD COLUMN IF NOT EXISTS last_accrual_period VARCHAR(7);
