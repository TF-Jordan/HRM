package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class BankStatementNotFoundException extends DomainException {
    public BankStatementNotFoundException(UUID statementId) {
        super("Bank statement not found: " + statementId);
    }
}
