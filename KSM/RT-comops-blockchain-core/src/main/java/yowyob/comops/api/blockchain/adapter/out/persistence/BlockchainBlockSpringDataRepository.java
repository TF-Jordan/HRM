package yowyob.comops.api.blockchain.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BlockchainBlockSpringDataRepository extends ReactiveCrudRepository<BlockchainBlockEntity, UUID> {

    Mono<BlockchainBlockEntity> findFirstByTenantIdAndOrganizationIdAndChainCodeOrderByHeightDesc(
            UUID tenantId, UUID organizationId, String chainCode);

    Flux<BlockchainBlockEntity> findAllByTenantIdAndOrganizationIdAndChainCodeOrderByHeightAsc(
            UUID tenantId, UUID organizationId, String chainCode);
}
