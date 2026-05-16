package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.BankStatementRepository;
import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class BankStatementR2dbcRepositoryAdapter implements BankStatementRepository {

    private final BankStatementSpringDataRepository repository;

    public BankStatementR2dbcRepositoryAdapter(BankStatementSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<BankStatement> findById(UUID statementId) {
        return repository.findById(statementId).map(this::toDomain);
    }

    @Override
    public Flux<BankStatement> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Flux<BankStatement> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return repository.findAllByTenantIdAndBankAccountId(tenantId, bankAccountId)
                .map(this::toDomain);
    }

    @Override
    public Mono<BankStatement> findLatestByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return repository.findFirstByTenantIdAndBankAccountIdOrderByStatementDateDesc(tenantId, bankAccountId)
                .map(this::toDomain);
    }

    @Override
    public Mono<BankStatement> save(BankStatement bankStatement) {
        BankStatementEntity entity = new BankStatementEntity(bankStatement.id(), bankStatement.tenantId(),
                bankStatement.createdAt(), bankStatement.updatedAt(), bankStatement.organizationId(),
                bankStatement.bankAccountId(), bankStatement.statementNumber(), bankStatement.statementDate(),
                bankStatement.openingBalance(), bankStatement.closingBalance());
        return repository.save(entity).map(this::toDomain);
    }

    private BankStatement toDomain(BankStatementEntity entity) {
        return BankStatement.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.bankAccountId(), entity.statementNumber(), entity.statementDate(),
                entity.openingBalance(), entity.closingBalance());
    }
}
