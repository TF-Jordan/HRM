package yowyob.comops.api.blockchain.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BlockchainWalletSpringDataRepository extends ReactiveCrudRepository<BlockchainWalletEntity, UUID> {

    Flux<BlockchainWalletEntity> findAllByTenantIdAndOrganizationIdAndActiveIsTrueOrderByCreatedAtDesc(
            UUID tenantId, UUID organizationId);
}
