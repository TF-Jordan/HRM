package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class LoanAdvance extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID employeeId;
    private final BigDecimal montant;
    private final BigDecimal soldeRestant;
    private final BigDecimal mensualite;
    private final LoanAdvanceStatus status;
    private final LocalDate dateDebut;
    private final int nbEcheances;
    private final String motif;
    private final UUID approvedBy;

    private LoanAdvance(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        UUID organizationId, UUID agencyId, UUID employeeId,
                        BigDecimal montant, BigDecimal soldeRestant, BigDecimal mensualite,
                        LoanAdvanceStatus status, LocalDate dateDebut, int nbEcheances,
                        String motif, UUID approvedBy) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.montant = Objects.requireNonNull(montant, "montant is required");
        this.soldeRestant = Objects.requireNonNull(soldeRestant, "soldeRestant is required");
        this.mensualite = Objects.requireNonNull(mensualite, "mensualite is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut is required");
        this.nbEcheances = nbEcheances;
        this.agencyId = agencyId;
        this.motif = motif;
        this.approvedBy = approvedBy;
    }

    public static LoanAdvance request(UUID tenantId, UUID organizationId, UUID agencyId,
                                       UUID employeeId, BigDecimal montant, int nbEcheances,
                                       String motif) {
        Instant now = Instant.now();
        BigDecimal mensualite = montant.divide(BigDecimal.valueOf(nbEcheances), 2, java.math.RoundingMode.HALF_UP);
        return new LoanAdvance(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                employeeId, montant, montant, mensualite, LoanAdvanceStatus.PENDING,
                LocalDate.now(), nbEcheances, motif, null);
    }

    public static LoanAdvance rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID organizationId, UUID agencyId, UUID employeeId,
                                         BigDecimal montant, BigDecimal soldeRestant, BigDecimal mensualite,
                                         LoanAdvanceStatus status, LocalDate dateDebut, int nbEcheances,
                                         String motif, UUID approvedBy) {
        return new LoanAdvance(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                employeeId, montant, soldeRestant, mensualite, status, dateDebut, nbEcheances,
                motif, approvedBy);
    }

    public LoanAdvance approve(UUID approverId) {
        if (this.status != LoanAdvanceStatus.PENDING) {
            throw new IllegalStateException("Cannot approve loan advance in status " + this.status);
        }
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, montant, soldeRestant, mensualite, LoanAdvanceStatus.IN_REPAYMENT,
                dateDebut, nbEcheances, motif, approverId);
    }

    public LoanAdvance reject(UUID approverId, String motifRejet) {
        if (this.status != LoanAdvanceStatus.PENDING) {
            throw new IllegalStateException("Cannot reject loan advance in status " + this.status);
        }
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, montant, soldeRestant, mensualite, LoanAdvanceStatus.REJECTED,
                dateDebut, nbEcheances, motifRejet, approverId);
    }

    public LoanAdvance deduire(BigDecimal deduction) {
        if (this.status != LoanAdvanceStatus.IN_REPAYMENT) {
            throw new IllegalStateException("Cannot deduct from loan advance in status " + this.status);
        }
        BigDecimal actualDeduction = deduction.min(soldeRestant);
        BigDecimal newSolde = soldeRestant.subtract(actualDeduction);
        LoanAdvanceStatus newStatus = newSolde.compareTo(BigDecimal.ZERO) <= 0
                ? LoanAdvanceStatus.FULLY_REPAID : LoanAdvanceStatus.IN_REPAYMENT;
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, montant, newSolde, mensualite, newStatus, dateDebut, nbEcheances,
                motif, approvedBy);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID employeeId() { return employeeId; }
    public BigDecimal montant() { return montant; }
    public BigDecimal soldeRestant() { return soldeRestant; }
    public BigDecimal mensualite() { return mensualite; }
    public LoanAdvanceStatus status() { return status; }
    public LocalDate dateDebut() { return dateDebut; }
    public int nbEcheances() { return nbEcheances; }
    public String motif() { return motif; }
    public UUID approvedBy() { return approvedBy; }
}
