package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Registry entry for a generated, electronically-sealed payroll document. It records the stored
 * PDF ({@code fileId}), the signed canonical content, and the {@link DocumentSeal} fields so the
 * document's authenticity and integrity can be re-verified at any time without re-rendering.
 */
public final class PayrollDocument extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final PayrollDocumentType type;
    private final UUID subjectId;
    private final String periode;
    private final UUID fileId;
    private final String fileName;
    private final String canonicalContent;
    private final String algorithm;
    private final String contentHashHex;
    private final String signatureBase64;
    private final String keyId;
    private final String signedBy;
    private final Instant signedAt;
    private final String verificationCode;

    private PayrollDocument(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                            UUID organizationId, UUID employeeId, PayrollDocumentType type, UUID subjectId,
                            String periode, UUID fileId, String fileName, String canonicalContent,
                            String algorithm, String contentHashHex, String signatureBase64, String keyId,
                            String signedBy, Instant signedAt, String verificationCode) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.subjectId = Objects.requireNonNull(subjectId, "subjectId is required");
        this.fileId = Objects.requireNonNull(fileId, "fileId is required");
        this.fileName = Objects.requireNonNull(fileName, "fileName is required");
        this.canonicalContent = Objects.requireNonNull(canonicalContent, "canonicalContent is required");
        this.contentHashHex = Objects.requireNonNull(contentHashHex, "contentHashHex is required");
        this.signatureBase64 = Objects.requireNonNull(signatureBase64, "signatureBase64 is required");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm is required");
        this.keyId = keyId;
        this.signedBy = signedBy;
        this.signedAt = signedAt;
        this.verificationCode = verificationCode;
        this.periode = periode;
    }

    public static PayrollDocument create(UUID tenantId, UUID organizationId, UUID employeeId,
                                         PayrollDocumentType type, UUID subjectId, String periode,
                                         UUID fileId, String fileName, String canonicalContent,
                                         DocumentSeal seal) {
        Instant now = Instant.now();
        return new PayrollDocument(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId, type,
                subjectId, periode, fileId, fileName, canonicalContent, seal.algorithm(),
                seal.contentHashHex(), seal.signatureBase64(), seal.keyId(), seal.signedBy(),
                seal.signedAt(), seal.verificationCode());
    }

    public static PayrollDocument rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            UUID organizationId, UUID employeeId, PayrollDocumentType type,
                                            UUID subjectId, String periode, UUID fileId, String fileName,
                                            String canonicalContent, String algorithm, String contentHashHex,
                                            String signatureBase64, String keyId, String signedBy,
                                            Instant signedAt, String verificationCode) {
        return new PayrollDocument(id, tenantId, createdAt, updatedAt, organizationId, employeeId, type,
                subjectId, periode, fileId, fileName, canonicalContent, algorithm, contentHashHex,
                signatureBase64, keyId, signedBy, signedAt, verificationCode);
    }

    public DocumentSeal seal() {
        return new DocumentSeal(algorithm, contentHashHex, signatureBase64, keyId, signedBy, signedAt,
                verificationCode);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public PayrollDocumentType type() { return type; }
    public UUID subjectId() { return subjectId; }
    public String periode() { return periode; }
    public UUID fileId() { return fileId; }
    public String fileName() { return fileName; }
    public String canonicalContent() { return canonicalContent; }
    public String algorithm() { return algorithm; }
    public String contentHashHex() { return contentHashHex; }
    public String signatureBase64() { return signatureBase64; }
    public String keyId() { return keyId; }
    public String signedBy() { return signedBy; }
    public Instant signedAt() { return signedAt; }
    public String verificationCode() { return verificationCode; }
}
