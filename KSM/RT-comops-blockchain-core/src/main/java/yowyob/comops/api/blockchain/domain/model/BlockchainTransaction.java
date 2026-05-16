package yowyob.comops.api.blockchain.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BlockchainTransaction(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        String chainCode,
        String transactionType,
        String sourceService,
        String sourceReference,
        String payload,
        String payloadHash,
        String senderPublicKey,
        String signature,
        String transactionHash,
        BlockchainTransactionStatus status,
        UUID blockId,
        Long blockHeight,
        Instant createdAt,
        Instant minedAt) {

    public static BlockchainTransaction pending(UUID tenantId, UUID organizationId, String chainCode,
            String transactionType, String sourceService, String sourceReference, String payload, String payloadHash,
            String senderPublicKey, String signature, String transactionHash) {
        return new BlockchainTransaction(UUID.randomUUID(), tenantId, organizationId, normalizeChain(chainCode),
                transactionType, sourceService, sourceReference, payload, payloadHash, senderPublicKey, signature,
                transactionHash, BlockchainTransactionStatus.PENDING, null, null, Instant.now(), null);
    }

    public BlockchainTransaction mined(UUID blockId, long blockHeight, Instant minedAt) {
        return new BlockchainTransaction(id, tenantId, organizationId, chainCode, transactionType, sourceService,
                sourceReference, payload, payloadHash, senderPublicKey, signature, transactionHash,
                BlockchainTransactionStatus.MINED, blockId, blockHeight, createdAt, minedAt);
    }

    public static String normalizeChain(String chainCode) {
        return chainCode == null || chainCode.isBlank() ? "COMOPS_MAIN" : chainCode.trim().toUpperCase();
    }
}
