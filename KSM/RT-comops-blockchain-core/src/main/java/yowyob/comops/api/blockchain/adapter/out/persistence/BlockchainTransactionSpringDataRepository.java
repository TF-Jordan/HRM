package yowyob.comops.api.blockchain.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BlockchainTransactionSpringDataRepository
        extends ReactiveCrudRepository<BlockchainTransactionEntity, UUID> {

    Flux<BlockchainTransactionEntity> findAllByTenantIdAndOrganizationIdAndChainCodeAndStatusOrderByCreatedAtAsc(
            UUID tenantId, UUID organizationId, String chainCode, String status);

    Flux<BlockchainTransactionEntity> findAllByTenantIdAndOrganizationIdAndChainCodeOrderByCreatedAtDesc(
            UUID tenantId, UUID organizationId, String chainCode);

    Flux<BlockchainTransactionEntity> findAllByBlockIdOrderByCreatedAtAsc(UUID blockId);
}
