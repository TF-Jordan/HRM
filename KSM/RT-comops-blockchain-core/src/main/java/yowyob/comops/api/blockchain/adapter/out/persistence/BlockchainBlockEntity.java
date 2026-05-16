package yowyob.comops.api.blockchain.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("blockchain_block")
public record BlockchainBlockEntity(
        @Id UUID id,
        UUID tenantId,
        UUID organizationId,
        String chainCode,
        long height,
        String previousHash,
        String merkleRoot,
        String blockHash,
        long nonce,
        int difficulty,
        int transactionCount,
        String minedBy,
        Instant minedAt) {
}
