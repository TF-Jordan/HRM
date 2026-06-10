package yowyob.comops.api.payroll.domain.model;

/**
 * Lifecycle of a payroll cycle, richer than the legacy hrm-core model.
 *
 * <pre>
 * DRAFT → VARIABLES_LOCKED → CALCULATED → REVIEW → VALIDATED → APPROVED → PAYMENT_INITIATED → PAID → CLOSED
 *                                  ↑          │
 *                                  └─ REJECTED ┘  (HR admin returns the cycle with a justification)
 * </pre>
 *
 * The separation between VALIDATED (payroll officer) and APPROVED (finance / DAF)
 * materialises the segregation-of-duties control required for auditable payroll.
 *
 * REJECTED is a non-terminal state: only the HR admin (validator) can move a CALCULATED/REVIEW
 * cycle there, attaching a mandatory justification routed to the payroll manager, who recalculates
 * to bring the cycle back to CALCULATED.
 */
public enum PayrollRunStatus {
    DRAFT,
    VARIABLES_LOCKED,
    CALCULATED,
    REVIEW,
    REJECTED,
    VALIDATED,
    APPROVED,
    PAYMENT_INITIATED,
    PAID,
    CLOSED
}
