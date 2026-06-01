package yowyob.comops.api.blockchain.application.port.out;

import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BlockchainBlockRepository {

    Mono<BlockchainBlock> save(BlockchainBlock block);

    Mono<BlockchainBlock> findLatest(UUID tenantId, UUID organizationId, String chainCode);

    Flux<BlockchainBlock> findAll(UUID tenantId, UUID organizationId, String chainCode);
}
