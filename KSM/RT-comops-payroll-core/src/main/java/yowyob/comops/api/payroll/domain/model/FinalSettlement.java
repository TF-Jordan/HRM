package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A persisted final settlement (solde de tout compte) for a departing employee — the stored
 * outcome of {@code FinalSettlementCalculator}, with its statutory component breakdown.
 */
public final class FinalSettlement extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final String periode;
    private final LocalDate departureDate;
    private final TerminationReason reason;
    private final String currency;
    private final int seniorityYears;
    private final BigDecimal proratedSalary;
    private final BigDecimal leaveCompensation;
    private final BigDecimal noticeIndemnity;
    private final BigDecimal severanceIndemnity;
    private final BigDecimal gratification;
    private final BigDecimal grossSettlement;
    private final BigDecimal loanDeducted;
    private final BigDecimal netSettlement;
    private final FinalSettlementStatus status;

    private FinalSettlement(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                            UUID organizationId, UUID employeeId, String periode, LocalDate departureDate,
                            TerminationReason reason, String currency, int seniorityYears,
                            BigDecimal proratedSalary, BigDecimal leaveCompensation,
                            BigDecimal noticeIndemnity, BigDecimal severanceIndemnity,
                            BigDecimal gratification, BigDecimal grossSettlement, BigDecimal loanDeducted,
                            BigDecimal netSettlement, FinalSettlementStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.periode = Objects.requireNonNull(periode, "periode is required");
        this.departureDate = Objects.requireNonNull(departureDate, "departureDate is required");
        this.reason = Objects.requireNonNull(reason, "reason is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.seniorityYears = seniorityYears;
        this.proratedSalary = proratedSalary;
        this.leaveCompensation = leaveCompensation;
        this.noticeIndemnity = noticeIndemnity;
        this.severanceIndemnity = severanceIndemnity;
        this.gratification = gratification;
        this.grossSettlement = grossSettlement;
        this.loanDeducted = loanDeducted;
        this.netSettlement = netSettlement;
    }

    public static FinalSettlement create(UUID tenantId, UUID organizationId, UUID employeeId,
                                         String periode, LocalDate departureDate, TerminationReason reason,
                                         String currency, int seniorityYears, BigDecimal proratedSalary,
                                         BigDecimal leaveCompensation, BigDecimal noticeIndemnity,
                                         BigDecimal severanceIndemnity, BigDecimal gratification,
                                         BigDecimal grossSettlement, BigDecimal loanDeducted,
                                         BigDecimal netSettlement) {
        Instant now = Instant.now();
        return new FinalSettlement(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                periode, departureDate, reason, currency, seniorityYears, proratedSalary, leaveCompensation,
                noticeIndemnity, severanceIndemnity, gratification, grossSettlement, loanDeducted,
                netSettlement, FinalSettlementStatus.CALCULATED);
    }

    public static FinalSettlement rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            UUID organizationId, UUID employeeId, String periode,
                                            LocalDate departureDate, TerminationReason reason,
                                            String currency, int seniorityYears, BigDecimal proratedSalary,
                                            BigDecimal leaveCompensation, BigDecimal noticeIndemnity,
                                            BigDecimal severanceIndemnity, BigDecimal gratification,
                                            BigDecimal grossSettlement, BigDecimal loanDeducted,
                                            BigDecimal netSettlement, FinalSettlementStatus status) {
        return new FinalSettlement(id, tenantId, createdAt, updatedAt, organizationId, employeeId, periode,
                departureDate, reason, currency, seniorityYears, proratedSalary, leaveCompensation,
                noticeIndemnity, severanceIndemnity, gratification, grossSettlement, loanDeducted,
                netSettlement, status);
    }

    public FinalSettlement markPaid() {
        if (status != FinalSettlementStatus.CALCULATED) {
            throw new IllegalStateException("Cannot mark final settlement paid in status " + status);
        }
        return new FinalSettlement(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                periode, departureDate, reason, currency, seniorityYears, proratedSalary, leaveCompensation,
                noticeIndemnity, severanceIndemnity, gratification, grossSettlement, loanDeducted,
                netSettlement, FinalSettlementStatus.PAID);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String periode() { return periode; }
    public LocalDate departureDate() { return departureDate; }
    public TerminationReason reason() { return reason; }
    public String currency() { return currency; }
    public int seniorityYears() { return seniorityYears; }
    public BigDecimal proratedSalary() { return proratedSalary; }
    public BigDecimal leaveCompensation() { return leaveCompensation; }
    public BigDecimal noticeIndemnity() { return noticeIndemnity; }
    public BigDecimal severanceIndemnity() { return severanceIndemnity; }
    public BigDecimal gratification() { return gratification; }
    public BigDecimal grossSettlement() { return grossSettlement; }
    public BigDecimal loanDeducted() { return loanDeducted; }
    public BigDecimal netSettlement() { return netSettlement; }
    public FinalSettlementStatus status() { return status; }
}
