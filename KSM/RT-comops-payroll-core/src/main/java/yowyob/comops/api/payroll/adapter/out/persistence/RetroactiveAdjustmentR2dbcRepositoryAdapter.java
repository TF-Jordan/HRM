package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.RetroactiveAdjustmentRepository;
import yowyob.comops.api.payroll.domain.model.RetroactiveAdjustment;
import yowyob.comops.api.payroll.domain.model.RetroactiveStatus;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class RetroactiveAdjustmentR2dbcRepositoryAdapter implements RetroactiveAdjustmentRepository {

    private final RetroactiveAdjustmentSpringDataRepository repository;

    public RetroactiveAdjustmentR2dbcRepositoryAdapter(RetroactiveAdjustmentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<RetroactiveAdjustment> save(RetroactiveAdjustment adjustment) {
        return repository.save(toEntity(adjustment)).map(this::toDomain);
    }

    @Override
    public Mono<RetroactiveAdjustment> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<RetroactiveAdjustment> findByEmployee(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<RetroactiveAdjustment> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private RetroactiveAdjustmentEntity toEntity(RetroactiveAdjustment a) {
        return new RetroactiveAdjustmentEntity(a.id(), a.tenantId(), a.createdAt(), a.updatedAt(),
                a.organizationId(), a.employeeId(), a.originPeriod(), a.targetPeriod(), a.reason(),
                a.currency(), a.oldGross(), a.newGross(), a.deltaGross(), a.oldNet(), a.newNet(),
                a.deltaNet(), a.status().name());
    }

    private RetroactiveAdjustment toDomain(RetroactiveAdjustmentEntity e) {
        return RetroactiveAdjustment.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.originPeriod(), e.targetPeriod(), e.reason(),
                e.currency(), e.oldGross(), e.newGross(), e.deltaGross(), e.oldNet(), e.newNet(),
                e.deltaNet(), RetroactiveStatus.valueOf(e.status()));
    }
}
