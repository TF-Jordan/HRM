package yowyob.comops.api.accounting.extension.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;
import yowyob.comops.api.accounting.extension.web.AccountingLegacyDtos;
import yowyob.comops.api.accounting.extension.web.AccountingOperationsViews;

@Service
public class AccountingLegacyBrouillardService {

    private final AccountingBookkeepingService bookkeepingService;
    private final AccountingOperationsService operationsService;

    public AccountingLegacyBrouillardService(AccountingBookkeepingService bookkeepingService,
            AccountingOperationsService operationsService) {
        this.bookkeepingService = bookkeepingService;
        this.operationsService = operationsService;
    }

    public Mono<List<AccountingLegacyDtos.BrouillardComptableDto>> listBrouillards(
            AccountingLegacyDtos.BrouillardStatut statut,
            AccountingLegacyDtos.BrouillardType type,
            int page,
            int size,
            AccountingExtensionRequestContext context) {
        return Mono.zip(
                bookkeepingService.listLegacyDraftEntries(context).collectList(),
                bookkeepingService.listJournals(context).collectList(),
                operationsService.listPeriods(context).collectList())
                .map(tuple -> tuple.getT1().stream()
                        .filter(draft -> statut == null || draft.legacyStatus().equalsIgnoreCase(statut.name()))
                        .filter(draft -> type == null || draft.legacyType().equalsIgnoreCase(type.name()))
                        .skip((long) page * size)
                        .limit(size)
                        .map(draft -> toLegacyDto(draft, tuple.getT2(), tuple.getT3()))
                        .toList());
    }

    public Mono<AccountingLegacyDtos.BrouillardComptableDto> getBrouillard(UUID draftEntryId,
            AccountingExtensionRequestContext context) {
        return Mono.zip(
                bookkeepingService.getLegacyDraftEntry(draftEntryId, context),
                bookkeepingService.listJournals(context).collectList(),
                operationsService.listPeriods(context).collectList())
                .map(tuple -> toLegacyDto(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public Mono<AccountingLegacyDtos.BrouillardComptableDto> uploadDraft(FilePart file,
            AccountingExtensionRequestContext context) {
        return Mono.zip(
                ensureDefaultJournal(context),
                operationsService.createAttachmentFromFile("BROUILLARD", UUID.randomUUID(), file, context))
                .flatMap(tuple -> bookkeepingService.createLegacyDraft(
                        new AccountingBookkeepingService.CreateLegacyDraftRequest(
                                tuple.getT1().id(),
                                null,
                                file.filename(),
                                Instant.now(),
                                List.of(),
                                inferDraftType(file.filename()).name(),
                                AccountingLegacyDtos.BrouillardStatut.EN_ATTENTE_VALIDATION.name(),
                                UUID.randomUUID().toString(),
                                "OCR_UPLOAD",
                                file.filename(),
                                file.filename(),
                                BigDecimal.ZERO,
                                "XAF",
                                null,
                                List.of(tuple.getT2().id()),
                                null),
                        context))
                .flatMap(draft -> getBrouillard(draft.id(), context));
    }

    public Mono<AccountingLegacyDtos.BrouillardComptableDto> validateBrouillard(UUID draftEntryId,
            AccountingLegacyDtos.BrouillardValidationRequest request,
            AccountingExtensionRequestContext context) {
        return bookkeepingService.validateLegacyDraftEntry(draftEntryId,
                actorLabel(context),
                request == null ? null : request.notes(),
                context)
                .flatMap(draft -> getBrouillard(draft.id(), context));
    }

    public Mono<AccountingLegacyDtos.BrouillardComptableDto> rejectBrouillard(UUID draftEntryId,
            AccountingLegacyDtos.BrouillardRejectionRequest request,
            AccountingExtensionRequestContext context) {
        return bookkeepingService.rejectLegacyDraftEntry(draftEntryId,
                actorLabel(context),
                request == null ? null : request.reason(),
                context)
                .flatMap(draft -> getBrouillard(draft.id(), context));
    }

    public Mono<Void> deleteBrouillard(UUID draftEntryId, AccountingExtensionRequestContext context) {
        return bookkeepingService.deleteDraftEntry(draftEntryId, context);
    }

    private Mono<AccountingBookkeepingViews.JournalView> ensureDefaultJournal(AccountingExtensionRequestContext context) {
        return bookkeepingService.listJournals(context)
                .next()
                .switchIfEmpty(bookkeepingService.createJournal(
                        new AccountingBookkeepingRequests.CreateJournalRequest("GEN", "Journal General", "GENERAL"),
                        context));
    }

    private AccountingLegacyDtos.BrouillardComptableDto toLegacyDto(AccountingBookkeepingService.DraftEntry draft,
            List<AccountingBookkeepingViews.JournalView> journals,
            List<AccountingOperationsViews.AccountingPeriodView> periods) {
        AccountingBookkeepingViews.JournalView journal = journals.stream()
                .filter(candidate -> candidate.id().equals(draft.journalId()))
                .findFirst()
                .orElse(null);
        AccountingOperationsViews.AccountingPeriodView period = periods.stream()
                .filter(candidate -> draft.periodId() != null && candidate.id().equals(draft.periodId()))
                .findFirst()
                .orElse(null);
        return new AccountingLegacyDtos.BrouillardComptableDto(
                draft.id(),
                AccountingLegacyDtos.BrouillardType.valueOf(draft.legacyType()),
                AccountingLegacyDtos.BrouillardStatut.valueOf(draft.legacyStatus()),
                draft.sourceId(),
                draft.sourceType(),
                draft.pieceNumber(),
                toLocalDate(draft.entryDate()),
                draft.label(),
                draft.totalAmount(),
                draft.currency(),
                draft.journalId(),
                journal == null ? null : journal.code(),
                journal == null ? null : journal.label(),
                draft.periodId(),
                period == null ? null : period.code(),
                draft.entryId(),
                draft.attachmentIds(),
                draft.notes(),
                toLocalDateTime(draft.createdAt()),
                toLocalDateTime(draft.postedAt() == null ? draft.createdAt() : draft.postedAt()),
                draft.createdBy(),
                draft.validatedBy(),
                toLocalDateTime(draft.validatedAt()),
                draft.rejectedBy(),
                toLocalDateTime(draft.rejectedAt()),
                draft.rejectionReason());
    }

    private AccountingLegacyDtos.BrouillardType inferDraftType(String filename) {
        String normalized = filename == null ? "" : filename.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("supplier") || normalized.contains("fournisseur") || normalized.contains("achat")) {
            return AccountingLegacyDtos.BrouillardType.FACTURE_FOURNISSEUR;
        }
        if (normalized.contains("client") || normalized.contains("sale") || normalized.contains("vente")) {
            return AccountingLegacyDtos.BrouillardType.FACTURE_CLIENT;
        }
        return AccountingLegacyDtos.BrouillardType.AUTRE;
    }

    private String actorLabel(AccountingExtensionRequestContext context) {
        if (context.actorId() != null) {
            return context.actorId().toString();
        }
        if (context.userId() != null) {
            return context.userId().toString();
        }
        return "system";
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
