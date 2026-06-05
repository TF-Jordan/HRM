package yowyob.comops.api.payroll.application.port.out;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Provides the RSA key pair used to seal (and verify) generated documents. A default in-memory
 * provider is configured in payroll-core; production deployments can supply a persistent key.
 */
public interface DocumentSigningKeyProvider {

    String keyId();

    PrivateKey privateKey();

    PublicKey publicKey();
}
