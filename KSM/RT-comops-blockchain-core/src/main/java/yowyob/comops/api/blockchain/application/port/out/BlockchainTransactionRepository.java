package yowyob.comops.api.blockchain.application.port.out;

import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BlockchainTransactionRepository {

    Mono<BlockchainTransaction> save(BlockchainTransaction transaction);

    Flux<BlockchainTransaction> findPending(UUID tenantId, UUID organizationId, String chainCode, int limit);

    Flux<BlockchainTransaction> findAll(UUID tenantId, UUID organizationId, String chainCode);

    Flux<BlockchainTransaction> findByBlockId(UUID blockId);
}
