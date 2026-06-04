package yowyob.comops.api.payroll.domain.model;

/**
 * Kind of wage garnishment, in legal priority order (lowest ordinal = highest priority).
 *
 * <ul>
 *   <li>{@link #ALIMONY} — child support / spousal maintenance; paid first and may reach the
 *       fraction otherwise protected from ordinary seizure.</li>
 *   <li>{@link #TAX_LEVY} — Treasury seizure for unpaid taxes.</li>
 *   <li>{@link #CREDITOR} — ordinary creditor attachment, limited to the seizable quota.</li>
 * </ul>
 */
public enum GarnishmentType {
    ALIMONY,
    TAX_LEVY,
    CREDITOR
}
