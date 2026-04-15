package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.MaintenanceRecordRepository;
import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryMaintenanceRecordRepository implements MaintenanceRecordRepository {
    private final Map<UUID, MaintenanceRecord> store = new ConcurrentHashMap<>();

    @Override
    public Mono<MaintenanceRecord> save(MaintenanceRecord record) {
        return Mono.fromSupplier(() -> { store.put(record.id(), record); return record; });
    }

    @Override
    public Flux<MaintenanceRecord> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> item.resourceId().equals(resourceId))
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())));
    }
}
