package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import java.util.UUID;

public record AccountingJournalResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        String code,
        String label,
        String type,
        String notes,
        boolean active) {

    public static AccountingJournalResponse from(AccountingJournal journal) {
        return new AccountingJournalResponse(journal.id(), journal.tenantId(), journal.organizationId(),
                journal.code(), journal.label(), journal.type(), journal.notes(), journal.active());
    }
}
