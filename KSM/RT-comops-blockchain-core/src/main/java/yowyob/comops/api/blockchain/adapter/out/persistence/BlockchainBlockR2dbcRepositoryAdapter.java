package yowyob.comops.api.blockchain.adapter.out.persistence;

import yowyob.comops.api.blockchain.application.port.out.BlockchainBlockRepository;
import yowyob.comops.api.blockchain.domain.model.BlockchainBlock;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class BlockchainBlockR2dbcRepositoryAdapter implements BlockchainBlockRepository {

    private final BlockchainBlockSpringDataRepository repository;

    public BlockchainBlockR2dbcRepositoryAdapter(BlockchainBlockSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<BlockchainBlock> save(BlockchainBlock block) {
        return repository.save(toEntity(block)).map(this::toDomain);
    }

    @Override
    public Mono<BlockchainBlock> findLatest(UUID tenantId, UUID organizationId, String chainCode) {
        return repository.findFirstByTenantIdAndOrganizationIdAndChainCodeOrderByHeightDesc(tenantId, organizationId,
                chainCode).map(this::toDomain);
    }

    @Override
    public Flux<BlockchainBlock> findAll(UUID tenantId, UUID organizationId, String chainCode) {
        return repository.findAllByTenantIdAndOrganizationIdAndChainCodeOrderByHeightAsc(tenantId, organizationId,
                chainCode).map(this::toDomain);
    }

    private BlockchainBlockEntity toEntity(BlockchainBlock block) {
        return new BlockchainBlockEntity(block.id(), block.tenantId(), block.organizationId(), block.chainCode(),
                block.height(), block.previousHash(), block.merkleRoot(), block.blockHash(), block.nonce(),
                block.difficulty(), block.transactionCount(), block.minedBy(), block.minedAt());
    }

    private BlockchainBlock toDomain(BlockchainBlockEntity entity) {
        return new BlockchainBlock(entity.id(), entity.tenantId(), entity.organizationId(), entity.chainCode(),
                entity.height(), entity.previousHash(), entity.merkleRoot(), entity.blockHash(), entity.nonce(),
                entity.difficulty(), entity.transactionCount(), entity.minedBy(), entity.minedAt());
    }
}
