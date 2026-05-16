package yowyob.comops.api.billing.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Flux<PaymentEntity> findByOrganizationId(UUID organizationId);
}
