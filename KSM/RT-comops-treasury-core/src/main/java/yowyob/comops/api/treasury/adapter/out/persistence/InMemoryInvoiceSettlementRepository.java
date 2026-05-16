package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.InvoiceSettlementRepository;
import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryInvoiceSettlementRepository implements InvoiceSettlementRepository {

    private final Map<UUID, InvoiceSettlement> settlements = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsBySettlementNumber(UUID tenantId, UUID organizationId, String settlementNumber) {
        return Mono.fromSupplier(() -> settlements.values().stream()
                .filter(settlement -> settlement.tenantId().equals(tenantId))
                .filter(settlement -> settlement.organizationId().equals(organizationId))
                .anyMatch(settlement -> settlement.settlementNumber().equalsIgnoreCase(settlementNumber)));
    }

    @Override
    public Mono<InvoiceSettlement> findById(UUID settlementId) {
        return Mono.justOrEmpty(settlements.get(settlementId));
    }

    @Override
    public Flux<InvoiceSettlement> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(settlements.values().stream()
                .filter(settlement -> settlement.tenantId().equals(tenantId))
                .filter(settlement -> settlement.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<InvoiceSettlement> save(InvoiceSettlement settlement) {
        return Mono.fromSupplier(() -> {
            settlements.put(settlement.id(), settlement);
            return settlement;
        });
    }
}
