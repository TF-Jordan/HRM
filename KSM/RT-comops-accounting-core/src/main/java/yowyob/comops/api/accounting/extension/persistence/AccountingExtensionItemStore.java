package yowyob.comops.api.accounting.extension.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.springframework.r2dbc.BadSqlGrammarException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AccountingExtensionItemStore {

    private final AccountingExtensionItemRepository repository;
    private final ObjectMapper objectMapper;

    public AccountingExtensionItemStore(AccountingExtensionItemRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public <T> Flux<T> loadAll(String scope, String itemType, Class<T> type) {
        return repository.findAllByScopeAndItemType(scope, itemType)
                .flatMap(entity -> Mono.fromCallable(() -> objectMapper.readValue(entity.payload(), type)))
                .onErrorResume(BadSqlGrammarException.class, exception -> Flux.empty());
    }

    public <T> Mono<Void> save(String scope,
            String itemType,
            UUID itemId,
            UUID organizationId,
            T payload) {
        return Mono.fromCallable(() -> serialize(payload))
                .flatMap(json -> repository.save(new AccountingExtensionItemEntity(
                        itemKey(scope, itemType, itemId),
                        scope,
                        itemType,
                        itemId,
                        organizationId,
                        json,
                        Instant.now())))
                .then();
    }

    public Mono<Void> delete(String scope, String itemType, UUID itemId) {
        return repository.deleteByScopeAndItemTypeAndItemId(scope, itemType, itemId);
    }

    public <T> Mono<Void> replaceAll(String scope,
            String itemType,
            List<T> items,
            Function<T, UUID> idExtractor,
            Function<T, UUID> organizationIdExtractor) {
        return repository.deleteByScopeAndItemType(scope, itemType)
                .thenMany(Flux.fromIterable(items)
                        .flatMap(item -> save(
                                scope,
                                itemType,
                                idExtractor.apply(item),
                                organizationIdExtractor.apply(item),
                                item)))
                .then();
    }

    private String itemKey(String scope, String itemType, UUID itemId) {
        return scope + ":" + itemType + ":" + itemId;
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize accounting extension item", exception);
        }
    }
}
