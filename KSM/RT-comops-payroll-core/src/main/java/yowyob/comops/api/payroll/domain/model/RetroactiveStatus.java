package yowyob.comops.api.payroll.domain.model;

/** Lifecycle of a retroactive adjustment: computed, then applied to a run (or cancelled). */
public enum RetroactiveStatus {
    PENDING,
    APPLIED,
    CANCELLED
}
