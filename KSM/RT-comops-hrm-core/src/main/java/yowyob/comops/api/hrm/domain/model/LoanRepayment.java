package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One real repayment line: an amount actually withheld against a loan during a payroll run.
 *
 * <p>Unlike the projected amortization schedule (which the UI can compute from
 * {@code montant / nbEcheances}), this is an immutable historical fact recorded by the payroll
 * engine each time {@code LoanAdvance.deduire(...)} is applied — carrying the run that produced
 * it, the period label, and the resulting outstanding balance ({@code soldeApres}).
 */
public final class LoanRepayment extends BaseEntity {

    private final UUID organizationId;
    private final UUID loanId;
    private final UUID employeeId;
    private final UUID runId;
    private final String period;
    private final UUID payrollEntryId;
    private final BigDecimal montant;
    private final BigDecimal soldeApres;

    private LoanRepayment(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                          UUID organizationId, UUID loanId, UUID employeeId, UUID runId,
                          String period, UUID payrollEntryId, BigDecimal montant, BigDecimal soldeApres) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.loanId = Objects.requireNonNull(loanId, "loanId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.montant = Objects.requireNonNull(montant, "montant is required");
        this.soldeApres = Objects.requireNonNull(soldeApres, "soldeApres is required");
        this.runId = runId;
        this.period = period;
        this.payrollEntryId = payrollEntryId;
    }

    public static LoanRepayment record(UUID tenantId, UUID organizationId, UUID loanId, UUID employeeId,
                                       UUID runId, String period, UUID payrollEntryId,
                                       BigDecimal montant, BigDecimal soldeApres) {
        Instant now = Instant.now();
        return new LoanRepayment(UUID.randomUUID(), tenantId, now, now, organizationId, loanId,
                employeeId, runId, period, payrollEntryId, montant, soldeApres);
    }

    public static LoanRepayment rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID organizationId, UUID loanId, UUID employeeId, UUID runId,
                                          String period, UUID payrollEntryId, BigDecimal montant,
                                          BigDecimal soldeApres) {
        return new LoanRepayment(id, tenantId, createdAt, updatedAt, organizationId, loanId, employeeId,
                runId, period, payrollEntryId, montant, soldeApres);
    }

    public UUID organizationId() { return organizationId; }
    public UUID loanId() { return loanId; }
    public UUID employeeId() { return employeeId; }
    public UUID runId() { return runId; }
    public String period() { return period; }
    public UUID payrollEntryId() { return payrollEntryId; }
    public BigDecimal montant() { return montant; }
    public BigDecimal soldeApres() { return soldeApres; }
}
