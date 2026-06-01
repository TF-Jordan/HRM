package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OpenReconciliationRequest(@NotNull UUID organizationId, @NotNull UUID bankAccountId,
        @NotNull UUID statementId, String referenceNumber) {
}
