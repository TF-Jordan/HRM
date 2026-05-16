package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListBankStatementsUseCase {
    Flux<BankStatement> listStatements(UUID tenantId, UUID organizationId, UUID bankAccountId);
}
