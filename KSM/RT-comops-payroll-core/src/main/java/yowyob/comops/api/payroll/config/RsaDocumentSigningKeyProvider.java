package yowyob.comops.api.payroll.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yowyob.comops.api.payroll.application.port.out.DocumentSigningKeyProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Provides the RSA key pair used to seal generated documents.
 *
 * <p>If {@code iwm.payroll.document-signing.private-key-path} (PKCS#8 PEM) and the matching public
 * key (X.509 PEM) are configured, they are loaded — the production setup, where the key is stable
 * so historical documents remain verifiable across restarts. Otherwise an in-memory RSA-2048 pair
 * is generated at startup (development only), with a warning.
 */
@Component
public class RsaDocumentSigningKeyProvider implements DocumentSigningKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaDocumentSigningKeyProvider.class);

    private final String keyId;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaDocumentSigningKeyProvider(
            @Value("${iwm.payroll.document-signing.key-id:payroll-doc-key-1}") String keyId,
            @Value("${iwm.payroll.document-signing.private-key-path:}") String privateKeyPath,
            @Value("${iwm.payroll.document-signing.public-key-path:}") String publicKeyPath) {
        this.keyId = keyId;
        if (privateKeyPath != null && !privateKeyPath.isBlank()
                && publicKeyPath != null && !publicKeyPath.isBlank()) {
            this.privateKey = loadPrivate(privateKeyPath);
            this.publicKey = loadPublic(publicKeyPath);
            log.info("Document signing key loaded from PEM (keyId={})", keyId);
        } else {
            KeyPair pair = generate();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
            log.warn("No document-signing key configured — generated an in-memory RSA key (keyId={}). "
                    + "Documents signed now will not verify after a restart. Configure "
                    + "iwm.payroll.document-signing.private-key-path for production.", keyId);
        }
    }

    @Override
    public String keyId() {
        return keyId;
    }

    @Override
    public PrivateKey privateKey() {
        return privateKey;
    }

    @Override
    public PublicKey publicKey() {
        return publicKey;
    }

    private static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate document signing key", e);
        }
    }

    private static PrivateKey loadPrivate(String path) {
        try {
            byte[] der = pemBody(Files.readString(Path.of(path)));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load document signing private key from " + path, e);
        }
    }

    private static PublicKey loadPublic(String path) {
        try {
            byte[] der = pemBody(Files.readString(Path.of(path)));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load document signing public key from " + path, e);
        }
    }

    private static byte[] pemBody(String pem) {
        String body = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }
}
