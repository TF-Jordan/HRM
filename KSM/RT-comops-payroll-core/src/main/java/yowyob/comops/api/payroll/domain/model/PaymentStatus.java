package yowyob.comops.api.payroll.domain.model;

/** Per-employee payment lifecycle, updated from banking / mobile-money callbacks. */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
