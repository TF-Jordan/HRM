package yowyob.comops.api.accounting.extension.web;

import java.util.UUID;

public record OrganizationSummaryView(
        UUID id,
        String code,
        String shortName,
        String longName,
        String status,
        boolean isActive) {
}
