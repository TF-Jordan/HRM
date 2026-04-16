package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PayrollEntry extends BaseEntity {

    private final UUID organizationId;
    private final UUID payrollRunId;
    private final UUID employeeId;
    private final BigDecimal salaireBase;
    private final BigDecimal brut;
    private final BigDecimal net;
    private final BigDecimal cnpsEmploye;
    private final BigDecimal cnpsEmployeur;
    private final BigDecimal irpp;
    private final BigDecimal cac;
    private final BigDecimal primes;
    private final BigDecimal retenues;
    private final BigDecimal avancesDeduites;
    private final PaymentStatus paymentStatus;
    private final String paymentChannel;
    private final String accountRef;

    private PayrollEntry(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID organizationId, UUID payrollRunId, UUID employeeId,
                         BigDecimal salaireBase, BigDecimal brut, BigDecimal net,
                         BigDecimal cnpsEmploye, BigDecimal cnpsEmployeur, BigDecimal irpp,
                         BigDecimal cac, BigDecimal primes, BigDecimal retenues,
                         BigDecimal avancesDeduites, PaymentStatus paymentStatus,
                         String paymentChannel, String accountRef) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.payrollRunId = Objects.requireNonNull(payrollRunId);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.salaireBase = salaireBase;
        this.brut = brut;
        this.net = net;
        this.cnpsEmploye = cnpsEmploye;
        this.cnpsEmployeur = cnpsEmployeur;
        this.irpp = irpp;
        this.cac = cac;
        this.primes = primes;
        this.retenues = retenues;
        this.avancesDeduites = avancesDeduites;
        this.paymentStatus = paymentStatus;
        this.paymentChannel = paymentChannel;
        this.accountRef = accountRef;
    }

    public static PayrollEntry create(UUID tenantId, UUID organizationId, UUID payrollRunId,
                                       UUID employeeId, BigDecimal salaireBase, BigDecimal brut,
                                       BigDecimal net, BigDecimal cnpsEmploye, BigDecimal cnpsEmployeur,
                                       BigDecimal irpp, BigDecimal cac, BigDecimal primes,
                                       BigDecimal retenues, BigDecimal avancesDeduites,
                                       String paymentChannel, String accountRef) {
        Instant now = Instant.now();
        return new PayrollEntry(UUID.randomUUID(), tenantId, now, now, organizationId, payrollRunId,
                employeeId, salaireBase, brut, net, cnpsEmploye, cnpsEmployeur, irpp, cac,
                primes, retenues, avancesDeduites, PaymentStatus.PENDING, paymentChannel, accountRef);
    }

    public static PayrollEntry rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID organizationId, UUID payrollRunId, UUID employeeId,
                                          BigDecimal salaireBase, BigDecimal brut, BigDecimal net,
                                          BigDecimal cnpsEmploye, BigDecimal cnpsEmployeur,
                                          BigDecimal irpp, BigDecimal cac, BigDecimal primes,
                                          BigDecimal retenues, BigDecimal avancesDeduites,
                                          PaymentStatus paymentStatus, String paymentChannel,
                                          String accountRef) {
        return new PayrollEntry(id, tenantId, createdAt, updatedAt, organizationId, payrollRunId,
                employeeId, salaireBase, brut, net, cnpsEmploye, cnpsEmployeur, irpp, cac,
                primes, retenues, avancesDeduites, paymentStatus, paymentChannel, accountRef);
    }

    public PayrollEntry updatePaymentStatus(PaymentStatus newStatus) {
        return new PayrollEntry(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                payrollRunId, employeeId, salaireBase, brut, net, cnpsEmploye, cnpsEmployeur,
                irpp, cac, primes, retenues, avancesDeduites, newStatus, paymentChannel, accountRef);
    }

    public UUID organizationId() { return organizationId; }
    public UUID payrollRunId() { return payrollRunId; }
    public UUID employeeId() { return employeeId; }
    public BigDecimal salaireBase() { return salaireBase; }
    public BigDecimal brut() { return brut; }
    public BigDecimal net() { return net; }
    public BigDecimal cnpsEmploye() { return cnpsEmploye; }
    public BigDecimal cnpsEmployeur() { return cnpsEmployeur; }
    public BigDecimal irpp() { return irpp; }
    public BigDecimal cac() { return cac; }
    public BigDecimal primes() { return primes; }
    public BigDecimal retenues() { return retenues; }
    public BigDecimal avancesDeduites() { return avancesDeduites; }
    public PaymentStatus paymentStatus() { return paymentStatus; }
    public String paymentChannel() { return paymentChannel; }
    public String accountRef() { return accountRef; }
}
