package yowyob.comops.api.sales.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SalesOrderLineSpringDataRepository extends ReactiveCrudRepository<SalesOrderLineEntity, UUID> {

    Flux<SalesOrderLineEntity> findAllBySalesOrderIdOrderByCreatedAtAsc(UUID salesOrderId);

    Mono<Void> deleteAllBySalesOrderId(UUID salesOrderId);
}
