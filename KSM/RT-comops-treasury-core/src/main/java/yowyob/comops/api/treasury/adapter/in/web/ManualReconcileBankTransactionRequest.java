package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ManualReconcileBankTransactionRequest(
        @NotNull UUID transactionId,
    UUID statementId,
    UUID statementLineId) {

    public UUID resolvedStatementId() {
        UUID resolved = statementId != null ? statementId : statementLineId;
        if (resolved == null) {
            throw new IllegalArgumentException("statementId or statementLineId is required");
        }
        return resolved;
    }
}
