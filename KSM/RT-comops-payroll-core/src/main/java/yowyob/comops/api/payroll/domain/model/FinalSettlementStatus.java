package yowyob.comops.api.payroll.domain.model;

/** Lifecycle of a final settlement: computed, then paid out. */
public enum FinalSettlementStatus {
    CALCULATED,
    PAID
}
