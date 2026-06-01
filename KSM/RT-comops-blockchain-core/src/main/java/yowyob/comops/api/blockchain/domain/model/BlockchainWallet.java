package yowyob.comops.api.blockchain.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BlockchainWallet(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        String label,
        String publicKey,
        String fingerprint,
        Instant createdAt,
        boolean active) {

    public static BlockchainWallet create(UUID tenantId, UUID organizationId, String label, String publicKey,
            String fingerprint) {
        return new BlockchainWallet(UUID.randomUUID(), tenantId, organizationId, label, publicKey, fingerprint,
                Instant.now(), true);
    }
}
