package yowyob.comops.api.treasury.application.port.in;

import java.util.UUID;

public record ReconciliationRunResult(UUID reconciliationId, int matchedTransactions) {
}
