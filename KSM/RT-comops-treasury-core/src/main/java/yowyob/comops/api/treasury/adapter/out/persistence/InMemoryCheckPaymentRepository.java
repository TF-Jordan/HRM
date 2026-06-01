package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.CheckPaymentRepository;
import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryCheckPaymentRepository implements CheckPaymentRepository {
    private final Map<UUID, CheckPayment> checks = new ConcurrentHashMap<>();

    @Override
    public Mono<CheckPayment> findById(UUID checkPaymentId) {
        return Mono.justOrEmpty(checks.get(checkPaymentId));
    }

    @Override
    public Flux<CheckPayment> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(checks.values().stream()
                .filter(check -> check.tenantId().equals(tenantId))
                .filter(check -> check.organizationId().equals(organizationId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Flux<CheckPayment> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return Flux.fromStream(checks.values().stream()
                .filter(check -> check.tenantId().equals(tenantId))
                .filter(check -> check.bankAccountId().equals(bankAccountId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Mono<CheckPayment> save(CheckPayment checkPayment) {
        return Mono.fromSupplier(() -> { checks.put(checkPayment.id(), checkPayment); return checkPayment; });
    }
}
