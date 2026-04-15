package yowyob.comops.api.administration.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AdministrativePlatformOptionsSpringDataRepository
        extends ReactiveCrudRepository<AdministrativePlatformOptionsEntity, UUID> {

    Mono<AdministrativePlatformOptionsEntity> findByTenantId(UUID tenantId);
}
