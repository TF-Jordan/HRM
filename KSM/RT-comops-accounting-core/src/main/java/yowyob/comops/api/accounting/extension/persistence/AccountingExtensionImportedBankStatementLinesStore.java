package yowyob.comops.api.accounting.extension.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionImportedBankStatementLinesStore {

    private static final TypeReference<List<Map<String, Object>>> ROWS_TYPE = new TypeReference<>() {
    };

    private final AccountingExtensionImportedBankStatementLinesRepository repository;
    private final ObjectMapper objectMapper;

    public AccountingExtensionImportedBankStatementLinesStore(
            AccountingExtensionImportedBankStatementLinesRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Flux<AccountingBookkeepingService.ImportedBankStatementLines> loadAll() {
        return repository.findAll().flatMap(entity -> Mono.fromCallable(() -> toDomain(entity)));
    }

    public Mono<Void> save(AccountingBookkeepingService.ImportedBankStatementLines lines) {
        return Mono.fromCallable(() -> toEntity(lines))
                .flatMap(repository::save)
                .then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionImportedBankStatementLinesEntity toEntity(
            AccountingBookkeepingService.ImportedBankStatementLines lines) {
        return new AccountingExtensionImportedBankStatementLinesEntity(
                lines.id(),
                lines.organizationId(),
                serializeRows(lines.rows()));
    }

    private AccountingBookkeepingService.ImportedBankStatementLines toDomain(
            AccountingExtensionImportedBankStatementLinesEntity entity) {
        return new AccountingBookkeepingService.ImportedBankStatementLines(
                entity.id(),
                entity.organizationId(),
                deserializeRows(entity.rowsJson()));
    }

    private String serializeRows(List<Map<String, Object>> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize imported bank statement lines", exception);
        }
    }

    private List<Map<String, Object>> deserializeRows(String rowsJson) {
        try {
            return objectMapper.readValue(rowsJson, ROWS_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize imported bank statement lines", exception);
        }
    }
}
