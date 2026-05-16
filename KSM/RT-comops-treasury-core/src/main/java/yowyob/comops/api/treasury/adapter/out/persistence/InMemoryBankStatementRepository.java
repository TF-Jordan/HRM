package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.BankStatementRepository;
import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryBankStatementRepository implements BankStatementRepository {

    private final Map<UUID, BankStatement> bankStatements = new ConcurrentHashMap<>();

    @Override
    public Mono<BankStatement> findById(UUID statementId) {
        return Mono.justOrEmpty(bankStatements.get(statementId));
    }

    @Override
    public Flux<BankStatement> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(bankStatements.values().stream()
                .filter(statement -> statement.tenantId().equals(tenantId))
                .filter(statement -> statement.organizationId().equals(organizationId))
                .sorted((left, right) -> left.statementDate().compareTo(right.statementDate())));
    }

    @Override
    public Flux<BankStatement> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return Flux.fromStream(bankStatements.values().stream()
                .filter(statement -> statement.tenantId().equals(tenantId))
                .filter(statement -> statement.bankAccountId().equals(bankAccountId))
                .sorted((left, right) -> left.statementDate().compareTo(right.statementDate())));
    }

    @Override
    public Mono<BankStatement> findLatestByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return Flux.fromStream(bankStatements.values().stream()
                .filter(statement -> statement.tenantId().equals(tenantId))
                .filter(statement -> statement.bankAccountId().equals(bankAccountId))
                .sorted((left, right) -> right.statementDate().compareTo(left.statementDate())))
                .next();
    }

    @Override
    public Mono<BankStatement> save(BankStatement bankStatement) {
        return Mono.fromSupplier(() -> {
            bankStatements.put(bankStatement.id(), bankStatement);
            return bankStatement;
        });
    }
}
