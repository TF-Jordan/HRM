package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_attachment")
public record AccountingExtensionAttachmentEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("target_type")
        String targetType,
        @Column("target_id")
        UUID targetId,
        @Column("filename")
        String filename,
        @Column("content_type")
        String contentType,
        @Column("size_bytes")
        long sizeBytes,
        @Column("content")
        byte[] content,
        @Column("created_at")
        Instant createdAt) {
}
