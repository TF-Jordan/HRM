package yowyob.comops.api.treasury.application.port.out;

import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankStatementRepository {
    Mono<BankStatement> findById(UUID statementId);
    Flux<BankStatement> findByOrganizationId(UUID tenantId, UUID organizationId);
    Flux<BankStatement> findByBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<BankStatement> findLatestByBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<BankStatement> save(BankStatement bankStatement);
}
