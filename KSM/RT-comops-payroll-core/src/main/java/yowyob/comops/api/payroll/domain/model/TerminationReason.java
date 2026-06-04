package yowyob.comops.api.payroll.domain.model;

/**
 * Reason an employment relationship ends, which drives the components of the final settlement
 * (solde de tout compte) — notably whether severance and notice indemnities are due.
 */
public enum TerminationReason {
    /** Employee resigns — no severance, no employer notice indemnity. */
    RESIGNATION,
    /** Employer dismissal for a real and serious cause (not misconduct) — severance + notice. */
    DISMISSAL,
    /** Dismissal for gross misconduct (faute lourde) — no severance, no notice. */
    DISMISSAL_GROSS_MISCONDUCT,
    /** Fixed-term contract reaching its term — no severance (end-of-contract indemnity instead). */
    END_OF_CONTRACT,
    /** Retirement — retirement allowance treated like severance. */
    RETIREMENT,
    /** Negotiated departure — severance by default. */
    MUTUAL_AGREEMENT,
    /** Death in service — settlement paid to the estate; severance due. */
    DEATH
}
