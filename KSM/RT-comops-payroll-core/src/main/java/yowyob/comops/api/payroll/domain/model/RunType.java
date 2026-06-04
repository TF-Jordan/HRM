package yowyob.comops.api.payroll.domain.model;

/** Nature of a payroll cycle. A period may carry one regular run plus additional runs. */
public enum RunType {
    /** The ordinary monthly run. At most one per (period, organization, agency). */
    REGULAR,
    /** An additional run within a period (corrections, late variables). */
    COMPLEMENTARY,
    /** Year-end 13th-month / end-of-year bonus run. */
    THIRTEENTH_MONTH,
    /** One-off exceptional bonus run. */
    EXCEPTIONAL_BONUS
}
