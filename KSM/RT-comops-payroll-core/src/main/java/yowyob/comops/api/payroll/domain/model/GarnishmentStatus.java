package yowyob.comops.api.payroll.domain.model;

/** Lifecycle of a wage-garnishment order. */
public enum GarnishmentStatus {
    ACTIVE,
    SUSPENDED,
    COMPLETED,
    CANCELLED
}
