package yowyob.comops.api.blockchain.application.port.out;

import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BlockchainWalletRepository {

    Mono<BlockchainWallet> save(BlockchainWallet wallet);

    Flux<BlockchainWallet> findAll(UUID tenantId, UUID organizationId);
}
