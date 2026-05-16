package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_draft_entry")
public record AccountingExtensionDraftEntryEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("journal_id")
        UUID journalId,
        @Column("period_id")
        UUID periodId,
        @Column("reference")
        String reference,
        @Column("entry_date")
        Instant entryDate,
        @Column("lines_json")
        String linesJson,
        @Column("legacy_type")
        String legacyType,
        @Column("legacy_status")
        String legacyStatus,
        @Column("source_id")
        String sourceId,
        @Column("source_type")
        String sourceType,
        @Column("piece_number")
        String pieceNumber,
        @Column("label")
        String label,
        @Column("total_amount")
        BigDecimal totalAmount,
        @Column("currency")
        String currency,
        @Column("notes")
        String notes,
        @Column("attachment_ids_json")
        String attachmentIdsJson,
        @Column("created_by")
        String createdBy,
        @Column("validated_by")
        String validatedBy,
        @Column("validated_at")
        Instant validatedAt,
        @Column("rejected_by")
        String rejectedBy,
        @Column("rejected_at")
        Instant rejectedAt,
        @Column("rejection_reason")
        String rejectionReason,
        @Column("created_at")
        Instant createdAt,
        @Column("posted_at")
        Instant postedAt,
        @Column("entry_id")
        UUID entryId) {
}
