package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.domain.model.DocumentSeal;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentSignerTest {

    private static KeyPair keyPair;

    @BeforeAll
    static void generateKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
    }

    @Test
    void sealVerifiesAgainstUnchangedContent() {
        String content = "PAYSLIP|2026-10|EMP-001|NET=332066";
        DocumentSeal seal = DocumentSigner.seal(content, keyPair.getPrivate(), "payroll-key-1", "system");

        assertThat(seal.algorithm()).isEqualTo("SHA256withRSA");
        assertThat(seal.contentHashHex()).hasSize(64);
        assertThat(seal.verificationCode()).matches("[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}");
        assertThat(DocumentSigner.verify(content, seal, keyPair.getPublic())).isTrue();
    }

    @Test
    void verificationFailsWhenContentTampered() {
        String content = "PAYSLIP|2026-10|EMP-001|NET=332066";
        DocumentSeal seal = DocumentSigner.seal(content, keyPair.getPrivate(), "payroll-key-1", "system");

        String tampered = "PAYSLIP|2026-10|EMP-001|NET=999999";
        assertThat(DocumentSigner.verify(tampered, seal, keyPair.getPublic())).isFalse();
    }

    @Test
    void verificationFailsWithWrongKey() throws Exception {
        String content = "PAYSLIP|2026-10|EMP-001|NET=332066";
        DocumentSeal seal = DocumentSigner.seal(content, keyPair.getPrivate(), "payroll-key-1", "system");

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair other = generator.generateKeyPair();
        assertThat(DocumentSigner.verify(content, seal, other.getPublic())).isFalse();
    }

    @Test
    void digestIsDeterministic() {
        assertThat(DocumentSigner.sha256Hex("abc".getBytes()))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
