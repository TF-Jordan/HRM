package yowyob.comops.api.payroll.domain.model;

/**
 * Lifecycle of a payroll cycle, richer than the legacy hrm-core model.
 *
 * <pre>
 * DRAFT → VARIABLES_LOCKED → CALCULATED → REVIEW → VALIDATED → APPROVED → PAYMENT_INITIATED → PAID → CLOSED
 * </pre>
 *
 * The separation between VALIDATED (payroll officer) and APPROVED (finance / DAF)
 * materialises the segregation-of-duties control required for auditable payroll.
 */
public enum PayrollRunStatus {
    DRAFT,
    VARIABLES_LOCKED,
    CALCULATED,
    REVIEW,
    VALIDATED,
    APPROVED,
    PAYMENT_INITIATED,
    PAID,
    CLOSED
}
