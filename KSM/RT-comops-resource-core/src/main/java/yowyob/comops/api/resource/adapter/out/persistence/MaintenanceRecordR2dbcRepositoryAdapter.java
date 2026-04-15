package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.MaintenanceRecordRepository;
import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class MaintenanceRecordR2dbcRepositoryAdapter implements MaintenanceRecordRepository {
    private final MaintenanceRecordSpringDataRepository repository;

    public MaintenanceRecordR2dbcRepositoryAdapter(MaintenanceRecordSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<MaintenanceRecord> save(MaintenanceRecord record) {
        MaintenanceRecordEntity entity = new MaintenanceRecordEntity(record.id(), record.tenantId(), record.createdAt(),
                record.updatedAt(), record.resourceId(), record.maintenanceType(), record.description(),
                record.status(), record.completedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<MaintenanceRecord> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findAllByTenantIdAndResourceIdOrderByCreatedAtDesc(tenantId, resourceId)
                .map(this::toDomain);
    }

    private MaintenanceRecord toDomain(MaintenanceRecordEntity entity) {
        return MaintenanceRecord.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.resourceId(), entity.maintenanceType(), entity.description(), entity.status(),
                entity.completedAt());
    }
}
