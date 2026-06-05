package yowyob.comops.api.payroll.domain.model;

import java.time.Instant;

/**
 * A detached electronic seal over a generated document: the SHA-256 digest of the document's
 * canonical content and an RSA signature of that digest. Stored alongside the document and printed
 * on it (with the {@link #verificationCode}) so the document's authenticity and integrity can be
 * verified later — the legal "signature électronique" of the payslip / settlement / attestation.
 *
 * @param algorithm        signature algorithm (e.g. {@code SHA256withRSA})
 * @param contentHashHex   SHA-256 digest of the canonical content, hex-encoded
 * @param signatureBase64  RSA signature of the digest, Base64-encoded
 * @param keyId            identifier of the signing key
 * @param signedBy         who/what produced the seal (system or user reference)
 * @param signedAt         when the seal was produced
 * @param verificationCode short human-readable code (derived from the hash) shown on the document
 */
public record DocumentSeal(
        String algorithm,
        String contentHashHex,
        String signatureBase64,
        String keyId,
        String signedBy,
        Instant signedAt,
        String verificationCode) {
}
