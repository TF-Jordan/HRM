package yowyob.comops.api.blockchain.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("blockchain_transaction")
public record BlockchainTransactionEntity(
        @Id UUID id,
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
        String status,
        UUID blockId,
        Long blockHeight,
        Instant createdAt,
        Instant minedAt) {
}
