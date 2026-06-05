package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_document")
public record PayrollDocumentEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String type,
        UUID subjectId,
        String periode,
        UUID fileId,
        String fileName,
        String canonicalContent,
        String algorithm,
        String contentHashHex,
        String signatureBase64,
        String keyId,
        String signedBy,
        Instant signedAt,
        String verificationCode) implements PersistableEntity {
}
