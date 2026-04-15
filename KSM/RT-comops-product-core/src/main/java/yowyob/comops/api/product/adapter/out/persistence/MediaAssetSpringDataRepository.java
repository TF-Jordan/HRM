package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MediaAssetSpringDataRepository extends ReactiveCrudRepository<MediaAssetEntity, UUID> {

    Flux<MediaAssetEntity> findAllByTenantIdAndTargetTypeAndTargetIdOrderByPositionAsc(UUID tenantId, String targetType,
            UUID targetId);
}
