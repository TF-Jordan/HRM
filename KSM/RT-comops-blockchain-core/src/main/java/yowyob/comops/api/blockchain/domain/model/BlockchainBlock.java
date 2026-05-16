package yowyob.comops.api.blockchain.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BlockchainBlock(
        UUID id,
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

    public static final String GENESIS_PREVIOUS_HASH = "0".repeat(64);

    public static BlockchainBlock genesis(UUID tenantId, UUID organizationId, String chainCode, String blockHash,
            int difficulty, Instant minedAt) {
        return new BlockchainBlock(UUID.randomUUID(), tenantId, organizationId,
                BlockchainTransaction.normalizeChain(chainCode), 0, GENESIS_PREVIOUS_HASH, GENESIS_PREVIOUS_HASH,
                blockHash, 0, difficulty, 0, "SYSTEM", minedAt);
    }

    public static BlockchainBlock mined(UUID tenantId, UUID organizationId, String chainCode, long height,
            String previousHash, String merkleRoot, String blockHash, long nonce, int difficulty, int transactionCount,
            String minedBy, Instant minedAt) {
        return new BlockchainBlock(UUID.randomUUID(), tenantId, organizationId,
                BlockchainTransaction.normalizeChain(chainCode), height, previousHash, merkleRoot, blockHash, nonce,
                difficulty, transactionCount, minedBy, minedAt);
    }
}
