package yowyob.comops.api.blockchain.adapter.out.persistence;

import yowyob.comops.api.blockchain.application.port.out.BlockchainTransactionRepository;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransaction;
import yowyob.comops.api.blockchain.domain.model.BlockchainTransactionStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class BlockchainTransactionR2dbcRepositoryAdapter implements BlockchainTransactionRepository {

    private final BlockchainTransactionSpringDataRepository repository;

    public BlockchainTransactionR2dbcRepositoryAdapter(BlockchainTransactionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<BlockchainTransaction> save(BlockchainTransaction transaction) {
        return repository.save(toEntity(transaction)).map(this::toDomain);
    }

    @Override
    public Flux<BlockchainTransaction> findPending(UUID tenantId, UUID organizationId, String chainCode, int limit) {
        return repository.findAllByTenantIdAndOrganizationIdAndChainCodeAndStatusOrderByCreatedAtAsc(tenantId,
                organizationId, chainCode, BlockchainTransactionStatus.PENDING.name()).take(limit).map(this::toDomain);
    }

    @Override
    public Flux<BlockchainTransaction> findAll(UUID tenantId, UUID organizationId, String chainCode) {
        return repository.findAllByTenantIdAndOrganizationIdAndChainCodeOrderByCreatedAtDesc(tenantId, organizationId,
                chainCode).map(this::toDomain);
    }

    @Override
    public Flux<BlockchainTransaction> findByBlockId(UUID blockId) {
        return repository.findAllByBlockIdOrderByCreatedAtAsc(blockId).map(this::toDomain);
    }

    private BlockchainTransactionEntity toEntity(BlockchainTransaction transaction) {
        return new BlockchainTransactionEntity(transaction.id(), transaction.tenantId(), transaction.organizationId(),
                transaction.chainCode(), transaction.transactionType(), transaction.sourceService(),
                transaction.sourceReference(), transaction.payload(), transaction.payloadHash(),
                transaction.senderPublicKey(), transaction.signature(), transaction.transactionHash(),
                transaction.status().name(), transaction.blockId(), transaction.blockHeight(), transaction.createdAt(),
                transaction.minedAt());
    }

    private BlockchainTransaction toDomain(BlockchainTransactionEntity entity) {
        return new BlockchainTransaction(entity.id(), entity.tenantId(), entity.organizationId(), entity.chainCode(),
                entity.transactionType(), entity.sourceService(), entity.sourceReference(), entity.payload(),
                entity.payloadHash(), entity.senderPublicKey(), entity.signature(), entity.transactionHash(),
                BlockchainTransactionStatus.valueOf(entity.status()), entity.blockId(), entity.blockHeight(),
                entity.createdAt(), entity.minedAt());
    }
}
