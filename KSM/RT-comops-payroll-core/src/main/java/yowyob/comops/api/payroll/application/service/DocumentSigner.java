package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.DocumentSeal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Produces and verifies the {@link DocumentSeal} (electronic signature) of a document. Uses only
 * the JDK's JCA — {@code SHA-256} digest and {@code SHA256withRSA} signature — so it needs no
 * external dependency and is fully deterministic/testable.
 *
 * <p>The seal is detached: it signs the document's <em>canonical content</em> (a stable textual
 * projection of the document data). Verification recomputes the digest from the canonical content
 * and checks the RSA signature, proving the document was issued by the holder of the private key
 * and has not been altered.
 */
public final class DocumentSigner {

    public static final String ALGORITHM = "SHA256withRSA";

    private DocumentSigner() {}

    /** SHA-256 digest of the content, hex-encoded. */
    public static String sha256Hex(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Signs the canonical content and returns a complete {@link DocumentSeal}. */
    public static DocumentSeal seal(String canonicalContent, PrivateKey privateKey, String keyId,
                                    String signedBy) {
        byte[] content = canonicalContent.getBytes(StandardCharsets.UTF_8);
        String hashHex = sha256Hex(content);
        byte[] signature = sign(content, privateKey);
        return new DocumentSeal(ALGORITHM, hashHex, Base64.getEncoder().encodeToString(signature),
                keyId, signedBy, Instant.now(), verificationCode(hashHex));
    }

    /** Verifies a seal against the document's canonical content and the signing public key. */
    public static boolean verify(String canonicalContent, DocumentSeal seal, PublicKey publicKey) {
        if (seal == null || seal.signatureBase64() == null) {
            return false;
        }
        byte[] content = canonicalContent.getBytes(StandardCharsets.UTF_8);
        if (!sha256Hex(content).equals(seal.contentHashHex())) {
            return false; // content changed since signing
        }
        try {
            Signature verifier = Signature.getInstance(ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(content);
            return verifier.verify(Base64.getDecoder().decode(seal.signatureBase64()));
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] sign(byte[] content, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance(ALGORITHM);
            signer.initSign(privateKey);
            signer.update(content);
            return signer.sign();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign document", e);
        }
    }

    /** A short, human-readable verification code derived from the digest (e.g. {@code A1B2-C3D4-E5F6}). */
    static String verificationCode(String hashHex) {
        String head = hashHex.substring(0, 12).toUpperCase();
        return head.substring(0, 4) + "-" + head.substring(4, 8) + "-" + head.substring(8, 12);
    }
}
