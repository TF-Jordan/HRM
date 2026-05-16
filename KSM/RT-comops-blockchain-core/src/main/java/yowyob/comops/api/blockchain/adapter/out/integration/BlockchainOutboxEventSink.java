package yowyob.comops.api.blockchain.adapter.out.integration;

import yowyob.comops.api.blockchain.application.port.out.BlockchainBlockRepository;
import yowyob.comops.api.blockchain.application.port.out.BlockchainTransactionRepository;
import yowyob.comops.api.blockchain.application.service.BlockchainCrypto;
import yowyob.comops.api.blockchain.config.BlockchainLedgerProperties;
import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.kernel.application.port.out.BusinessEventDeliverySink;
import yowyob.comops.api.kernel.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Order(50)
public class BlockchainOutboxEventSink implements BusinessEventDeliverySink {

    private final BlockchainTransactionRepository transactionRepository;
    private final BlockchainBlockRepository blockRepository;
    private final BlockchainLedgerProperties properties;

    public BlockchainOutboxEventSink(BlockchainTransactionRepository transactionRepository,
            BlockchainBlockRepository blockRepository,
            BlockchainLedgerProperties properties) {
        this.transactionRepository = transactionRepository;
        this.blockRepository = blockRepository;
        this.properties = properties;
    }

    @Override
    public Mono<Void> deliver(OutboxEvent event) {
        if (!properties.isEnabled()) {
            return Mono.empty();
        }
        UUID organizationId = event.organizationId() == null ? properties.getSystemOrganizationId()
                : event.organizationId();
        String chainCode = BlockchainTransaction.normalizeChain(properties.getChainCode());
        String payload = canonicalPayload(event);
        String payloadHash = BlockchainCrypto.sha256Hex(payload);
        String sender = "COMOPS_OUTBOX";
        String sourceReference = event.aggregateType() + ":" + event.aggregateId();
        String signingPayload = BlockchainCrypto.signingPayload(chainCode, event.eventType(), "OUTBOX",
                sourceReference, payloadHash, sender);
        String signature = BlockchainCrypto.sha256Hex(signingPayload);
        String transactionHash = BlockchainCrypto.transactionHash(signingPayload, signature);
        BlockchainTransaction transaction = BlockchainTransaction.pending(event.tenantId(), organizationId, chainCode,
                event.eventType(), "OUTBOX", sourceReference, payload, payloadHash, sender, signature,
                transactionHash);
        Mono<BlockchainTransaction> saved = transactionRepository.save(transaction);
        if (!properties.isAutoMine()) {
            return saved.then();
        }
        return saved.then(mineOneEventBlock(event.tenantId(), organizationId, chainCode)).then();
    }

    private Mono<BlockchainBlock> mineOneEventBlock(UUID tenantId, UUID organizationId, String chainCode) {
        int difficulty = Math.max(0, Math.min(properties.getDifficulty(), 5));
        return ensureGenesis(tenantId, organizationId, chainCode, difficulty)
                .then(blockRepository.findLatest(tenantId, organizationId, chainCode))
                .flatMap(latest -> transactionRepository.findPending(tenantId, organizationId, chainCode, 100)
                        .collectList()
                        .flatMap(transactions -> {
                            if (transactions.isEmpty()) {
                                return Mono.just(latest);
                            }
                            String merkleRoot = BlockchainCrypto.merkleRoot(transactions.stream()
                                    .map(BlockchainTransaction::transactionHash)
                                    .toList());
                            long height = latest.height() + 1;
                            long nonce = 0;
                            String hash;
                            do {
                                hash = BlockchainCrypto.blockHash(height, latest.blockHash(), merkleRoot, nonce,
                                        difficulty, transactions.size(), properties.getMiner());
                                nonce++;
                            } while (!BlockchainCrypto.meetsDifficulty(hash, difficulty));
                            Instant minedAt = Instant.now();
                            BlockchainBlock block = BlockchainBlock.mined(tenantId, organizationId, chainCode, height,
                                    latest.blockHash(), merkleRoot, hash, nonce - 1, difficulty, transactions.size(),
                                    properties.getMiner(), minedAt);
                            return blockRepository.save(block)
                                    .flatMap(savedBlock -> Flux.fromIterable(transactions)
                                            .concatMap(transaction -> transactionRepository.save(transaction
                                                    .mined(savedBlock.id(), savedBlock.height(), minedAt)))
                                            .then(Mono.just(savedBlock)));
                        }));
    }

    private Mono<BlockchainBlock> ensureGenesis(UUID tenantId, UUID organizationId, String chainCode, int difficulty) {
        return blockRepository.findLatest(tenantId, organizationId, chainCode)
                .switchIfEmpty(Mono.defer(() -> {
                    String hash = BlockchainCrypto.blockHash(0, BlockchainBlock.GENESIS_PREVIOUS_HASH,
                            BlockchainBlock.GENESIS_PREVIOUS_HASH, 0, difficulty, 0, "SYSTEM");
                    return blockRepository.save(BlockchainBlock.genesis(tenantId, organizationId, chainCode, hash,
                            difficulty, Instant.now()));
                }));
    }

    private String canonicalPayload(OutboxEvent event) {
        return "outboxId=" + event.id()
                + "\ntenantId=" + event.tenantId()
                + "\norganizationId=" + event.organizationId()
                + "\neventType=" + event.eventType()
                + "\naggregateType=" + event.aggregateType()
                + "\naggregateId=" + event.aggregateId()
                + "\noccurredAt=" + event.occurredAt()
                + "\npayload=" + canonicalMap(event.payload());
    }

    private String canonicalMap(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return "{}";
        }
        return payload.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(entry -> entry.getKey() + "=" + String.valueOf(entry.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }
}
