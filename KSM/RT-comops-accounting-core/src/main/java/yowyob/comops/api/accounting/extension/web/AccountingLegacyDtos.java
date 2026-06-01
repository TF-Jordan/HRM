package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class AccountingLegacyDtos {

    private AccountingLegacyDtos() {
    }

    public enum BrouillardStatut {
        BROUILLON,
        EN_ATTENTE_VALIDATION,
        VALIDE,
        REJETE
    }

    public enum BrouillardType {
        FACTURE_CLIENT,
        FACTURE_FOURNISSEUR,
        MOUVEMENT_STOCK,
        MOUVEMENT_CAISSE,
        OPERATION_BANCAIRE,
        AUTRE
    }

    public record BrouillardComptableDto(
            UUID id,
            BrouillardType type,
            BrouillardStatut statut,
            String sourceId,
            String sourceType,
            String numeroPiece,
            LocalDate datePiece,
            String libelle,
            BigDecimal montantTotal,
            String devise,
            UUID journalId,
            String journalCode,
            String journalLibelle,
            UUID periodeId,
            String periodeCode,
            UUID ecritureId,
            List<UUID> attachmentIds,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdBy,
            String validatedBy,
            LocalDateTime validatedAt,
            String rejectedBy,
            LocalDateTime rejectedAt,
            String rejectionReason) {
    }

    public record BrouillardValidationRequest(
            String notes,
            Boolean forceValidation) {
    }

    public record BrouillardRejectionRequest(
            String reason) {
    }

    public record ReportItemDto(
            String code,
            String description,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal solde) {
    }

    public record BilanDto(
            List<ReportItemDto> actifs,
            List<ReportItemDto> passifs,
            List<ReportItemDto> capitauxPropres) {
    }

    public record CompteResultatDto(
            List<ReportItemDto> produits,
            List<ReportItemDto> charges) {
    }

    public record CashFlowItemDto(
            String code,
            String description,
            BigDecimal amount,
            String category) {
    }

    public record CashFlowDto(
            List<CashFlowItemDto> operationnel,
            List<CashFlowItemDto> investissement,
            List<CashFlowItemDto> financement) {
    }

    public record ExecutiveSummaryItemDto(
            String section,
            BigDecimal total,
            String description) {
    }

    public record ExecutiveSummaryDto(
            List<ExecutiveSummaryItemDto> bilan,
            List<ExecutiveSummaryItemDto> compteResultat,
            List<ExecutiveSummaryItemDto> fluxTresorerie) {
    }

    public record LigneGrandLivreDto(
            UUID ecritureId,
            LocalDateTime date,
            String journal,
            String reference,
            String libelle,
            BigDecimal debit,
            BigDecimal credit) {
    }

    public record GrandLivreDto(
            String noCompte,
            String libelleCompte,
            BigDecimal soldeOuverture,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            BigDecimal soldeCloture,
            List<LigneGrandLivreDto> lignes) {
    }

    public record LigneBalanceDto(
            String noCompte,
            String libelle,
            BigDecimal soldeOuvertureDebit,
            BigDecimal soldeOuvertureCredit,
            BigDecimal mouvementDebit,
            BigDecimal mouvementCredit,
            BigDecimal soldeClotureDebit,
            BigDecimal soldeClotureCredit) {
    }

    public record BalanceDesComptesDto(
            BigDecimal totalDebitOuverture,
            BigDecimal totalCreditOuverture,
            BigDecimal totalDebitMouvement,
            BigDecimal totalCreditMouvement,
            BigDecimal totalDebitCloture,
            BigDecimal totalCreditCloture,
            List<LigneBalanceDto> lignes) {
    }
}
