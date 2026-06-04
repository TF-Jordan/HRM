package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Year-to-date accumulator per employee, required for the legal payslip (cumuls annuels),
 * the annual DIPE declaration, and end-of-year income-tax regularisation.
 *
 * One row per (tenant, organization, employee, year). Each calculated run folds its figures
 * into the matching accumulator via {@link #accumulate}.
 */
public final class AnnualAccumulator extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final int year;
    private final BigDecimal cumulativeGross;
    private final BigDecimal cumulativeDeductions;
    private final BigDecimal cumulativeIncomeTax;
    private final BigDecimal cumulativeNet;
    private final BigDecimal cumulativeEmployerCharges;

    private AnnualAccumulator(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                              UUID organizationId, UUID employeeId, int year,
                              BigDecimal cumulativeGross, BigDecimal cumulativeDeductions,
                              BigDecimal cumulativeIncomeTax, BigDecimal cumulativeNet,
                              BigDecimal cumulativeEmployerCharges) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.year = year;
        this.cumulativeGross = nullToZero(cumulativeGross);
        this.cumulativeDeductions = nullToZero(cumulativeDeductions);
        this.cumulativeIncomeTax = nullToZero(cumulativeIncomeTax);
        this.cumulativeNet = nullToZero(cumulativeNet);
        this.cumulativeEmployerCharges = nullToZero(cumulativeEmployerCharges);
    }

    /** Starts an empty accumulator for the given employee-year. */
    public static AnnualAccumulator start(UUID tenantId, UUID organizationId, UUID employeeId, int year) {
        Instant now = Instant.now();
        return new AnnualAccumulator(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                year, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static AnnualAccumulator rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                              UUID organizationId, UUID employeeId, int year,
                                              BigDecimal cumulativeGross, BigDecimal cumulativeDeductions,
                                              BigDecimal cumulativeIncomeTax, BigDecimal cumulativeNet,
                                              BigDecimal cumulativeEmployerCharges) {
        return new AnnualAccumulator(id, tenantId, createdAt, updatedAt, organizationId, employeeId, year,
                cumulativeGross, cumulativeDeductions, cumulativeIncomeTax, cumulativeNet,
                cumulativeEmployerCharges);
    }

    /** Folds one entry's figures into the accumulator, returning a new instance. */
    public AnnualAccumulator accumulate(BigDecimal gross, BigDecimal deductions, BigDecimal incomeTax,
                                        BigDecimal net, BigDecimal employerCharges) {
        return new AnnualAccumulator(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, year,
                cumulativeGross.add(nullToZero(gross)),
                cumulativeDeductions.add(nullToZero(deductions)),
                cumulativeIncomeTax.add(nullToZero(incomeTax)),
                cumulativeNet.add(nullToZero(net)),
                cumulativeEmployerCharges.add(nullToZero(employerCharges)));
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public int year() { return year; }
    public BigDecimal cumulativeGross() { return cumulativeGross; }
    public BigDecimal cumulativeDeductions() { return cumulativeDeductions; }
    public BigDecimal cumulativeIncomeTax() { return cumulativeIncomeTax; }
    public BigDecimal cumulativeNet() { return cumulativeNet; }
    public BigDecimal cumulativeEmployerCharges() { return cumulativeEmployerCharges; }
}
