package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetBankStatementUseCase {
    Mono<BankStatement> getStatement(UUID statementId);
}
