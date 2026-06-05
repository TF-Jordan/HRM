package yowyob.comops.api.payroll.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yowyob.comops.api.payroll.application.port.out.DocumentSigningKeyProvider;
import yowyob.comops.api.payroll.application.service.DocumentSigner;
import yowyob.comops.api.payroll.domain.model.DocumentSeal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class RsaDocumentSigningKeyProviderTest {

    @Test
    void autoGeneratesAnInMemoryKeyWhenNoPathConfigured() {
        DocumentSigningKeyProvider provider = new RsaDocumentSigningKeyProvider(
                "test-key", "", "");

        assertThat(provider.keyId()).isEqualTo("test-key");
        assertThat(provider.privateKey().getAlgorithm()).isEqualTo("RSA");
        assertThat(provider.publicKey().getAlgorithm()).isEqualTo("RSA");

        DocumentSeal seal = DocumentSigner.seal("payload", provider.privateKey(),
                provider.keyId(), "system");
        assertThat(DocumentSigner.verify("payload", seal, provider.publicKey())).isTrue();
    }

    @Test
    void loadsPemFilesAndProducesVerifiableSeals(@TempDir Path tmp) throws Exception {
        KeyPair pair = generate();
        Path priv = writePem(tmp.resolve("priv.pem"), "PRIVATE KEY", pair.getPrivate().getEncoded());
        Path pub = writePem(tmp.resolve("pub.pem"), "PUBLIC KEY", pair.getPublic().getEncoded());

        DocumentSigningKeyProvider provider = new RsaDocumentSigningKeyProvider(
                "payroll-doc-key-prod", priv.toString(), pub.toString());

        // The loaded keys match the original keypair: a seal signed by the loaded private key
        // verifies with the loaded public key, AND the loaded private key produces the same
        // signature as the original one (round-trip key material is intact).
        DocumentSeal seal = DocumentSigner.seal("payload", provider.privateKey(),
                provider.keyId(), "system");
        assertThat(DocumentSigner.verify("payload", seal, provider.publicKey())).isTrue();
        assertThat(DocumentSigner.verify("payload", seal, pair.getPublic())).isTrue();
    }

    @Test
    void rotatingTheKeyInvalidatesOldSealsButNewOnesStillVerify() {
        DocumentSigningKeyProvider v1 = new RsaDocumentSigningKeyProvider("k-v1", "", "");
        DocumentSeal oldSeal = DocumentSigner.seal("doc", v1.privateKey(), v1.keyId(), "system");

        DocumentSigningKeyProvider v2 = new RsaDocumentSigningKeyProvider("k-v2", "", "");
        DocumentSeal newSeal = DocumentSigner.seal("doc", v2.privateKey(), v2.keyId(), "system");

        // Documenting the rotation property: each public key only verifies its own seals.
        // This is why the keyId is stored on each PayrollDocument — a future multi-key provider
        // can route verification of an old document to its original public key.
        assertThat(DocumentSigner.verify("doc", oldSeal, v1.publicKey())).isTrue();
        assertThat(DocumentSigner.verify("doc", oldSeal, v2.publicKey())).isFalse();
        assertThat(DocumentSigner.verify("doc", newSeal, v2.publicKey())).isTrue();
        assertThat(DocumentSigner.verify("doc", newSeal, v1.publicKey())).isFalse();
    }

    private static KeyPair generate() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        return g.generateKeyPair();
    }

    private static Path writePem(Path path, String label, byte[] der) throws Exception {
        String pem = "-----BEGIN " + label + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der)
                + "\n-----END " + label + "-----\n";
        Files.writeString(path, pem);
        return path;
    }
}
