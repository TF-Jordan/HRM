package yowyob.comops.api.accounting.extension.web;

import java.util.UUID;

public record DocumentSequenceSummaryView(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String documentType,
        String prefix,
        String suffix,
        int paddingWidth,
        long nextNumber) {
}
