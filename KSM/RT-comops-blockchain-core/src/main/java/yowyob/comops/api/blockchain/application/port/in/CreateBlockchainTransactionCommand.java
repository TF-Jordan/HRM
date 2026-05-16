package yowyob.comops.api.blockchain.application.port.in;

import java.util.UUID;

public record CreateBlockchainTransactionCommand(
        UUID organizationId,
        String chainCode,
        String transactionType,
        String sourceService,
        String sourceReference,
        String payload,
        String payloadHash,
        String senderPublicKey,
        String signature) {
}
