package yowyob.comops.api.treasury.application.port.in;

import java.util.UUID;

public record ManualReconcileBankTransactionCommand(UUID transactionId, UUID statementId) {
}
