package yowyob.comops.api.accounting.extension.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;

@Component
public class AccountingExtensionDraftEntryStore {

    private static final TypeReference<List<AccountingBookkeepingViews.EntryLineView>> ENTRY_LINES_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<UUID>> UUID_LIST_TYPE = new TypeReference<>() {
    };

    private final AccountingExtensionDraftEntryRepository repository;
    private final ObjectMapper objectMapper;

    public AccountingExtensionDraftEntryStore(AccountingExtensionDraftEntryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Flux<AccountingBookkeepingService.DraftEntry> loadAll() {
        return repository.findAll().flatMap(entity -> Mono.fromCallable(() -> toDomain(entity)));
    }

    public Mono<Void> save(AccountingBookkeepingService.DraftEntry draftEntry) {
        return Mono.fromCallable(() -> toEntity(draftEntry))
                .flatMap(repository::save)
                .then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionDraftEntryEntity toEntity(AccountingBookkeepingService.DraftEntry draftEntry) {
        return new AccountingExtensionDraftEntryEntity(
                draftEntry.id(),
                draftEntry.organizationId(),
                draftEntry.journalId(),
                draftEntry.periodId(),
                draftEntry.reference(),
                draftEntry.entryDate(),
                serializeLines(draftEntry.lines()),
                draftEntry.legacyType(),
                draftEntry.legacyStatus(),
                draftEntry.sourceId(),
                draftEntry.sourceType(),
                draftEntry.pieceNumber(),
                draftEntry.label(),
                draftEntry.totalAmount(),
                draftEntry.currency(),
                draftEntry.notes(),
                serializeAttachmentIds(draftEntry.attachmentIds()),
                draftEntry.createdBy(),
                draftEntry.validatedBy(),
                draftEntry.validatedAt(),
                draftEntry.rejectedBy(),
                draftEntry.rejectedAt(),
                draftEntry.rejectionReason(),
                draftEntry.createdAt(),
                draftEntry.postedAt(),
                draftEntry.entryId());
    }

    private AccountingBookkeepingService.DraftEntry toDomain(AccountingExtensionDraftEntryEntity entity) {
        return new AccountingBookkeepingService.DraftEntry(
                entity.id(),
                entity.organizationId(),
                entity.journalId(),
                entity.periodId(),
                entity.reference(),
                entity.entryDate(),
                deserializeLines(entity.linesJson()),
                entity.legacyType(),
                entity.legacyStatus(),
                entity.sourceId(),
                entity.sourceType(),
                entity.pieceNumber(),
                entity.label(),
                entity.totalAmount(),
                entity.currency(),
                entity.notes(),
                deserializeAttachmentIds(entity.attachmentIdsJson()),
                entity.createdBy(),
                entity.validatedBy(),
                entity.validatedAt(),
                entity.rejectedBy(),
                entity.rejectedAt(),
                entity.rejectionReason(),
                entity.createdAt(),
                entity.postedAt(),
                entity.entryId());
    }

    private String serializeLines(List<AccountingBookkeepingViews.EntryLineView> lines) {
        try {
            return objectMapper.writeValueAsString(lines);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize draft entry lines", exception);
        }
    }

    private List<AccountingBookkeepingViews.EntryLineView> deserializeLines(String linesJson) {
        try {
            return objectMapper.readValue(linesJson, ENTRY_LINES_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize draft entry lines", exception);
        }
    }

    private String serializeAttachmentIds(List<UUID> attachmentIds) {
        try {
            return objectMapper.writeValueAsString(attachmentIds == null ? List.of() : attachmentIds);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize draft entry attachment ids", exception);
        }
    }

    private List<UUID> deserializeAttachmentIds(String attachmentIdsJson) {
        if (attachmentIdsJson == null || attachmentIdsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(attachmentIdsJson, UUID_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize draft entry attachment ids", exception);
        }
    }
}
