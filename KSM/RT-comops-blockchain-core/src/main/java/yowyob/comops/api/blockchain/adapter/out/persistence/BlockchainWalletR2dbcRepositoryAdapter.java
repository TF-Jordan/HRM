package yowyob.comops.api.blockchain.adapter.out.persistence;

import yowyob.comops.api.blockchain.application.port.out.BlockchainWalletRepository;
import yowyob.comops.api.blockchain.domain.model.BlockchainWallet;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class BlockchainWalletR2dbcRepositoryAdapter implements BlockchainWalletRepository {

    private final BlockchainWalletSpringDataRepository repository;

    public BlockchainWalletR2dbcRepositoryAdapter(BlockchainWalletSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<BlockchainWallet> save(BlockchainWallet wallet) {
        return repository.save(toEntity(wallet)).map(this::toDomain);
    }

    @Override
    public Flux<BlockchainWallet> findAll(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationIdAndActiveIsTrueOrderByCreatedAtDesc(tenantId,
                organizationId).map(this::toDomain);
    }

    private BlockchainWalletEntity toEntity(BlockchainWallet wallet) {
        return new BlockchainWalletEntity(wallet.id(), wallet.tenantId(), wallet.organizationId(), wallet.label(),
                wallet.publicKey(), wallet.fingerprint(), wallet.createdAt(), wallet.active());
    }

    private BlockchainWallet toDomain(BlockchainWalletEntity entity) {
        return new BlockchainWallet(entity.id(), entity.tenantId(), entity.organizationId(), entity.label(),
                entity.publicKey(), entity.fingerprint(), entity.createdAt(), entity.active());
    }
}
