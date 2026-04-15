package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankStatement;
import reactor.core.publisher.Mono;

public interface RegisterBankStatementUseCase {
    Mono<BankStatement> registerStatement(RegisterBankStatementCommand command);
}
