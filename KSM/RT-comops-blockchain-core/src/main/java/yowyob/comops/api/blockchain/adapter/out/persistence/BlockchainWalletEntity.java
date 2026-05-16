package yowyob.comops.api.blockchain.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("blockchain_wallet")
public record BlockchainWalletEntity(
        @Id UUID id,
        UUID tenantId,
        UUID organizationId,
        String label,
        String publicKey,
        String fingerprint,
        Instant createdAt,
        boolean active) {
}
