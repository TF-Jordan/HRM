package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The per-employee result of a payroll run: the headline figures plus the payment
 * coordinates. The full breakdown lives in the associated {@link PayslipLine}s.
 *
 * Totals are kept generic (gross / deductions / income tax / employer charges / net) so the
 * shape is country-agnostic — the legacy CNPS/CAC/CFC columns are now ordinary payslip lines.
 */
public final class PayrollEntry extends BaseEntity {

    private final UUID organizationId;
    private final UUID payrollRunId;
    private final UUID employeeId;
    private final String currency;

    private final BigDecimal salaireBase;
    private final BigDecimal brut;
    private final BigDecimal totalDeductions;
    private final BigDecimal incomeTax;
    private final BigDecimal employerCharges;
    private final BigDecimal net;

    private final PaymentStatus paymentStatus;
    private final PaymentChannel paymentChannel;
    private final String accountRef;

    private PayrollEntry(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID organizationId, UUID payrollRunId, UUID employeeId, String currency,
                         BigDecimal salaireBase, BigDecimal brut, BigDecimal totalDeductions,
                         BigDecimal incomeTax, BigDecimal employerCharges, BigDecimal net,
                         PaymentStatus paymentStatus, PaymentChannel paymentChannel, String accountRef) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.payrollRunId = Objects.requireNonNull(payrollRunId, "payrollRunId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.salaireBase = salaireBase;
        this.brut = brut;
        this.totalDeductions = totalDeductions;
        this.incomeTax = incomeTax;
        this.employerCharges = employerCharges;
        this.net = net;
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus is required");
        this.paymentChannel = Objects.requireNonNull(paymentChannel, "paymentChannel is required");
        this.accountRef = accountRef;
    }

    public static PayrollEntry create(UUID tenantId, UUID organizationId, UUID payrollRunId,
                                      UUID employeeId, String currency, BigDecimal salaireBase,
                                      BigDecimal brut, BigDecimal totalDeductions, BigDecimal incomeTax,
                                      BigDecimal employerCharges, BigDecimal net,
                                      PaymentChannel paymentChannel, String accountRef) {
        Instant now = Instant.now();
        return new PayrollEntry(UUID.randomUUID(), tenantId, now, now, organizationId, payrollRunId,
                employeeId, currency, salaireBase, brut, totalDeductions, incomeTax, employerCharges,
                net, PaymentStatus.PENDING, paymentChannel, accountRef);
    }

    public static PayrollEntry rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID organizationId, UUID payrollRunId, UUID employeeId,
                                         String currency, BigDecimal salaireBase, BigDecimal brut,
                                         BigDecimal totalDeductions, BigDecimal incomeTax,
                                         BigDecimal employerCharges, BigDecimal net,
                                         PaymentStatus paymentStatus, PaymentChannel paymentChannel,
                                         String accountRef) {
        return new PayrollEntry(id, tenantId, createdAt, updatedAt, organizationId, payrollRunId,
                employeeId, currency, salaireBase, brut, totalDeductions, incomeTax, employerCharges,
                net, paymentStatus, paymentChannel, accountRef);
    }

    public PayrollEntry withPaymentStatus(PaymentStatus newStatus) {
        return new PayrollEntry(id(), tenantId(), createdAt(), Instant.now(), organizationId, payrollRunId,
                employeeId, currency, salaireBase, brut, totalDeductions, incomeTax, employerCharges,
                net, newStatus, paymentChannel, accountRef);
    }

    public UUID organizationId() { return organizationId; }
    public UUID payrollRunId() { return payrollRunId; }
    public UUID employeeId() { return employeeId; }
    public String currency() { return currency; }
    public BigDecimal salaireBase() { return salaireBase; }
    public BigDecimal brut() { return brut; }
    public BigDecimal totalDeductions() { return totalDeductions; }
    public BigDecimal incomeTax() { return incomeTax; }
    public BigDecimal employerCharges() { return employerCharges; }
    public BigDecimal net() { return net; }
    public PaymentStatus paymentStatus() { return paymentStatus; }
    public PaymentChannel paymentChannel() { return paymentChannel; }
    public String accountRef() { return accountRef; }
}
