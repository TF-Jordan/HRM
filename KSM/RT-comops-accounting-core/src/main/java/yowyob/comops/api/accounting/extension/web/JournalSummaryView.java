package yowyob.comops.api.accounting.extension.web;

import java.util.UUID;

public record JournalSummaryView(
        UUID id,
        String code,
        String label,
        String type,
        boolean active) {
}
