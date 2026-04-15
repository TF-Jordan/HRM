package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryI18nSpringDataRepository extends ReactiveCrudRepository<CategoryI18nEntity, UUID> {

    Mono<CategoryI18nEntity> findByTenantIdAndCategoryIdAndLocale(UUID tenantId, UUID categoryId, String locale);

    Flux<CategoryI18nEntity> findAllByTenantIdAndCategoryId(UUID tenantId, UUID categoryId);
}
