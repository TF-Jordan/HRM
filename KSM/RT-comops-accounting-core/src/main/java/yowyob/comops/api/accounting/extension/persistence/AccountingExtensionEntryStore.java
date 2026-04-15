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
public class AccountingExtensionEntryStore {

    private static final TypeReference<List<AccountingBookkeepingViews.EntryLineView>> ENTRY_LINES_TYPE = new TypeReference<>() {
    };

    private final AccountingExtensionEntryRepository repository;
    private final ObjectMapper objectMapper;

    public AccountingExtensionEntryStore(AccountingExtensionEntryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Flux<AccountingBookkeepingService.Entry> loadAll() {
        return repository.findAll()
                .flatMap(entity -> Mono.fromCallable(() -> toDomain(entity)));
    }

    public Mono<Void> save(AccountingBookkeepingService.Entry entry) {
        return Mono.fromCallable(() -> toEntity(entry))
                .flatMap(repository::save)
                .then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionEntryEntity toEntity(AccountingBookkeepingService.Entry entry) {
        return new AccountingExtensionEntryEntity(
                entry.id(),
                entry.organizationId(),
                entry.journalId(),
                entry.reference(),
                entry.entryDate(),
                entry.status(),
                serializeLines(entry.lines()),
                entry.createdAt(),
                entry.validatedAt(),
                entry.cancelledAt(),
                entry.active());
    }

    private AccountingBookkeepingService.Entry toDomain(AccountingExtensionEntryEntity entity) {
        return new AccountingBookkeepingService.Entry(
                entity.id(),
                entity.organizationId(),
                entity.journalId(),
                entity.reference(),
                entity.entryDate(),
                entity.status(),
                deserializeLines(entity.linesJson()),
                entity.createdAt(),
                entity.validatedAt(),
                entity.cancelledAt(),
                entity.active());
    }

    private String serializeLines(List<AccountingBookkeepingViews.EntryLineView> lines) {
        try {
            return objectMapper.writeValueAsString(lines);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize accounting entry lines", exception);
        }
    }

    private List<AccountingBookkeepingViews.EntryLineView> deserializeLines(String linesJson) {
        try {
            return objectMapper.readValue(linesJson, ENTRY_LINES_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize accounting entry lines", exception);
        }
    }
}
